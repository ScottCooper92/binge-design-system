package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the season dropdown, the season picker's rows and the episodes section. */
class SeasonPickerSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun dropdown() = SeasonDropdownSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun dropdownLong() = SeasonDropdownLongLabelSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun picker() = SeasonPickerContentSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun episodes() = DetailEpisodesSectionSample()

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun episodesLoading() = DetailEpisodesSectionLoadingSample()
}
