package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for [com.binge.designsystem.template.CollectionDetailScreen]: each state at the phone and expanded widths, and the
 * states that carry copy at 1.3x and 2x font as well.
 */
class CollectionDetailScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ready() = CollectionDetailReadySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun loading() = CollectionDetailLoadingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun failed() = CollectionDetailFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun empty() = CollectionDetailEmptySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun posterOnly() = CollectionDetailPosterOnlySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun plain() = CollectionDetailPlainSample()
}
