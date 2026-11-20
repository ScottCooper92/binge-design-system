package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.BingePullToRefresh
import com.binge.designsystem.component.CollapsedFilterAction
import com.binge.designsystem.component.DiscoverQuickClearChip
import com.binge.designsystem.component.FilterPill
import com.binge.designsystem.component.MediaCard
import com.binge.designsystem.component.MediaCardSkeleton
import com.binge.designsystem.component.MediaCardStatus
import com.binge.designsystem.component.QuickClearTone
import com.binge.designsystem.component.ScrollToTopOnTabReselect
import com.binge.designsystem.component.rememberPaneTopBarScrollBehavior
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.mediaGridColumns
import com.binge.designsystem.navOverlayPadding
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** How many cells from the end the grid asks for the next page. */
const val PAGED_GRID_LOAD_MORE_AHEAD: Int = 6

/** The lazy key of the append footer; an app's own keys are text too, so it is namespaced. */
private const val FOOTER_KEY = "paged-grid-footer"

/** How many rows of plates the loading skeleton draws. */
const val PAGED_GRID_SKELETON_ROWS: Int = 8

/** One poster in a [PagedGridScreen]. [key] is the app's identity for it, text so a lazy grid can save it. */
@Immutable
class GridItem(
    val key: String,
    val posterUrl: String?,
    val title: String,
    val rating: Float? = null,
    val status: MediaCardStatus? = null,
    val favourite: Boolean = false,
    val progress: Float? = null,
    val subtitle: String? = null,
)

/** One filter in force, shown as a chip the user can tap to remove. [key] is handed back to `onClearChip`. */
@Immutable
class GridFilterChip(
    val key: String,
    val label: String,
)

/**
 * The filter pill and the chips under it. [activeCount] is the app's own number (it may count a non-default sort
 * that has no chip), so it is never worked out from [chips]. No chips means no chip row.
 */
@Immutable
class GridFilterBar(
    val activeCount: Int,
    val pillLabel: String,
    val pillDescription: String,
    val chips: List<GridFilterChip> = emptyList(),
)

/** What a [PagedGridScreen] draws. */
@Immutable
sealed interface PagedGridState {
    /** The first page is on its way. */
    data object Loading : PagedGridState

    /** Posters. The footer shows while [appending] a page, or when the last page [appendFailed]. */
    class Ready(
        val items: List<GridItem>,
        val hasMore: Boolean,
        val appending: Boolean = false,
        val appendFailed: Boolean = false,
    ) : PagedGridState

    /** Nothing to list, said under its [icon] with an optional [action]. [title] null says "Nothing here yet". */
    class Empty(
        val message: String,
        val title: String? = null,
        val icon: ImageVector = Icons.Filled.SearchOff,
        val action: ScreenAction? = null,
    ) : PagedGridState

    /** The load failed. [title] and [message] default to [kind]'s own words; Try again shows only when [retryable]. */
    class Failed(
        val kind: ErrorKind,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
    ) : PagedGridState
}

/** What the screen reports. [onBack] null draws no back control; [onRefresh] null draws no pull-to-refresh. */
@Immutable
class PagedGridCallbacks(
    val onOpenItem: (key: String) -> Unit,
    val onLoadMore: () -> Unit,
    val onRetry: () -> Unit,
    val onClearChip: (key: String) -> Unit = {},
    val onOpenFilters: () -> Unit = {},
    val onBack: (() -> Unit)? = null,
    val onRefresh: (() -> Unit)? = null,
)

/** Whether the last visible cell is within [ahead] of the end. */
fun isNearEnd(
    lastVisibleIndex: Int,
    totalCount: Int,
    ahead: Int = PAGED_GRID_LOAD_MORE_AHEAD,
): Boolean = lastVisibleIndex >= totalCount - ahead

/** Whether the grid's footer (a spinner, or Retry) is drawn. */
fun showsGridFooter(ready: PagedGridState.Ready): Boolean = ready.appending || ready.appendFailed

