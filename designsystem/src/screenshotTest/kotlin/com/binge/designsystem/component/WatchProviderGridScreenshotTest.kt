package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Multi-column provider grid — column count/spacing reflows with width. */
class WatchProviderGridScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Ready() {
        ScreenshotTheme {
            WatchProviderGrid(
                providers = listOf(
                    WatchProviderUi(id = 1, name = "Netflix", logoUrl = ""),
                    WatchProviderUi(id = 2, name = "Disney+", logoUrl = ""),
                    WatchProviderUi(id = 3, name = "Prime Video", logoUrl = ""),
                    WatchProviderUi(id = 4, name = "Max", logoUrl = ""),
                    WatchProviderUi(id = 5, name = "Apple TV+", logoUrl = ""),
                    WatchProviderUi(id = 6, name = "Hulu", logoUrl = ""),
                ),
                selectedWatchProviderIds = setOf(1, 4),
                onToggle = {},
            )
        }
    }

    /** A short last row: the trailing spacers must keep its tiles the width of a full row's. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun PartialRow() {
        ScreenshotTheme {
            WatchProviderGridRow(
                providers = listOf(
                    WatchProviderUi(id = 1, name = "Netflix", logoUrl = ""),
                    WatchProviderUi(id = 2, name = "Disney+", logoUrl = ""),
                ),
                columns = 4,
                selectedWatchProviderIds = setOf(1),
                onToggle = {},
            )
        }
    }
}
