package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Tri-state filter chip — neutral, include, and exclude. */
class IncludeExcludeChipScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Neutral() {
        ScreenshotTheme {
            IncludeExcludeChip(
                label = "Action",
                state = IncludeExcludeState.Neutral,
                onTap = {},
                onLongPress = {},
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Include() {
        ScreenshotTheme {
            IncludeExcludeChip(
                label = "Action",
                state = IncludeExcludeState.Include,
                onTap = {},
                onLongPress = {},
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Exclude() {
        ScreenshotTheme {
            IncludeExcludeChip(
                label = "Horror",
                state = IncludeExcludeState.Exclude,
                onTap = {},
                onLongPress = {},
            )
        }
    }
}
