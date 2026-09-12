package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Standard() {
        BingeTopBarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Transparent() {
        BingeTopBarTransparentSample()
    }
}
