package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [ListRow] primitive catalog samples — renders the shared samples, the
 * primitive's one public fixture.
 */
class ListRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun poster() {
        ListRowPosterSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun header() {
        ListRowHeaderSample()
    }
}
