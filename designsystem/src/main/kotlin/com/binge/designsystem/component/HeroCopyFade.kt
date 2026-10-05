package com.binge.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.R

/**
 * How a page-colour fade climbs behind a block of copy. Above the copy it rises through [easeIn] —
 * pairs of (fraction of the band above the copy, alpha) — so it starts without a visible edge. It
 * reaches [atCopyTop] at the copy's first line, [midway] halfway from there to the foot, and the solid
 * page colour at the foot.
 */
internal class PageFadeRamp(
    val easeIn: List<Pair<Float, Float>>,
    val atCopyTop: Float,
    val midway: Float,
)

/**
 * The ramp behind a hero's title, genres and tagline. Under half strength at the first line, so the
 * art still shows through the copy, and only solid at the foot, where the hero meets the page.
 */
internal val CopyFadeRamp = PageFadeRamp(
    easeIn = listOf(0.2f to 0.03f, 0.4f to 0.09f, 0.6f to 0.18f, 0.8f to 0.30f),
    atCopyTop = 0.45f,
    midway = 0.72f,
)

/**
 * A stronger ramp, for a row of small labels (a facts row) that has to stay legible across the full
 * width of the art, not only beside the copy.
 */
internal val StatsFadeRamp = PageFadeRamp(
    easeIn = listOf(0.2f to 0.04f, 0.4f to 0.12f, 0.6f to 0.26f, 0.8f to 0.44f),
    atCopyTop = 0.62f,
    midway = 0.85f,
)

/**
 * The full-width band at a carousel's foot: eased like [CopyFadeRamp], so the two read as one, and
 * solid at the bottom edge. Pairs of (fraction of the band, alpha).
 */
private val SeamBandStops = listOf(
    0.15f to 0.03f,
    0.30f to 0.10f,
    0.45f to 0.22f,
    0.60f to 0.40f,
    0.80f to 0.72f,
)

/**
 * The page-colour mask past the copy's end edge, as pairs of (fraction of the falloff, alpha). Full
 * strength behind the copy, then falling away, so a short title leaves the far side of the art raw.
 */
private val FalloffStops = listOf(0.35f to 0.75f, 0.70f to 0.30f)

/**
 * A fade in the theme's page colour behind a hero's copy column, so the copy reads in
 * `onBackground` over any art, in either theme, and the hero ends on the colour the page starts with.
 *
 * Apply it to a full-width column that ends at the hero's bottom edge. It eases in over
 * [R.dimen.hero_copy_fade_band] above the column, so there is no edge where it starts. It is 45% by
 * the column's top, 72% halfway down and solid at the column's foot. It draws outside the column's
 * top edge, so nothing between the column and the art should clip.
 */
@Composable
fun Modifier.heroCopyFade(): Modifier {
    val page = MaterialTheme.colorScheme.background
    val bandPx = with(LocalDensity.current) { dimensionResource(R.dimen.hero_copy_fade_band).toPx() }
    return drawBehind { drawPageFade(page, copyTop = 0f, bandPx = bandPx, ramp = CopyFadeRamp, clampToBounds = false) }
}

/**
 * Draws a page-colour fade that eases in over [bandPx] above [copyTop] and runs to solid at the
 * bottom of the draw area, following [ramp]. With [clampToBounds] the fade starts no higher than the
 * top of the draw area, and the eased band shortens to fit.
 */
internal fun DrawScope.drawPageFade(
    page: Color,
    copyTop: Float,
    bandPx: Float,
    ramp: PageFadeRamp,
    clampToBounds: Boolean = true,
) {
    val top = (copyTop - bandPx).let { if (clampToBounds) it.coerceAtLeast(0f) else it }
    val span = (size.height - top).coerceAtLeast(1f)
    val band = (copyTop - top) / span
    val stops = buildList {
        add(0f to Color.Transparent)
        ramp.easeIn.forEach { (at, alpha) -> add(band * at to page.copy(alpha = alpha)) }
        add(band to page.copy(alpha = ramp.atCopyTop))
        add(band + (1f - band) / 2f to page.copy(alpha = ramp.midway))
        add(1f to page)
    }
    drawRect(
        brush = Brush.verticalGradient(*stops.toTypedArray(), startY = top, endY = size.height),
        topLeft = Offset(0f, top),
        size = Size(size.width, span),
    )
}

/**
 * Masks what has been drawn so far to the copy's side: full strength up to [copyEnd] (an x position
 * in this draw area), falling away over [falloffPx] past it. The copy's end edge is its right in a
 * left-to-right layout and its left in a right-to-left one. Needs an offscreen layer to mask into.
 */
internal fun DrawScope.maskPastCopyEnd(copyEnd: Float, falloffPx: Float) {
    val fadeTo = if (layoutDirection == LayoutDirection.Rtl) copyEnd - falloffPx else copyEnd + falloffPx
    val stops = buildList {
        add(0f to Color.Black)
        FalloffStops.forEach { (at, alpha) -> add(at to Color.Black.copy(alpha = alpha)) }
        add(1f to Color.Transparent)
    }
    drawRect(
        brush = Brush.horizontalGradient(*stops.toTypedArray(), startX = copyEnd, endX = fadeTo),
        blendMode = BlendMode.DstIn,
    )
}

/** Draws the full-width seam band, [bandPx] tall, at the bottom of the draw area. */
internal fun DrawScope.drawSeamBand(page: Color, bandPx: Float) {
    val top = size.height - bandPx
    val stops = buildList {
        add(0f to Color.Transparent)
        SeamBandStops.forEach { (at, alpha) -> add(at to page.copy(alpha = alpha)) }
        add(1f to page)
    }
    drawRect(
        brush = Brush.verticalGradient(*stops.toTypedArray(), startY = top, endY = size.height),
        topLeft = Offset(0f, top),
        size = Size(size.width, bandPx),
    )
}
