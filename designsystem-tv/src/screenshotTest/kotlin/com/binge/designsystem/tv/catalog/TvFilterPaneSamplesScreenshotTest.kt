package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * The filter pane's views: the category list with and without filters, a multi-select's values (none chosen, chosen and ruled out,
 * each pinned row and a value focused), the app-worded services row in its three states, rows with a flag or an own-language name, a
 * single-select, the range view (empty, set, each tab and a step focused) and the pane's loading plates. Focus is a parameter, so a
 * static frame can show it.
 */
class TvFilterPaneSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun nothingSet() = Frame { TvFilterPaneNothingSetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun filtersSet() = Frame { TvFilterPaneFiltersSetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun headerFocused() = Frame { TvFilterPaneHeaderFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun categoryFocused() = Frame { TvFilterPaneCategoryFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun genres() = Frame { TvFilterPaneGenresSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun genresChosen() = Frame { TvFilterPaneGenresChosenSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun genresHeaderFocused() = Frame { TvFilterPaneGenresHeaderFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun genresCountFocused() = Frame { TvFilterPaneGenresCountFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun genresSwitchFocused() = Frame { TvFilterPaneGenresSwitchFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun genresValueFocused() = Frame { TvFilterPaneGenresValueFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun servicesAdd() = Frame { TvFilterPaneServicesAddSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun servicesUseSaved() = Frame { TvFilterPaneServicesUseSavedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun servicesUseSavedFocused() = Frame { TvFilterPaneServicesUseSavedFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun servicesSelected() = Frame { TvFilterPaneServicesSelectedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun language() = Frame { TvFilterPaneLanguageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun country() = Frame { TvFilterPaneCountrySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun singleSelect() = Frame { TvFilterPaneSingleSelectSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeNothingSet() = Frame { TvFilterPaneRangeNothingSetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeSet() = Frame { TvFilterPaneRangeSetSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeSettingMaximum() = Frame { TvFilterPaneRangeSettingMaximumSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeHeaderFocused() = Frame { TvFilterPaneRangeHeaderFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeMinimumFocused() = Frame { TvFilterPaneRangeMinimumFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeMaximumFocused() = Frame { TvFilterPaneRangeMaximumFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeStepFocused() = Frame { TvFilterPaneRangeStepFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rangeCustomScale() = Frame { TvFilterPaneRangeCustomScaleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun skeleton() = Frame { TvFilterPaneSkeletonSample() }

    /** No outer inset, so the pane's own gutter and overscan protect its content. */
    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) { content() }
        }
    }
}
