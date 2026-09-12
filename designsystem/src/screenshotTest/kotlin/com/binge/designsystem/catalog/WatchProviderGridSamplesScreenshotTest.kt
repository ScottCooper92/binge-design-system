package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class WatchProviderGridSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WatchProviderGrid() {
        WatchProviderGridSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Skeleton() {
        WatchProviderGridSkeletonSample()
    }
}
