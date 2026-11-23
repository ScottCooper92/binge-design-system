package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

/**
 * Screenshot coverage for the filter-chip-row-skeleton catalog sample — renders the shared
 * [FilterChipRowSkeletonSample].
 */
class FilterChipRowSkeletonSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Skeleton() {
        FilterChipRowSkeletonSample()
    }
}
