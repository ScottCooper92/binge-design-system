package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.PreviewSystemBarInsets
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for [com.binge.designsystem.template.SearchScreen]: each state at the phone and expanded widths, and the
 * states that carry copy at 1.3x and 2x font as well.
 */
class SearchScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun idle() = SearchScreenIdleSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun idleEmpty() = SearchScreenIdleEmptySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun idleExtras() = SearchScreenIdleExtrasSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun loading() = SearchScreenLoadingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ready() = SearchScreenReadySample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun people() = SearchScreenPeopleSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun appending() = SearchScreenAppendingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun appendFailed() = SearchScreenAppendFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun noResults() = SearchScreenNoResultsSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun noResultsExtras() = SearchScreenNoResultsExtrasSample()

    /** [noResultsExtras] under system bars: the next step must clear the navigation bar, which a preview reports as 0dp without this. */
    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun noResultsExtrasUnderSystemBars() = PreviewSystemBarInsets { SearchScreenNoResultsExtrasSample() }

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun typeEmpty() = SearchScreenTypeEmptySample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun failed() = SearchScreenFailedSample()
}
