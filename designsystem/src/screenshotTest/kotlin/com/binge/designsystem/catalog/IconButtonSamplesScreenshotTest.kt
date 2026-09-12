package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the [ExpressiveIconButton] catalog samples — renders the shared samples,
 * the component's one public fixture (#745).
 */
class IconButtonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tones() {
        IconButtonToneSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        IconButtonDisabledSample()
    }
}
