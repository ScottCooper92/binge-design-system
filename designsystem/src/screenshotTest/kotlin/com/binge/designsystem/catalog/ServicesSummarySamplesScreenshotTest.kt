package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class ServicesSummarySamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Selected() {
        ServicesSummarySelectedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Empty() {
        ServicesSummaryEmptySample()
    }
}
