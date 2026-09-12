package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeFilterChipRow
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [BingeFilterChipRow] — the scrolling row of filter chips with one selected,
 * each carrying an optional count. Catalog under `"Chips"`; see [MediaCardRatedSample] for the
 * convention.
 */
@Composable
fun BingeFilterChipRowSample() {
    ScreenshotTheme {
        BingeFilterChipRow(
            items = listOf(
                FilterChipItem(label = "All", count = 142),
                FilterChipItem(label = "Movies", count = 88),
                FilterChipItem(label = "TV", count = 54),
                FilterChipItem(label = "Watchlist"),
            ),
            selectedIndex = 0,
            onSelect = {},
        )
    }
}
