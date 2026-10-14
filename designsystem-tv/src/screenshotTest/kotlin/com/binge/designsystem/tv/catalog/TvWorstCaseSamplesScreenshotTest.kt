package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvScreenshotTheme
import com.binge.designsystem.tv.preview.TvWorstCaseFontPreviews

/**
 * The shared TV rows with every optional line filled and long text, at font scales 1.3 and 2.0. The same fixtures run
 * through the no-clipping assertion in `TvWorstCaseFixturesTest`; these frames show them.
 */
class TvWorstCaseSamplesScreenshotTest {
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun optionRow() = Frame { TvOptionRowWorstCaseSample() }

    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun checkboxRow() = Frame { TvCheckboxRowWorstCaseSample() }

    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun choiceRow() = Frame { TvChoiceRowWorstCaseSample() }

    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun detailActionRow() = Frame { TvDetailActionRowWorstCaseSample() }

    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun listPaneBoard() = Frame { TvListPaneBoardWorstCaseSample() }

    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun hubFailedRow() = Frame { TvHubFailedRowWorstCaseSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
