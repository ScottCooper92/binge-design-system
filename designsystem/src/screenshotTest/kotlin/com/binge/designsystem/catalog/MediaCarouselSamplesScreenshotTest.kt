package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the media-carousel catalog sample — renders the shared
 * [MediaCarouselSample].
 */
class MediaCarouselSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Carousel() {
        MediaCarouselSample()
    }
}