/**
 * A paged grid of posters on the large collapsing bar: an optional filter pill that hands over to a bar action as the bar collapses,
 * the filters in force as quick-clear chips in the bar's header, then a skeleton, an empty or failed plate, or the grid, which asks for
 * more near its end and ends in a spinner or Retry. The bar and back control stay put across all four states.
 *
 * Nothing here is an app's type: items are [GridItem]s with a text key, the sheet behind the pill is the [filterSheet] slot, and
 * the app owns its paging, sorting and filter state. [filterBar] null shows neither the pill nor the bar action; pass it in every state,
 * loading and failed included, so the pill and chip row do not appear or vanish as the page changes. [titleTrailing]
 * sits after the pill and is told whether the title row is still interactive; [barActions] is told the same and how far the bar has
 * collapsed (both change at [BAR_HANDOVER_COLLAPSE]). [failedContent] replaces the default failure plate.
 *
 * @param refreshing whether a pull-to-refresh is in flight; used only when [PagedGridCallbacks.onRefresh] is set.
 * @param reselectKey the tab this grid is the root of, as the shell emits it on the reselect signal (compared by equality, so
 *   pass the same value); tapping it again scrolls to the top. Null for a grid with no tab.
 * @param loadingLabel what a screen reader says of the first loading plate; the app words it ("Loading titles").
 * @param firstVisibleIndex scrolls the grid to a cell, for a frame of the grid mid-list.
 * @param filterSheet the sheet the pill opens, given a dismiss callback; the screen keeps whether it is open.
 *
 * @need list posters in a paged grid with filters, load more, empty and failed states
 * @group screens
 * @replaces a hand-built scaffold with a pill, filter chips and a lazy poster grid
 */
@Composable
fun PagedGridScreen(
    title: String,
    state: PagedGridState,
    callbacks: PagedGridCallbacks,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    filterBar: GridFilterBar? = null,
    refreshing: Boolean = false,
    reselectKey: Any? = null,
    firstVisibleIndex: Int = 0,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    moreFailedLabel: String = stringResource(R.string.paged_grid_more_failed),
    loadingLabel: String = stringResource(R.string.paged_grid_loading),
    titleTrailing: (@Composable (pillInteractive: Boolean) -> Unit)? = null,
    barActions: @Composable RowScope.(handedOver: Boolean, collapsedFraction: Float) -> Unit = { _, _ -> },
    failedContent: (@Composable (Modifier) -> Unit)? = null,
    filterSheet: (@Composable (onDismiss: () -> Unit) -> Unit)? = null,
    overlays: @Composable () -> Unit = {},
) {
    val scrollBehavior = rememberPaneTopBarScrollBehavior()
    var filtersOpen by rememberSaveable { mutableStateOf(false) }
    val collapsed = scrollBehavior.state.collapsedFraction
    val pillInteractive = collapsed < BAR_HANDOVER_COLLAPSE
    val openFilters = {
        filtersOpen = true
        callbacks.onOpenFilters()
    }
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = callbacks.onBack,
        bar = ScreenBar.Large,
        snackbarHostState = snackbarHostState,
        scrollBehavior = scrollBehavior,
        subtitle = subtitle,
        titleTrailing =
            if (filterBar != null || titleTrailing != null) {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                    ) {
                        if (filterBar != null) {
                            FilterPill(
                                activeCount = filterBar.activeCount,
                                enabled = pillInteractive,
                                onClick = openFilters,
                                label = filterBar.pillLabel,
                                contentDescription = filterBar.pillDescription,
                            )
                        }
                        titleTrailing?.invoke(pillInteractive)
                    }
                }
            } else {
                null
            },
        actions = {
            if (filterBar != null) {
                CollapsedFilterAction(
                    activeCount = filterBar.activeCount,
                    enabled = !pillInteractive,
                    collapsedFraction = collapsed,
                    contentDescription = filterBar.pillDescription,
                    onClick = openFilters,
                )
            }
            barActions(!pillInteractive, collapsed)
        },
        header = filterBar?.chips?.takeIf { it.isNotEmpty() }?.let { chips -> { ActiveFilterChips(chips, callbacks.onClearChip) } },
    ) { padding ->
        // The bar's height and the navigation bar go inside the grid's own scroll, so posters pass under the one and run down to the
        // window's edge under the other; only the sides stay outside it.
        ScreenBody(padding) { body ->
            val inner =
                navOverlayPadding(
                    PaddingValues(
                        start = dimensionResource(R.dimen.screen_content_inset),
                        top = body.calculateTopPadding() + dimensionResource(R.dimen.padding_s),
                        end = dimensionResource(R.dimen.screen_content_inset),
                        bottom = body.calculateBottomPadding() + dimensionResource(R.dimen.padding_m),
                    ),
                )
            when (state) {
                PagedGridState.Loading -> PagedGridSkeleton(inner, loadingLabel)
                is PagedGridState.Empty ->
                    EmptyScreen(
                        message = state.message,
                        modifier = Modifier.padding(inner),
                        title = state.title ?: stringResource(R.string.empty_screen_title),
                        icon = state.icon,
                        action = state.action,
                    )
                is PagedGridState.Failed ->
                    if (failedContent != null) {
                        failedContent(Modifier.padding(inner))
                    } else {
                        ErrorScreen(
                            kind = state.kind,
                            onRetry = callbacks.onRetry.takeIf { state.retryable },
                            modifier = Modifier.padding(inner),
                            title = state.title,
                            message = state.message,
                        )
                    }
                is PagedGridState.Ready ->
                    ReadyGrid(
                        state = state,
                        contentPadding = inner,
                        callbacks = callbacks,
                        refreshing = refreshing,
                        indicatorTopInset = body.calculateTopPadding(),
                        reselectKey = reselectKey,
                        firstVisibleIndex = firstVisibleIndex,
                        moreFailedLabel = moreFailedLabel,
                    )
            }
        }
    }
    if (filtersOpen && filterSheet != null) filterSheet { filtersOpen = false }
    overlays()
}

