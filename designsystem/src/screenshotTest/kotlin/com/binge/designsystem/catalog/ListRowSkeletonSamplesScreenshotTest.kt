package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenPreviews

/**
 * Screenshot coverage for the [ListRowSkeletonColumn] catalog samples — renders the shared
 * [ListRowSkeletonSample] across the device-class matrix so the centred reading-column width caps
 * are exercised alongside the shimmer, plus the [ListRowSkeletonCompactSample] height override at a
 * single spot-check cell.
 */
class ListRowSkeletonSamplesScreenshotTest {
    @PreviewTest
    @ScreenPreviews
    @Composable
    fun Loading() {
        ListRowSkeletonSample()
    }

    @PreviewTest
    @Preview(name = "compact")
    @Composable
    fun compactHeight() {
        ListRowSkeletonCompactSample()
    }
}
