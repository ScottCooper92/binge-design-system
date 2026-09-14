package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the rating-chip catalog samples — renders the shared [RatingChipToneSample]
 * and [RatingChipSizeSample].
 */
class RatingChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tones() {
        RatingChipToneSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Sizes() {
        RatingChipSizeSample()
    }
}
