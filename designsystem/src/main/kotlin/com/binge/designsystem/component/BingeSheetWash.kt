package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R

/** Where a page of a sheet asks its container to wash the top: the colour, and how far down the header reaches. */
@Stable
internal class BingeSheetWash {
    var tint by mutableStateOf<Color?>(null)
    var headerBottom by mutableFloatStateOf(0f)
    var containerTop by mutableFloatStateOf(0f)

    fun depth(): Float = (headerBottom - containerTop).coerceAtLeast(0f)
}

internal val LocalBingeSheetWash = staticCompositionLocalOf<BingeSheetWash?> { null }

@Composable
internal fun rememberBingeSheetWash(): BingeSheetWash = remember { BingeSheetWash() }

/**
 * Washes the sheet's top with [tint], from the top of the sheet down behind this block, across its full width, fading into
 * the sheet's own colour below it. Put it on the header block whose colour the page wants the sheet to take.
 *
 * The wash goes when the block leaves composition, so a page opened over another brings its own and a page without one has
 * none. Outside a [BingeAdaptiveSheet] it does nothing.
 */
@Composable
fun Modifier.bingeSheetWash(tint: Color): Modifier {
    val wash = LocalBingeSheetWash.current
    DisposableEffect(wash, tint) {
        wash?.tint = tint
        onDispose { if (wash?.tint == tint) wash.tint = null }
    }
    return onGloballyPositioned { wash?.headerBottom = it.positionInWindow().y + it.size.height }
}

/**
 * Reports the container's top, and draws [wash] behind everything it holds. A bottom sheet's content starts below its drag
 * handle, so [overdrawTop] lets the wash reach up over the handle too; the sheet's own shape clips what spills past its top.
 */
@Composable
internal fun Modifier.bingeSheetWashBehind(wash: BingeSheetWash, overdrawTop: Boolean = false): Modifier {
    val density = LocalDensity.current
    val fade = with(density) { dimensionResource(R.dimen.sheet_wash_fade).toPx() }
    val overdraw = if (overdrawTop) with(density) { dimensionResource(R.dimen.sheet_wash_overdraw).toPx() } else 0f
    return onGloballyPositioned { wash.containerTop = it.positionInWindow().y }.drawBehind {
        val tint = wash.tint ?: return@drawBehind
        val bottom = wash.depth() + fade
        drawRect(
            topLeft = Offset(0f, -overdraw),
            brush =
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            0f to tint.copy(alpha = WASH_TOP_ALPHA),
                            WASH_MID_STOP to tint.copy(alpha = WASH_MID_ALPHA),
                            1f to Color.Transparent,
                        ),
                    startY = -overdraw,
                    endY = bottom,
                ),
            size = Size(size.width, bottom + overdraw),
        )
    }
}

private const val WASH_TOP_ALPHA = 0.6f
private const val WASH_MID_ALPHA = 0.25f
private const val WASH_MID_STOP = 0.55f
