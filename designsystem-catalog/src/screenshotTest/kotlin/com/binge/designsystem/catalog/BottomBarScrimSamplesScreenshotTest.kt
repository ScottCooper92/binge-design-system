package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the bottom scrim: rows fading out above a floating element, light and dark. */
class BottomBarScrimSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Scrim() {
        BottomBarScrimSample()
    }
}
