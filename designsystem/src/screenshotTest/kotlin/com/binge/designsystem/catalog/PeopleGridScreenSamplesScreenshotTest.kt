package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenWidthPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for [com.binge.designsystem.template.PeopleGridScreen]: each state at the phone and expanded widths, and the grids at 1.3x and 2x font. */
class PeopleGridScreenSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun castCrew() = PeopleGridCastCrewSample()

    @PreviewTest
    @ScreenWidthPreviews
    @WorstCaseFontPreviews
    @Composable
    fun crew() = PeopleGridCrewSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun loading() = PeopleGridLoadingSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun failed() = PeopleGridFailedSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun empty() = PeopleGridEmptySample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun single() = PeopleGridSingleSample()
}
