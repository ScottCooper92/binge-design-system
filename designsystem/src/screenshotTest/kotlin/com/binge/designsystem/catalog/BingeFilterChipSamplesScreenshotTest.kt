package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the filter-chip catalog sample — renders the shared
 * [BingeFilterChipFamilySample], the component's one public fixture (#745, #749).
 */
class BingeFilterChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Family() {
        BingeFilterChipFamilySample()
    }
}
