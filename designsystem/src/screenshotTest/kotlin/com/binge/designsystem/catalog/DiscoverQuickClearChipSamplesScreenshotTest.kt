package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the quick-clear-chip catalog sample — renders the shared
 * [DiscoverQuickClearChipSample] (both tones) (#745, #749).
 */
class DiscoverQuickClearChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Tones() {
        DiscoverQuickClearChipSample()
    }
}
