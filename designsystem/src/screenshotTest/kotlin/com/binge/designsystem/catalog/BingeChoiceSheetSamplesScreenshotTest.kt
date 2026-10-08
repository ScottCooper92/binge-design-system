package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class BingeChoiceSheetSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun SingleChoice() {
        BingeChoiceSheetSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun SingleChoiceDocked() {
        BingeChoiceSheetDockedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MultiChoiceFiltered() {
        BingeMultiChoiceSheetSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun LoadingAndFailed() {
        BingeChoiceSheetStatesSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Rows() {
        BingeChoiceItemSample()
    }
}
