package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * The expanding hub's rows and hero, a frame per state. A frame dispatches no keys, so focus is the parameter the stateless pieces
 * take and the strip is seeded to where ←/→ would have left it; the D-pad movement is covered by `TvExpandingRowsHubFocusTest`.
 */
class TvExpandingRowsSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun rowResting() = Frame { TvExpandingRowRestingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowFocused() = Frame { TvExpandingRowFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowActionFailed() = Frame { TvExpandingRowActionFailedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowHeldOpen() = Frame { TvExpandingRowHeldOpenSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowExtrasLoading() = Frame { TvExpandingRowExtrasLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowWithoutLogo() = Frame { TvExpandingRowWithoutLogoSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun shortRowAtLastTitle() = Frame { TvExpandingShortRowAtLastTitleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rowRtl() = Frame { TvExpandingRowRtlSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun slotFocused() = Frame { TvExpandingSlotFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubLoadingSkeleton() = Frame { TvExpandingHubLoadingSkeletonSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubLoading() = Frame { TvExpandingHubLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun heroPlateFocused() = Frame { TvExpandingHeroPlateFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pendingRow() = Frame { TvExpandingPendingRowSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun metaLineWidths() = Frame { TvExpandingMetaLineWidthsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun metaLineRated() = Frame { TvExpandingMetaLineRatedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun heroResting() = Frame { TvExpandingHeroRestingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun heroFocused() = Frame { TvExpandingHeroFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun heroSeries() = Frame { TvExpandingHeroSeriesSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun heroExtrasLoading() = Frame { TvExpandingHeroExtrasLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubWithHero() = Frame { TvExpandingHubWithHeroSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun hubRowsOnly() = Frame { TvExpandingHubRowsOnlySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun seeAllLastTitle() = Frame { TvExpandingSeeAllLastTitleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun seeAllFocused() = Frame { TvExpandingSeeAllFocusedSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize()) { content() } }
    }
}
