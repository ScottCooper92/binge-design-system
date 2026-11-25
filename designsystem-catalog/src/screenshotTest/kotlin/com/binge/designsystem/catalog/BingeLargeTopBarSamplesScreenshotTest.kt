package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeLargeTopBarSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Opaque() {
        BingeLargeTopBarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Defaults() {
        BingeLargeTopBarDefaultsSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun DefaultsCollapsed() {
        BingeLargeTopBarDefaultsCollapsedSample()
    }
}
