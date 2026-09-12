package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Removable filter chip — include and exclude tones. */
class DiscoverQuickClearChipScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Include() {
        ScreenshotTheme {
            DiscoverQuickClearChip(
                label = "Action",
                tone = QuickClearTone.Include,
                onClear = {},
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Exclude() {
        ScreenshotTheme {
            DiscoverQuickClearChip(
                label = "Horror",
                tone = QuickClearTone.Exclude,
                onClear = {},
            )
        }
    }
}
