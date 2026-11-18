package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the navigation motion sample: its resting state, since a frame cannot show motion. */
class BingeNavMotionSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun resting() {
        BingeNavMotionSample()
    }
}
