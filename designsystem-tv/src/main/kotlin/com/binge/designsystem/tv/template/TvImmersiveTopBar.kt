package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max
import com.binge.designsystem.tv.nav.LocalTvTopInset
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Whether a host draws a top bar over the page, which is what moves the immersive geometry off its defaults. */
@Composable
private fun underTopBar(): Boolean = LocalTvTopInset.current > Dp.Hairline

/**
 * Where the immersive rows start, and how tall the copy band above them is: the hub, the band and the grid read this one
 * value so they stay in step. Under a top bar it moves down by the bar's height less [TvR.dimen.tv_immersive_top_bar_overlap],
 * and never up, so a short bar leaves it where it sits with none; with none it is [TvR.dimen.tv_immersive_content_top].
 */
@Composable
internal fun tvImmersiveContentTop(): Dp {
    val base = dimensionResource(TvR.dimen.tv_immersive_content_top)
    val overlap = dimensionResource(TvR.dimen.tv_immersive_top_bar_overlap)
    return if (underTopBar()) base + max(Dp.Hairline, LocalTvTopInset.current - overlap) else base
}

/** The copy band's spacing: where its copy may start, the gap between its lines, and what it leaves above the rows. */
internal class TvImmersiveCopySpacing(
    val top: Dp,
    val lineGap: Dp,
    val bottom: Dp,
)

/**
 * The copy band's spacing. Under a top bar the copy starts just inside the bar's foot, never above the overscan, and packs
 * its lines tighter so a two-line synopsis still fits the shorter band; with none it is the band's spacing as it always was.
 */
@Composable
internal fun tvImmersiveCopySpacing(copyTopInset: Dp): TvImmersiveCopySpacing {
    val overscan = dimensionResource(TvR.dimen.tv_overscan_vertical)
    return if (underTopBar()) {
        TvImmersiveCopySpacing(
            top = max(overscan, LocalTvTopInset.current - dimensionResource(TvR.dimen.tv_immersive_top_bar_copy_overlap)) + copyTopInset,
            lineGap = dimensionResource(DesR.dimen.padding_xs),
            bottom = dimensionResource(DesR.dimen.padding_sm),
        )
    } else {
        TvImmersiveCopySpacing(
            top = overscan + copyTopInset,
            lineGap = dimensionResource(DesR.dimen.padding_s),
            bottom = dimensionResource(DesR.dimen.padding_l),
        )
    }
}
