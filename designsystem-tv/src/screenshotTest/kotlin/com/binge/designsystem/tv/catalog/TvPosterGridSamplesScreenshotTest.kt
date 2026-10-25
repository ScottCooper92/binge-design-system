package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** The poster grid: resolved, a focused cell, the loading plates, and a focused stable plate. */
class TvPosterGridSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun resolved() = Frame { TvPosterGridSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun focusedCell() = Frame { TvPosterGridFocusedCellSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun loading() = Frame { TvPosterGridLoadingSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun focusedPlate() = Frame { TvPosterGridFocusedPlateSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) { content() }
    }
}
