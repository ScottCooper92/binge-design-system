@file:CatalogGroup("Chips")

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    var selected by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        BingeFilterChipRow(
            items = listOf(
                FilterChipItem(label = "All", count = 142),
                FilterChipItem(label = "Movies", count = 88),
                FilterChipItem(label = "TV", count = 54),
                FilterChipItem(label = "Watchlist"),
            ),
            selectedIndex = selected,
            onSelect = { selected = it },
        )
    }
}

/**
 * A selection past the first chip, over a request-status set whose last chip carries no count — the
 * filled chip sitting between outlined ones, each with its count sub-pill.
 */
@Composable
fun BingeFilterChipRowMidSelectionSample() {
    var selected by remember { mutableIntStateOf(1) }
    ScreenshotTheme {
        BingeFilterChipRow(
            items = listOf(
                FilterChipItem("All", 142),
                FilterChipItem("Pending", 3),
                FilterChipItem("Processing", 5),
                FilterChipItem("Available", 28),
                FilterChipItem("Failed"),
            ),
            selectedIndex = selected,
            onSelect = { selected = it },
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
    var selected by remember { mutableIntStateOf(0) }
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
                selectedIndex = selected,
                onSelectedIndexChange = { selected = it },
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

/**
 * [BingeFilterChipPager]'s scrim fully in, spanning the chip row over paged content running under a
 * transparent header. Bounded to [R.dimen.catalog_filter_chip_pager_height] like
 * [BingeFilterChipPagerSample].
 */
@Composable
fun BingeFilterChipPagerScrimmedSample() {
    var selected by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.catalog_filter_chip_pager_height)),
        ) {
            BingeFilterChipPager(
                items = listOf(
                    FilterChipItem("All", 142),
                    FilterChipItem("Pending", 3),
                    FilterChipItem("Processing", 5),
                ),
                selectedIndex = selected,
                onSelectedIndexChange = { selected = it },
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
