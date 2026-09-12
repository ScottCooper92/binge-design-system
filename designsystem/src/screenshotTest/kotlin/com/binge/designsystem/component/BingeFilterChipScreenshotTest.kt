package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Filter chip — selected, unselected, and with a leading icon. */
class BingeFilterChipScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Selected() {
        ScreenshotTheme {
            BingeFilterChip(label = "All", selected = true, onClick = {})
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Unselected() {
        ScreenshotTheme {
            BingeFilterChip(
                label = "Trending",
                selected = false,
                onClick = {},
                leadingIcon = Icons.Filled.Star,
            )
        }
    }
}
