package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/** Screenshot coverage for the [ImagePlaceholder][com.binge.designsystem.component.ImagePlaceholder] catalog sample. */
class ImagePlaceholderSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Placeholder() {
        ImagePlaceholderSample()
    }
}
