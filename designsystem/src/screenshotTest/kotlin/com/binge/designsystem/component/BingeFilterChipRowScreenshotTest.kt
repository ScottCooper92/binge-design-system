package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

private val SCRIMMED_PAGER_HEIGHT = 240.dp

/** Scrollable filter-chip row — selected chip filled, the rest outlined, each with a count sub-pill. */
class BingeFilterChipRowScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun FilterChips() {
        ScreenshotTheme {
            BingeFilterChipRow(
                items = listOf(
                    FilterChipItem("All", 142),
                    FilterChipItem("Pending", 3),
                    FilterChipItem("Processing", 5),
                    FilterChipItem("Available", 28),
                    FilterChipItem("Failed"),
                ),
                selectedIndex = 1,
                onSelect = {},
            )
        }
    }

    /** [BingeFilterChipPager]'s scrim, spanning the chip row over paged content running under it. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun ScrimmedPager() {
        ScreenshotTheme {
            Box(modifier = Modifier.fillMaxWidth().height(SCRIMMED_PAGER_HEIGHT)) {
                BingeFilterChipPager(
                    items = listOf(
                        FilterChipItem("All", 142),
                        FilterChipItem("Pending", 3),
                        FilterChipItem("Processing", 5),
                    ),
                    selectedIndex = 0,
                    onSelectedIndexChange = {},
                    headerBackground = Color.Transparent,
                    scrimFraction = 1f,
                    modifier = Modifier.fillMaxSize(),
                ) { _, _ ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                    )
                }
            }
        }
    }
}
