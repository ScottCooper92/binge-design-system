package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the filter-chip-row catalog samples — renders the shared
 * [BingeFilterChipRowSample] and [BingeFilterChipPagerSample], and their mid-selection and scrimmed variants.
 */
class BingeFilterChipRowSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Row() {
        BingeFilterChipRowSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Pager() {
        BingeFilterChipPagerSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FilterChips() {
        BingeFilterChipRowMidSelectionSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ScrimmedPager() {
        BingeFilterChipPagerScrimmedSample()
    }
}
