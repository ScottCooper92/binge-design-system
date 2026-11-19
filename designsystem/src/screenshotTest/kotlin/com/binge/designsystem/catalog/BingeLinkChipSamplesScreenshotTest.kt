package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.WorstCaseFontPreviews

/** Screenshot coverage for the [BingeLinkChipSample] catalog sample: light and dark, and at 1.3x and 2x font. */
class BingeLinkChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @WorstCaseFontPreviews
    @Composable
    fun Links() {
        BingeLinkChipSample()
    }
}
