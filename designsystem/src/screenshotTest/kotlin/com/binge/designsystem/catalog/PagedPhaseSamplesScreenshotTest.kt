package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenStatePreview

/**
 * Every arm of the paged-list template: the four phases `PagedPhaseContent` chooses between and the two states of the
 * append footer. A state of one layout each, so the single phone cell.
 */
class PagedPhaseSamplesScreenshotTest {
    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun skeleton() = PagedPhaseSkeletonSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun empty() = PagedPhaseEmptySample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun failed() = PagedPhaseFailedSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun rows() = PagedPhaseRowsSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun appendFooterLoading() = PagedAppendFooterLoadingSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun appendFooterRetry() = PagedAppendFooterRetrySample()
}
