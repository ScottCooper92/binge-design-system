package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeSnackbarHostSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithAction() {
        BingeSnackbarSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithDismiss() {
        BingeSnackbarDismissSample()
    }
}
