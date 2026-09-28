package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class HintCardSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Standard() {
        HintCardSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Info() {
        HintCardInfoSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Dismissible() {
        HintCardDismissibleSample()
    }
}
