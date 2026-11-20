package com.binge.designsystem.tv.template

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import androidx.tv.material3.MaterialTheme
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.binge.designsystem.theme.LocalReduceMotion
import kotlin.math.roundToInt

/** The portrait's height as a fraction of the screen's: 1.0 shows the whole photo, more crops its foot and grows it across the page. */
private const val PORTRAIT_HEIGHT_FRACTION = 1.4f
private const val PORTRAIT_ASPECT = 2f / 3f
private const val PORTRAIT_SCRIM_END = 0.85f
private const val TINT_FADE_MILLIS = 400

/**
 * A portrait right-aligned at the top, its left edge melting into a wash of its own colour. Shown at rest, gone once the page
 * expands onto a tab. Its height is [PORTRAIT_HEIGHT_FRACTION] of the screen's, placed by [portraitFrame] so a value above 1 is
 * not clamped to the screen and crops the foot under the bottom fade and the tabs.
 *
 * The wash is the photo's darkened left-edge colour ([edgeTint]), taken once the photo is decoded and faded in over the page
 * background; until then, and with no sample, the scrim ends in the page background. A [seedTint], an edge colour to darken as a
 * sample is, stands in for the sample. [fallbackUrl] names a smaller form of the photo to try when it fails to load, or null for
 * none.
 */
@Composable
internal fun TvDetailPortrait(
    url: String,
    seedTint: Color?,
    expanded: Boolean,
    fallbackUrl: (String) -> String? = { null },
) {
    val reduceMotion = LocalReduceMotion.current
    AnimatedVisibility(
        visible = !expanded,
        enter = fadeIn(if (reduceMotion) snap() else tween(DETAIL_SWAP_MILLIS)),
        exit = fadeOut(if (reduceMotion) snap() else tween(DETAIL_SWAP_MILLIS)),
    ) {
        val page = MaterialTheme.colorScheme.background
        var model by remember(url) { mutableStateOf(url) }
        var sampled by remember(url) { mutableStateOf<Color?>(null) }
        val context = LocalContext.current
        // A hardware bitmap cannot be read for its edge colour.
        val request = remember(model) {
            ImageRequest
                .Builder(context)
                .data(model)
                .allowHardware(false)
                .build()
        }
        val tint = seedTint?.let(::clampTint) ?: sampled
        val edge by animateColorAsState(
            targetValue = tint ?: page,
            animationSpec = if (reduceMotion) snap() else tween(TINT_FADE_MILLIS),
            label = "portraitTint",
        )
        Box(Modifier.fillMaxSize().clipToBounds()) {
            Box(Modifier.fillMaxSize().leftWash(edge, tint != null))
            Box(Modifier.portraitLayout(PORTRAIT_HEIGHT_FRACTION)) {
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopCenter,
                    onError = { fallbackUrl(model)?.let { model = it } },
                    onSuccess = { state -> if (sampled == null) sampled = state.result.image.edgeTint() },
                    modifier = Modifier.fillMaxSize(),
                )
                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(0f to edge, PORTRAIT_SCRIM_END to Color.Transparent)))
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(DETAIL_SCRIM_START to Color.Transparent, 1f to edge)),
            )
        }
    }
}

/** Fills everything left of the portrait with [color], so the copy sits on the tint rather than on the page; nothing without one. */
private fun Modifier.leftWash(color: Color, enabled: Boolean): Modifier =
    if (!enabled) {
        this
    } else {
        drawBehind {
            val frame = portraitFrame(size.width.roundToInt(), size.height.roundToInt(), PORTRAIT_HEIGHT_FRACTION)
            drawRect(color, size = Size(frame.x.toFloat(), size.height))
        }
    }

/** Where the portrait sits in a page of [pageWidth] by [pageHeight] px: its size, and the x that right-aligns it. */
internal data class PortraitFrame(
    val width: Int,
    val height: Int,
    val x: Int,
)

/** A [fraction] of the page's height tall and 2:3, flush right; the height may exceed the page's, which the parent clips. */
internal fun portraitFrame(
    pageWidth: Int,
    pageHeight: Int,
    fraction: Float,
): PortraitFrame {
    val height = (pageHeight * fraction).roundToInt()
    val width = (height * PORTRAIT_ASPECT).roundToInt()
    return PortraitFrame(width = width, height = height, x = pageWidth - width)
}

/** Measures the child at [portraitFrame]'s size, which `Modifier.height` cannot: it is clamped to the incoming maximum. */
private fun Modifier.portraitLayout(fraction: Float): Modifier =
    layout { measurable, constraints ->
        val frame = portraitFrame(constraints.maxWidth, constraints.maxHeight, fraction)
        val placeable = measurable.measure(Constraints.fixed(frame.width, frame.height))
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(frame.x, 0) }
    }
