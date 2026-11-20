package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for [com.binge.designsystem.template.PersonDetailScreen]: each state at the phone and expanded widths, and the
 * states that carry copy at 1.3x and 2x font as well.
 */
class PersonDetailScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ready() = PersonDetailReadySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun loading() = PersonDetailLoadingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun failed() = PersonDetailFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun creditsLoading() = PersonDetailCreditsLoadingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun creditsFailed() = PersonDetailCreditsFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun plain() = PersonDetailPlainSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun creditsEmpty() = PersonDetailCreditsEmptySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun seeAll() = PersonDetailSeeAllSample()
}
