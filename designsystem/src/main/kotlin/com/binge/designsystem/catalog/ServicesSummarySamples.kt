package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.ServicesSummary
import com.binge.designsystem.component.WatchProviderUi
import com.binge.designsystem.preview.ScreenshotTheme

/** Streaming-services summary with a few providers picked — lead text plus a row of logo tiles. */
@Composable
fun ServicesSummarySelectedSample() {
    ScreenshotTheme {
        ServicesSummary(
            selectedProviders = listOf(
                WatchProviderUi(id = 8, name = "Netflix", logoUrl = ""),
                WatchProviderUi(id = 9, name = "Prime Video", logoUrl = ""),
                WatchProviderUi(id = 337, name = "Disney+", logoUrl = ""),
            ),
            leadText = "Your services",
            emptyText = "Pick the streaming services you subscribe to.",
        )
    }
}

/** Empty state — nothing picked yet, so the hint shows at the component's fixed minimum height. */
@Composable
fun ServicesSummaryEmptySample() {
    ScreenshotTheme {
        ServicesSummary(
            selectedProviders = emptyList(),
            leadText = "Your services",
            emptyText = "Pick the streaming services you subscribe to.",
        )
    }
}
