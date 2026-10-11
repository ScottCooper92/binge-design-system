package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the rows that lead with a tinted icon plate. */
class TvRowIconSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun plate() = Frame { TvRowIconSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun rows() = Frame { TvRowIconRowsSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun sideSheet() = Frame { TvRowIconSideSheetSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
