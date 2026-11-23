package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the trending-search-chip catalog sample — renders the shared
 * [TrendingSearchChipSample].
 */
class TrendingSearchChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Ranked() {
        TrendingSearchChipSample()
    }
}
