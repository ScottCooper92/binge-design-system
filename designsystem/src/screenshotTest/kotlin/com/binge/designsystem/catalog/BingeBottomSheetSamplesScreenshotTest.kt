package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeBottomSheetSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TextField() {
        BingeBottomSheetSample()
    }
}
