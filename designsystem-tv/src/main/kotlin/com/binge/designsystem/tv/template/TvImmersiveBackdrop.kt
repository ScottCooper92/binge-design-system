package com.binge.designsystem.tv.template

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.startHorizontalGradient
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.TV_IMMERSIVE_CROSSFADE_MILLIS
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.LocalTvPaneShift
import com.binge.designsystem.tv.nav.ReportTvRailArtwork
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SCRIM_START_ALPHA = 0.86f
private const val SCRIM_START_HOLD_ALPHA = 0.72f
private const val SCRIM_START_MID_ALPHA = 0.4f
private const val SCRIM_START_TAIL_ALPHA = 0.12f
private const val SCRIM_BOTTOM_START_FRACTION = 0.38f
private const val SCRIM_BOTTOM_ALPHA = 0.96f

/**
 * How wide the backdrop's copy runs, and where the start scrim holds and fades to keep it legible. The two are one
 * decision: the scrim holds its density across the copy's width, then eases away, so a wider copy needs a wider hold.
 */
@Immutable
class TvImmersiveCopyLayout(
    val copyWidthFraction: Float,
    val scrimHoldFraction: Float,
    val scrimMidFraction: Float,
    val scrimTailFraction: Float,
) {
    companion object {
        /** Copy across three quarters of the screen, for a short title and meta line. */
        val Wide =
            TvImmersiveCopyLayout(copyWidthFraction = 0.75f, scrimHoldFraction = 0.75f, scrimMidFraction = 0.87f, scrimTailFraction = 0.95f)

        /** Copy across just over half, so more of the artwork shows beside a title and a two-line synopsis. */
        val Narrow =
            TvImmersiveCopyLayout(copyWidthFraction = 0.56f, scrimHoldFraction = 0.56f, scrimMidFraction = 0.74f, scrimTailFraction = 0.9f)
    }
}

/**
 * The backdrop layer: [artwork] full-bleed under a scrim, with [copy] in the band above the rows.
 *
 * [copyTopInset] reserves space at the top of the copy's band for a caller drawing over it, such as the grid's
 * heading. The copy is bottom-anchored, so without it a tall block would overflow up into the heading. [copyStart] is
 * where the copy starts: the content gutter by default, or a page's own start edge ([tvPagePadding]) for one that pads its
 * heading and cards by that. [layout] sets the copy's width and the scrim that beds it.
 */
@Composable
fun <T : Any> TvImmersiveBackdrop(
    item: T?,
    artwork: @Composable (T) -> Unit,
    copy: @Composable ColumnScope.(T) -> Unit,
    modifier: Modifier = Modifier,
    copyTopInset: Dp = Dp.Hairline,
    copyStart: Dp = tvContentGutterStart(),
    layout: TvImmersiveCopyLayout = TvImmersiveCopyLayout.Wide,
) {
    Crossfade(
        targetState = item,
        modifier = modifier.fillMaxSize(),
        animationSpec = if (LocalReduceMotion.current) snap() else tween(TV_IMMERSIVE_CROSSFADE_MILLIS),
        label = "tv-immersive-backdrop",
    ) { focused ->
        if (focused != null) {
            val paneShift = LocalTvPaneShift.current
            Box(modifier = Modifier.fillMaxSize()) {
                // Cancel the pane's expansion shift so the artwork holds still while rows and copy slide over it.
                Box(modifier = Modifier.fillMaxSize().graphicsLayer { translationX = -paneShift.value.toPx() }) {
                    artwork(focused)
                    TvImmersiveScrim(layout)
                }
                ReportTvRailArtwork()
                TvImmersiveCopyBand(copyTopInset, copyStart, layout.copyWidthFraction) { copy(focused) }
            }
        }
    }
}

/** The text band above the rows, bottom-anchored so the copy does not shift as a title wraps. */
@Composable
private fun BoxScope.TvImmersiveCopyBand(
    copyTopInset: Dp,
    start: Dp,
    widthFraction: Float,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .height(dimensionResource(TvR.dimen.tv_immersive_content_top))
                .fillMaxWidth(widthFraction)
                .padding(
                    start = start,
                    end = dimensionResource(DesR.dimen.padding_m),
                    top = dimensionResource(TvR.dimen.tv_overscan_vertical) + copyTopInset,
                    bottom = dimensionResource(DesR.dimen.padding_l),
                ).tvLayoutAnchor(TvLayoutAnchors.HUB_COPY),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s), Alignment.Bottom),
        content = content,
    )
}

/** Two gradients: the horizontal one beds the copy, the vertical one beds the rows below. */
@Composable
private fun TvImmersiveScrim(layout: TvImmersiveCopyLayout) {
    val background = MaterialTheme.colorScheme.background
    Box(
        modifier =
            Modifier.fillMaxSize().background(
                startHorizontalGradient(
                    0f to background.copy(alpha = SCRIM_START_ALPHA),
                    layout.scrimHoldFraction to background.copy(alpha = SCRIM_START_HOLD_ALPHA),
                    layout.scrimMidFraction to background.copy(alpha = SCRIM_START_MID_ALPHA),
                    layout.scrimTailFraction to background.copy(alpha = SCRIM_START_TAIL_ALPHA),
                    1f to background.copy(alpha = 0f),
                ),
            ),
    )
    Box(
        modifier =
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colorStops =
                        arrayOf(
                            SCRIM_BOTTOM_START_FRACTION to background.copy(alpha = 0f),
                            1f to background.copy(alpha = SCRIM_BOTTOM_ALPHA),
                        ),
                ),
            ),
    )
}
