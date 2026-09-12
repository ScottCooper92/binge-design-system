package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [BingeTextButton] catalog samples — renders the shared samples, the
 * component's one public fixture (#745).
 */
class TextButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Enabled() {
        TextButtonSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        TextButtonDisabledSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Error() {
        TextButtonErrorSample()
    }
}
