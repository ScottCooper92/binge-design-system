package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilterChipPager
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

/**
 * Public sample for [BingeFilterChipPager] — [BingeFilterChipRow] wired to a swipeable page per
 * filter, the chips overlaid on the paged content. Catalog under `"Chips"`; see
 * [MediaCardRatedSample] for the convention.
 *
 * Bounded to [R.dimen.catalog_filter_chip_pager_height] because the pager is otherwise
 * `fillMaxSize`; the page content is a plain filled box standing in for a screen.
 */
@Composable
fun BingeFilterChipPagerSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.catalog_filter_chip_pager_height)),
        ) {
            BingeFilterChipPager(
                items = listOf(
                    FilterChipItem(label = "All", count = 142),
                    FilterChipItem(label = "Movies", count = 88),
                    FilterChipItem(label = "TV", count = 54),
                ),
                selectedIndex = 0,
                onSelectedIndexChange = {},
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
