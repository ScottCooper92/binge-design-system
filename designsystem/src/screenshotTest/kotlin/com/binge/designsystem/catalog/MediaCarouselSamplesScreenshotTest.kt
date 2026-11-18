package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/**
 * Screenshot coverage for the media-carousel catalog sample — renders the shared
 * [MediaCarouselSample], and a row of captioned cards at the worst-case font scales.
 */
class MediaCarouselSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Carousel() {
        MediaCarouselSample()
    }

    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun PosterAlignedEndTile() {
        MediaCarouselPosterAlignedEndTileSample()
    }
}
