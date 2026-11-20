package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** The title detail page: at rest, expanded onto a tab, and while its title loads. A frame dispatches no keys, so the page takes its state as parameters. */
class TvTitleDetailPageSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun pageResting() = Frame { TvTitleDetailPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pageExpanded() = Frame { TvTitleDetailPageExpandedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pageTabFocused() = Frame { TvTitleDetailPageTabFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pageLoading() = Frame { TvTitleDetailPageLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun ratingBadgeSizes() = Frame { TvRatingBadgeSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun ratingBadgeOnAmberRows() = Frame { TvRatingBadgeOnAmberRowsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun metaLine() = Frame { TvMetaLineSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun skeletonLines() = Frame { TvSkeletonLineSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun overview() = Frame { TvDetailOverviewSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun castUnfocused() = Frame { TvDetailCastSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun castFocusedSecond() = Frame { TvDetailCastFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun castSeeAllFocused() = Frame { TvDetailCastSeeAllFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun castSeeAllOnly() = Frame { TvDetailCastSeeAllOnlySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun wideCarouselFocused() = Frame { TvDetailSlotCarouselSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun galleryGridFocused() = Frame { TvDetailGalleryGridSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pageExpandedOnCast() = Frame { TvTitleDetailPageTabsSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize()) { content() } }
    }
}
