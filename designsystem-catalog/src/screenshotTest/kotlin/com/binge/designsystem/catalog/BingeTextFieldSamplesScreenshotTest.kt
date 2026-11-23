package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the [BingeTextField] catalog samples: light and dark, and the dense ones at 1.3x and 2x font. */
class BingeTextFieldSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun States() {
        BingeTextFieldSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Decorated() {
        BingeTextFieldDecoratedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Secret() {
        BingeTextFieldSecretSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Revealed() {
        BingeTextFieldRevealedSample()
    }
}
