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

/**
 * A region-sized selection — more providers than the row has slots for, so it caps the logos and
 * folds the rest into a trailing "+N" badge instead of overflowing the row.
 */
@Composable
fun ServicesSummaryOverflowSample() {
    ScreenshotTheme {
        ServicesSummary(
            selectedProviders = listOf(
                WatchProviderUi(id = 8, name = "Netflix", logoUrl = ""),
                WatchProviderUi(id = 9, name = "Prime Video", logoUrl = ""),
                WatchProviderUi(id = 337, name = "Disney+", logoUrl = ""),
                WatchProviderUi(id = 15, name = "Hulu", logoUrl = ""),
                WatchProviderUi(id = 384, name = "Max", logoUrl = ""),
                WatchProviderUi(id = 350, name = "Apple TV+", logoUrl = ""),
                WatchProviderUi(id = 531, name = "Paramount+", logoUrl = ""),
                WatchProviderUi(id = 386, name = "Peacock", logoUrl = ""),
                WatchProviderUi(id = 2, name = "Crunchyroll", logoUrl = ""),
            ),
            leadText = "Your services",
            emptyText = "Pick the streaming services you subscribe to.",
        )
    }
}
