package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

class BingeStopSliderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        BingeStopSliderSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Warning() {
        BingeStopSliderWarningSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Held() {
        BingeStopSliderHeldSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun WorstCase() {
        BingeStopSliderWorstCaseSample()
    }
}
