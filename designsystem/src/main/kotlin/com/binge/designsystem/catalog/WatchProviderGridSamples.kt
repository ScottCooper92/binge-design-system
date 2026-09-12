package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.WatchProviderGrid
import com.binge.designsystem.component.WatchProviderGridSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

@Composable
fun WatchProviderGridSample() {
    ScreenshotTheme {
        WatchProviderGrid(
            providers = catalogSampleWatchProviders(),
            selectedWatchProviderIds = catalogSelectedWatchProviderIds(),
            onToggle = {},
        )
    }
}

@Composable
fun WatchProviderGridSkeletonSample() {
    ScreenshotTheme {
        WatchProviderGridSkeleton()
    }
}
