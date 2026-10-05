package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme

/** Screenshot coverage for the detail page template: at rest, with a section anchored, and its action row. */
class TvDetailSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun page() = Frame { TvDetailPageSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun pageFocused() = Frame { TvDetailPageFocusedSample() }

    @PreviewTest
    @TvPreviews
    @Composable
    fun actionRow() = Frame { TvDetailActionRowSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
