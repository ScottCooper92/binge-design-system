package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeTabRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TabRow() {
        BingeTabRowSample()
    }
}
