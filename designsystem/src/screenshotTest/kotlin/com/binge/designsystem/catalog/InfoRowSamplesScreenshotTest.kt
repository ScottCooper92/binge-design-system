package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [InfoRow] catalog samples (#751). */
class InfoRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun single() {
        InfoRowSingleSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun list() {
        InfoRowListSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun linked() {
        InfoRowLinkedSample()
    }
}
