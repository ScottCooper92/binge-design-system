package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [BingeBarIconAction][com.binge.designsystem.component.BingeBarIconAction] sample. */
class BingeBarIconActionSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Actions() {
        BingeBarIconActionSample()
    }
}