/** The filters in force, each removable, in one scrolling row under the bar. */
@Composable
private fun ActiveFilterChips(chips: List<GridFilterChip>, onClear: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.screen_content_inset)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        items(chips, key = { it.key }) { chip ->
            DiscoverQuickClearChip(label = chip.label, tone = QuickClearTone.Include, onClick = { onClear(chip.key) })
        }
    }
}

/** The grid's frame, shared by the loaded grid and its skeleton so the columns, gaps and padding are one thing. */
@Composable
private fun GridFrame(
    contentPadding: PaddingValues,
    state: LazyGridState = rememberLazyGridState(),
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(mediaGridColumns()),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.media_grid_gap)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.media_grid_gap)),
        content = content,
    )
}

private fun firstCellAnchor(index: Int): Modifier =
    Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))

/** How many plates the skeleton draws at the current width: the grid's columns times [PAGED_GRID_SKELETON_ROWS]. */
@Composable
fun pagedGridSkeletonCells(): Int = mediaGridColumns() * PAGED_GRID_SKELETON_ROWS

@Composable
private fun PagedGridSkeleton(
    contentPadding: PaddingValues,
    loading: String,
) {
    val cells = pagedGridSkeletonCells()
    GridFrame(contentPadding) {
        items(cells) { index ->
            MediaCardSkeleton(firstCellAnchor(index), description = if (index == 0) loading else null)
        }
    }
}

@Composable
private fun ReadyGrid(
    state: PagedGridState.Ready,
    contentPadding: PaddingValues,
    callbacks: PagedGridCallbacks,
    refreshing: Boolean,
    indicatorTopInset: Dp,
    reselectKey: Any?,
    firstVisibleIndex: Int,
    moreFailedLabel: String,
) {
    val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = firstVisibleIndex)
    if (reselectKey != null) ScrollToTopOnTabReselect(reselectKey, gridState)
    val nearEnd by remember {
        derivedStateOf {
            val info = gridState.layoutInfo
            isNearEnd(info.visibleItemsInfo.lastOrNull()?.index ?: 0, info.totalItemsCount)
        }
    }
    LaunchedEffect(gridState, state.hasMore, state.items.size) {
        if (state.hasMore) snapshotFlow { nearEnd }.distinctUntilChanged().filter { it }.collect { callbacks.onLoadMore() }
    }
    val grid: @Composable () -> Unit = {
        GridFrame(contentPadding, gridState) {
            itemsIndexed(state.items, key = { _, item -> item.key }) { index, item ->
                MediaCard(
                    posterUrl = item.posterUrl,
                    title = item.title,
                    rating = item.rating,
                    onClick = { callbacks.onOpenItem(item.key) },
                    modifier = firstCellAnchor(index),
                    status = item.status,
                    progress = item.progress,
                    subtitle = item.subtitle,
                    favourite = item.favourite,
                )
            }
            if (showsGridFooter(state)) {
                item(key = FOOTER_KEY, span = { GridItemSpan(maxLineSpan) }) {
                    AppendFooter(state.appendFailed, moreFailedLabel, callbacks.onLoadMore)
                }
            }
        }
    }
    val onRefresh = callbacks.onRefresh
    if (onRefresh != null) {
        BingePullToRefresh(isRefreshing = refreshing, onRefresh = onRefresh, indicatorTopInset = indicatorTopInset) { grid() }
    } else {
        grid()
    }
}

@Composable
private fun AppendFooter(
    failed: Boolean,
    failedLabel: String,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_m)), contentAlignment = Alignment.Center) {
        if (failed) {
            TextButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text(
                    failedLabel,
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_s)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            CircularProgressIndicator()
        }
    }
}
