package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

class BingeMessageDialogSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Default() {
        BingeMessageDialogSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LongBodyScrolls() {
        BingeMessageDialogLongSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun WorstCase() {
        BingeMessageDialogWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun SnackbarResultFitsAtLargeFonts() {
        BingeSnackbarWorstCaseSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun ShownOnce() {
        BingeMessageDialogShownOnceSample()
    }
}
