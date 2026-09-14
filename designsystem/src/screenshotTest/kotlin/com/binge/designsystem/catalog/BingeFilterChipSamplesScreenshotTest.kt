package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the filter-chip catalog samples — renders the shared
 * [BingeFilterChipFamilySample] and [BingeFilterChipDisabledSample], the component's public
 * fixtures (#745, #749).
 */
class BingeFilterChipSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Family() {
        BingeFilterChipFamilySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Disabled() {
        BingeFilterChipDisabledSample()
    }
}
