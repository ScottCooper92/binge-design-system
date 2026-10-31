package com.binge.designsystem.tv.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.R as TvR

/**
 * Where a page of a side sheet asks its panel to wash the top: the colour, and how far down the page's header reaches. The
 * panel draws it, the header block sets it through [tvSideSheetWash].
 */
internal class TvSideSheetWash {
    var tint by mutableStateOf<Color?>(null)

    /** The header's bottom edge in the window, and the panel's top edge, so the wash's depth is the header's depth in the panel. */
    var headerBottom by mutableFloatStateOf(0f)
    var panelTop by mutableFloatStateOf(0f)

    fun depth(): Float = (headerBottom - panelTop).coerceAtLeast(0f)
}

internal val LocalTvSideSheetWash = staticCompositionLocalOf<TvSideSheetWash?> { null }

/**
 * Washes the panel's top with [tint], from the top of the screen down behind this block, across the panel's full width,
 * fading into the panel below it. Put it on the header block, the one whose colour the page wants the sheet to take.
 *
 * Only one page's header washes at a time. The wash goes when the block leaves composition, so a page opened over another
 * brings its own and a page without one has none.
 */
@Composable
fun Modifier.tvSideSheetWash(tint: Color): Modifier {
    val wash = LocalTvSideSheetWash.current
    DisposableEffect(wash, tint) {
        wash?.tint = tint
        onDispose { if (wash?.tint == tint) wash.tint = null }
    }
    return onGloballyPositioned { coordinates: LayoutCoordinates ->
        wash?.headerBottom = coordinates.positionInWindow().y + coordinates.size.height
    }
}

/** Reports the panel's top, and draws [wash] behind everything the panel holds. */
@Composable
internal fun Modifier.tvSideSheetWashBehind(wash: TvSideSheetWash): Modifier {
    val fade = with(LocalDensity.current) { dimensionResource(TvR.dimen.tv_side_sheet_wash_fade).toPx() }
    return onGloballyPositioned { wash.panelTop = it.positionInWindow().y }.drawBehind {
        val tint = wash.tint ?: return@drawBehind
        val bottom = wash.depth() + fade
        drawRect(
            brush =
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            0f to tint.copy(alpha = WASH_TOP_ALPHA),
                            WASH_MID_STOP to tint.copy(alpha = WASH_MID_ALPHA),
                            1f to Color.Transparent,
                        ),
                    endY = bottom,
                ),
            size = Size(size.width, bottom),
        )
    }
}

private const val WASH_TOP_ALPHA = 0.6f
private const val WASH_MID_ALPHA = 0.25f
private const val WASH_MID_STOP = 0.55f
