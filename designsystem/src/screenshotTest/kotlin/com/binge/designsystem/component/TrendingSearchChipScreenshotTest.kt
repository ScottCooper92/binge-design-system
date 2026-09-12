package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/** Suggestion pill, both leading marks — colour axis only. */
class TrendingSearchChipScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Chip() {
        ScreenshotTheme {
            TrendingSearchChip(rank = 1, label = "Severance", onClick = {})
        }
    }

    /** The unranked form: a past search of the user's, marked with the history glyph instead. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun HistoryChip() {
        ScreenshotTheme {
            TrendingSearchChip(label = "dune", leadingIcon = Icons.Filled.History, onClick = {})
        }
    }
}
