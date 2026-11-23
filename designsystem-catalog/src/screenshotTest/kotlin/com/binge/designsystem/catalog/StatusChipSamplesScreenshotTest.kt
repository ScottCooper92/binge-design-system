package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for [StatusChipSample] in every sentiment, and [StatusChipNoDotSample]. */
class StatusChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Sentiments() {
        StatusChipSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun NoDot() {
        StatusChipNoDotSample()
    }
}
