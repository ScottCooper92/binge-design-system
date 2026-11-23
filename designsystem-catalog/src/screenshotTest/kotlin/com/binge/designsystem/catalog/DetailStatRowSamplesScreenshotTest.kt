package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [DetailStatRow] catalog sample. */
class DetailStatRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun stats() {
        DetailStatRowSample()
    }
}
