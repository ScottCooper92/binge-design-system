package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [BingeTextButton] catalog samples — renders the shared samples, the
 * component's one public fixture.
 */
class TextButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun States() {
        TextButtonSample()
    }
}
