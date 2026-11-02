package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

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

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Severe() {
        BingeConfirmDialogSevereSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun WorstCase() {
        BingeConfirmDialogWorstCaseSample()
    }

    @PreviewTest
    @WorstCaseFontPreviews
    @Composable
    fun SevereWorstCase() {
        BingeConfirmDialogSevereWorstCaseSample()
    }
}
