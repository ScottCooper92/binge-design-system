package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeSheetHeaderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Full() {
        BingeSheetHeaderSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TitleOnly() {
        BingeSheetHeaderTitleOnlySample()
    }
}
