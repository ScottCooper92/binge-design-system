package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the media-type-tag catalog sample — renders the shared [MediaTypeTagSample]
 * (#745, #749).
 */
class MediaTypeTagSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Types() {
        MediaTypeTagSample()
    }
}
