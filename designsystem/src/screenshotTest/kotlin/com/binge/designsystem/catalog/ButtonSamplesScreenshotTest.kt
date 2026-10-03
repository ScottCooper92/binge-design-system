package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the button catalog samples: one frame per button, each a labelled list of
 * its states, so the frame is the whole state matrix of that button.
 */
class ButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Filled() {
        FilledButtonSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Outlined() {
        OutlinedButtonSample()
    }
}
