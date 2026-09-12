package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [TextEntrySurface] catalog samples — renders the shared samples, the
 * component's one public fixture (#745).
 */
class TextEntrySurfaceSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        TextEntrySurfaceSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Error() {
        TextEntrySurfaceErrorSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Counter() {
        TextEntrySurfaceCounterSample()
    }
}
