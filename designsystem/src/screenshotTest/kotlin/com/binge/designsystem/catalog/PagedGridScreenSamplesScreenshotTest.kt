package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for [com.binge.designsystem.template.PagedGridScreen]: each state at the phone and expanded widths, and the
 * states that carry copy at 1.3x and 2x font as well.
 */
class PagedGridScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ready() = PagedGridReadySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun loading() = PagedGridLoadingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun appending() = PagedGridAppendingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun appendFailed() = PagedGridAppendFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun empty() = PagedGridEmptySample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun failed() = PagedGridFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun plain() = PagedGridPlainSample()
}
