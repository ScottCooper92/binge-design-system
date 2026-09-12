package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeConfirmDialogSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        BingeConfirmDialogSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Destructive() {
        BingeConfirmDialogDestructiveSample()
    }
}
