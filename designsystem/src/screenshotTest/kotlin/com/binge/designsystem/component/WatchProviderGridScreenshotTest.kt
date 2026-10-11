package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Multi-column provider grid — column count/spacing reflows with width. */
class WatchProviderGridScreenshotTest {
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
