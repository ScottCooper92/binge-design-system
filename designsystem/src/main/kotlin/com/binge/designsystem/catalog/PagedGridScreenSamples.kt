@file:OnePerScreen(fullScreen = true)
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.BingeMessageDialog
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.GridFilterBar
import com.binge.designsystem.template.GridFilterChip
import com.binge.designsystem.template.GridItem
import com.binge.designsystem.template.PagedGridCallbacks
import com.binge.designsystem.template.PagedGridScreen
import com.binge.designsystem.template.PagedGridState
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.theme.BingeSentiment
import kotlinx.coroutines.delay

private const val SAMPLE_ITEMS = 18
private const val PAGE_ITEMS = 12

/** Past the last cell, so the grid clamps to its end and a frame that starts there shows the footer. */
private const val SCROLLED_TO_END = 100
private const val LOAD_MILLIS = 900L

private val SampleTitles =
    listOf("Arrival", "Past Lives", "The Dark Knight", "Heat", "Paddington 2", "Drive", "Aliens", "Her", "Tár")

private fun sampleItems(count: Int = SAMPLE_ITEMS, from: Int = 0): List<GridItem> =
    List(count) { i ->
        val n = from + i
        GridItem(
            key = "movie-$n",
            posterUrl = null,
            title = SampleTitles[n % SampleTitles.size],
            rating = if (n % 4 == 3) null else 6f + (n % 4),
            status = if (n % 3 == 0) MediaCardStatus("Available", BingeSentiment.Positive) else null,
            favourite = n % 5 == 1,
            progress = if (n % 6 == 2) 0.4f else null,
            subtitle = if (n % 2 == 0) (1990 + n).toString() else null,
        )
    }

private val SampleChips = listOf(GridFilterChip("genre", "Drama"), GridFilterChip("year", "2020s"))

private fun sampleBar(chips: List<GridFilterChip> = SampleChips) =
    GridFilterBar(
        activeCount = chips.size,
        pillLabel = "Filters",
        pillDescription = "Filters, ${chips.size} applied",
        chips = chips,
    )

@Composable
private fun SampleGrid(
    state: PagedGridState,
    modifier: Modifier = Modifier,
    withFilters: Boolean = true,
    onBack: (() -> Unit)? = LocalDemoBack.current,
    firstVisibleIndex: Int = 0,
) {
    var chips by remember { mutableStateOf(SampleChips) }
    var info by remember { mutableStateOf<String?>(null) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        PagedGridScreen(
            title = "Discover",
            subtitle = "Popular movies, by rating",
            state = state,
            modifier = modifier,
            filterBar = if (withFilters) sampleBar(chips) else null,
            firstVisibleIndex = firstVisibleIndex,
            callbacks =
                PagedGridCallbacks(
                    onOpenItem = { info = it },
                    onLoadMore = {},
                    onRetry = {},
                    onClearChip = { key -> chips = chips.filterNot { it.key == key } },
                    onBack = onBack,
                ),
            filterSheet = { dismiss ->
                BingeMessageDialog(title = "Filters", message = "The app's own filter sheet goes here.", onDismiss = dismiss)
            },
        )
        info?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { info = null }) }
    }
}

/** Posters in a grid under the large bar: the filter pill with two filters in force, each a chip that clears when tapped. */
@Composable
fun PagedGridReadySample() = SampleGrid(PagedGridState.Ready(sampleItems(), hasMore = true))

/** The first page on its way: plates at the grid's column count, with the bar, pill and chips already in place. */
@Composable
fun PagedGridLoadingSample() = SampleGrid(PagedGridState.Loading)

/** The next page loading: the list scrolled to its end, with a spinner under the last row. */
@Composable
fun PagedGridAppendingSample() =
    SampleGrid(PagedGridState.Ready(sampleItems(), hasMore = true, appending = true), firstVisibleIndex = SCROLLED_TO_END)

/** The next page failed: the list scrolled to its end, where the footer is a Retry button. */
@Composable
fun PagedGridAppendFailedSample() =
    SampleGrid(PagedGridState.Ready(sampleItems(), hasMore = true, appendFailed = true), firstVisibleIndex = SCROLLED_TO_END)

/** Nothing matched the filters: an empty plate with its own icon and an action. */
@Composable
fun PagedGridEmptySample() =
    SampleGrid(
        PagedGridState.Empty(
            title = "No matches",
            message = "Nothing fits these filters.",
            icon = Icons.Filled.FilterAltOff,
            action = ScreenAction("Clear filters", onClick = {}),
        ),
    )

/** The load failed: the default failure plate with Try again, under the same bar. */
@Composable
fun PagedGridFailedSample() = SampleGrid(PagedGridState.Failed(ErrorKind.Network))

/** A grid with no filters and no back control, as a tab root: no pill, no bar action, and no chip row. */
@Composable
fun PagedGridPlainSample() = SampleGrid(PagedGridState.Ready(sampleItems(), hasMore = false), withFilters = false, onBack = null)

/**
 * The grid live: it pages in more as it nears the end, pulls to refresh, and clears a chip. The pill opens a stand-in sheet. Posters open
 * a message.
 */
@Composable
fun PagedGridDemo() {
    var items by remember { mutableStateOf(sampleItems(PAGE_ITEMS)) }
    var appending by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var chips by remember { mutableStateOf(SampleChips) }
    var opened by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(appending) {
        if (appending) {
            delay(LOAD_MILLIS)
            items = items + sampleItems(PAGE_ITEMS, items.size)
            appending = false
        }
    }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(LOAD_MILLIS)
            items = sampleItems(PAGE_ITEMS)
            refreshing = false
        }
    }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        PagedGridScreen(
            title = "Discover",
            subtitle = "Popular movies, by rating",
            state = PagedGridState.Ready(items, hasMore = items.size < PAGE_ITEMS * 4, appending = appending),
            filterBar = sampleBar(chips),
            refreshing = refreshing,
            callbacks =
                PagedGridCallbacks(
                    onOpenItem = { opened = it },
                    onLoadMore = { appending = true },
                    onRetry = {},
                    onClearChip = { key -> chips = chips.filterNot { it.key == key } },
                    onBack = LocalDemoBack.current,
                    onRefresh = { refreshing = true },
                ),
            filterSheet = { dismiss ->
                BingeMessageDialog(title = "Filters", message = "The app's own filter sheet goes here.", onDismiss = dismiss)
            },
        )
        opened?.let { BingeMessageDialog(title = "Opened", message = it, onDismiss = { opened = null }) }
    }
}
