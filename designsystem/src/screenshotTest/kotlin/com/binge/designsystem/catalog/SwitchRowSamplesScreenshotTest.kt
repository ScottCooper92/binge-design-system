package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

class SwitchRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        SwitchRowSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun WorstCase() {
        SwitchRowWorstCaseSample()
    }
}
