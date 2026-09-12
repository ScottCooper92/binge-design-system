package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the filter-chip-row catalog sample — renders the shared
 * [BingeFilterChipRowSample] (#745, #749).
 */
class BingeFilterChipRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Row() {
        BingeFilterChipRowSample()
    }
}
