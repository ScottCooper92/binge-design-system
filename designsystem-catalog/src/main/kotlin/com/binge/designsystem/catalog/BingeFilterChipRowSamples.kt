@file:CatalogGroup("Chips")

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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

/** [BingeFilterChipRow] with several chips on at once: tap a chip to add it to the filter or drop it again. */
@Composable
fun BingeFilterChipRowMultiSample() {
    var selected by remember { mutableStateOf(setOf(0, 2)) }
    ScreenshotTheme {
        BingeFilterChipRow(
            items = listOf(
                FilterChipItem(label = "Action", count = 31),
                FilterChipItem(label = "Comedy", count = 24),
                FilterChipItem(label = "Drama", count = 52),
                FilterChipItem(label = "Horror"),
                FilterChipItem(label = "Sci-Fi", count = 9),
            ),
            selected = selected,
            onToggle = { selected = if (it in selected) selected - it else selected + it },
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
                FilterChipItem("Failed", contentDescription = "Failed requests"),
            ),
            selectedIndex = selected,
            onSelect = { selected = it },
        )
    }
}

/**
 * A row whose chips carry a leading icon, one without, and a count on the first — the icon sits before
 * the label and the row stays a single scrolling line.
 */
@Composable
fun BingeFilterChipRowIconsSample() {
    var selected by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        BingeFilterChipRow(
            items = listOf(
                FilterChipItem(label = "Movies", count = 88, icon = Icons.Filled.Movie),
                FilterChipItem(label = "Favourites", icon = Icons.Filled.Star),
                FilterChipItem(label = "Refine", icon = Icons.Filled.Tune),
                FilterChipItem(label = "Watchlist"),
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
            val items = listOf(
                FilterChipItem(label = "All", count = 142),
                FilterChipItem(label = "Movies", count = 88),
                FilterChipItem(label = "TV", count = 54),
            )
            val listStates = List(items.size) { rememberLazyListState() }
            BingeFilterChipPager(
                items = items,
                selectedIndex = selected,
                onSelectedIndexChange = { selected = it },
                // Each page's own list state answers whether it is scrolled, so the scrim is up under the chips exactly while rows pass beneath them.
                pageScrolled = { listStates[it].canScrollBackward },
                modifier = Modifier.fillMaxSize(),
            ) { contentPadding, page ->
                LazyColumn(
                    state = listStates[page],
                    contentPadding = contentPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    items(PAGER_SAMPLE_ROWS) { row ->
                        Text(
                            text = "${items[page].label} ${row + 1}",
                            modifier = Modifier.padding(
                                horizontal = dimensionResource(R.dimen.padding_m),
                                vertical = dimensionResource(R.dimen.padding_s),
                            ),
                        )
                    }
                }
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
fun BingeFilterChipPagerScrimmedSample() = ScrimmedPagerSample(scrimFraction = 1f)

/**
 * [BingeFilterChipPager]'s scrim half in: the bar's solid surface has not started yet, so the chip row still reads through to the
 * content and the band below it tapers as before.
 */
@Composable
fun BingeFilterChipPagerScrimmedHalfSample() = ScrimmedPagerSample(scrimFraction = 0.5f)

@Composable
private fun ScrimmedPagerSample(scrimFraction: Float) {
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
                scrimFraction = scrimFraction,
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

/** How many rows each page of the pager sample holds: enough to scroll, so a page can be scrolled and its scrim shown. */
private const val PAGER_SAMPLE_ROWS = 40
