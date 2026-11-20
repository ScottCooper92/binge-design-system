package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/**
 * The filter page: the pane beside results that are in, loading, empty, failed (generic and offline) or stale, a grid whose cells carry
 * marks, and the page before it has a state.
 */
class TvFilterGridPageSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun results() = Frame { TvFilterGridPageResultsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun noFilters() = Frame { TvFilterGridPageNoFiltersSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun loading() = Frame { TvFilterGridPageLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun empty() = Frame { TvFilterGridPageEmptySample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun failed() = Frame { TvFilterGridPageFailedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun offline() = Frame { TvFilterGridPageOfflineSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun stale() = Frame { TvFilterGridPageStaleSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun marks() = Frame { TvFilterGridPageMarksSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun noStateYet() = Frame { TvFilterGridPageNoStateYetSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) { content() }
        }
    }
}
