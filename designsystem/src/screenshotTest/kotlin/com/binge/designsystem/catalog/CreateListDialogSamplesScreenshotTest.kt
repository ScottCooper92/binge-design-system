package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class CreateListDialogSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        CreateListDialogSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Submitting() {
        CreateListDialogSubmittingSample()
    }
}
