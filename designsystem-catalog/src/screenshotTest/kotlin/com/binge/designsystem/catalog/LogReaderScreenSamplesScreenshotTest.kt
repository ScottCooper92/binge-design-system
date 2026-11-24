package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenStatePreview
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for [com.binge.designsystem.template.LogReaderScreen]: the rows at the phone and expanded widths, and each state
 * that carries copy at 1.3x and 2x font as well.
 */
class LogReaderScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ready() = LogReaderReadySample()

    @PreviewTest
    @ScreenStatePreview
    @WorstCaseFontPreviews
    @Composable
    fun loading() = LogReaderLoadingSample()

    @PreviewTest
    @ScreenStatePreview
    @WorstCaseFontPreviews
    @Composable
    fun empty() = LogReaderEmptySample()

    @PreviewTest
    @ScreenStatePreview
    @WorstCaseFontPreviews
    @Composable
    fun failed() = LogReaderFailedSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun appending() = LogReaderAppendingSample()

    @PreviewTest
    @ScreenStatePreview
    @Composable
    fun appendFailed() = LogReaderAppendFailedSample()
}
