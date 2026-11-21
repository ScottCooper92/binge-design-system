package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** The two-pane search page in each state and with each focusable region lit. Focus is seeded, since a frame runs no coroutines. */
class TvSearchPageSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun idle() = Frame { TvSearchPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun fieldFocused() = Frame { TvSearchPageFieldFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun suggestionFocused() = Frame { TvSearchPageSuggestionFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun cellFocused() = Frame { TvSearchPageCellFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun results() = Frame { TvSearchPageResultsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun resultFocusedInSecondRow() = Frame { TvSearchPageResultFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun loadingIdle() = Frame { TvSearchPageLoadingIdleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun loadingResults() = Frame { TvSearchPageLoadingResultsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun empty() = Frame { TvSearchPageEmptySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun failedWithRetryFocused() = Frame { TvSearchPageFailedSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(Modifier.fillMaxSize()) { content() }
    }
}
