package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.tv.component.TvFilterCategory
import com.binge.designsystem.tv.component.TvFilterValue
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.component.TvPosterGrid
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

/**
 * A page of filters beside a poster grid, the shape of a discover or browse screen on a television. The filters are a fixed pane on
 * the left, the categories each with its current choice, and OK drills into one to pick its values; the results are the poster grid
 * on the right, whose focused row pins to the top. Every pick applies at once, so the caller's results follow the filters as they change.
 *
 * Each side remembers where it was left, so Back from a title, ← and → between the sides and ↓ from the bar all return there. A failed
 * query is a message with Try again in the grid's place; results that are the earlier query's, because the new one failed, stay with a
 * line above them carrying Try again ([TvGridResults.Items.stale]), and focus stays where it was.
 *
 * The item type is the caller's: [cell] turns one into the poster it draws. [refreshing] is whether a refresh is in flight, so a
 * stale-results retry stays busy until it settles. [listKey] is the filter set, so new results arrive at the top of a fresh scroll.
 * [onRetry] is for a failed query, and [onRetryStale] for the stale line, which keeps the grid and its focus where they are.
 *
 * [onVisitEnded] reports the deepest cell focus reached, once as the page leaves. [loadingShell] draws the page's shell, the pane and the
 * grid as plates, for the moment there is no state yet: the visit is held above it, so a filter change that reloads the page is the
 * same visit and keeps its depth. What a cell does on OK is its own [TvPosterCell.onClick], which [cell] builds from the position and the whole item.
 *
 * The page draws no copy of its own: [paneCopy] and [gridCopy] carry every word. The rest of the parameters are [TvFilterPaneHost]'s.
 *
 * The results side and the range scale are internal, so a host's test cannot drive them directly. Their paths are covered here, by the
 * design system's own tests, which a host can rely on and need not copy: `TvFilterResultsRetryFocusTest` (Try again's hand-off of focus
 * to the first title, a failure retried again, and the wording by error kind), `TvFilterGridPageStaleResultsTest` (the stale line, its
 * retry and the claim of focus, with a reload through the shell), `TvFilterGridPageFocusTest` (arrival, each side's remembered position,
 * the visit depth), `TvFilterGridPageGeometryTest` (the shell lands where the page does) and, for the range pick rules,
 * `TvFilterRangeLogicTest` and `TvFilterPaneFocusTest`. A host's own test finds the page's first category and first cell by
 * [TV_FILTER_ANCHOR_FIRST_CATEGORY] and [TV_FILTER_ANCHOR_FIRST_CELL].
 *
 * @need lay out a tv filter pane beside a grid of results that follow it
 * @group tv
 * @replaces a hand-built tv discover page with its own pane, grid and focus hand-offs
 * @sample TvFilterGridPageResultsSample
 */
@Composable
fun <T : Any> TvFilterGridPage(
    categories: List<TvFilterCategory>,
    paneCopy: TvFilterPaneCopy,
    gridCopy: TvFilterGridCopy,
    filterCount: Int,
    results: TvGridResults<T>,
    cell: (index: Int, item: T) -> TvPosterCell,
    listKey: Any?,
    onClearAll: () -> Unit,
    onToggle: (categoryKey: String, valueKey: String) -> Unit,
    onClearCategory: (categoryKey: String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    refreshing: Boolean = false,
    onRetryStale: () -> Unit = onRetry,
    onVisitEnded: (deepestIndex: Int?) -> Unit = {},
    ranges: Map<String, TvFilterRangeSpec> = emptyMap(),
    matchAllKey: String = TV_FILTER_KEY_MATCH_ALL,
    anyKey: String = TV_FILTER_KEY_ANY,
    pinnedRow: (option: TvFilterValue, selected: Int) -> TvFilterPinnedRowSpec? = { _, _ -> null },
    valueExtras: (categoryKey: String, value: TvFilterValue) -> TvFilterValueExtras? = { _, _ -> null },
    seed: TvFilterSeed = TvFilterSeed(),
    rangeSeed: TvFilterRangeSeed = TvFilterRangeSeed(),
    loadingShell: Boolean = false,
) {
    val visitDepth = rememberTvVisitDepth(onVisitEnded)
    if (loadingShell) return TvFilterGridPageLoading(modifier)
    var cellIndex by rememberSaveable { mutableStateOf<Int?>(null) }
    var gridLast by rememberSaveable { mutableStateOf(false) }
    val paneEntry = remember { FocusRequester() }
    val gridEntry = remember { FocusRequester() }
    // The empty plate carries no node for the grid's entry requester, so the page routes entry there only while something can take it:
    // results, or the failed plate's Try again. A loading grid's plates do not carry it either.
    val gridTakesFocus = (results as? TvGridResults.Items)?.let { it.count > 0 } == true || results is TvGridResults.Error
    Row(
        modifier = modifier
            .fillMaxSize()
            .tvEntryFocusGroup(if (gridLast && cellIndex != null && gridTakesFocus) gridEntry else paneEntry)
            .padding(start = tvContentGutterStart(), top = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_filter_page_gap)),
    ) {
        TvFilterPaneHost(
            categories = categories,
            copy = paneCopy,
            filterCount = filterCount,
            entry = paneEntry,
            onClearAll = onClearAll,
            onToggle = onToggle,
            onClearCategory = onClearCategory,
            modifier = Modifier
                .width(dimensionResource(TvR.dimen.tv_filter_pane_width))
                .fillMaxHeight()
                .onFocusChanged { if (it.hasFocus) gridLast = false },
            ranges = ranges,
            matchAllKey = matchAllKey,
            anyKey = anyKey,
            pinnedRow = pinnedRow,
            valueExtras = valueExtras,
            seed = seed,
            rangeSeed = rangeSeed,
        )
        Box(
            Modifier
                .weight(1f)
                .onFocusChanged { if (it.hasFocus) gridLast = true }
                .tvEntryFocusGroup(gridEntry),
        ) {
            TvFilterResults(
                results = results,
                refreshing = refreshing,
                cell = cell,
                copy = gridCopy,
                listKey = listKey,
                entry = gridEntry,
                entryIndex = cellIndex ?: 0,
                onCellFocused = {
                    cellIndex = it
                    visitDepth.reach(it)
                },
                onRetry = {
                    cellIndex = null
                    onRetry()
                },
                onRetryStale = onRetryStale,
            )
        }
    }
}

/**
 * The page before there is a state to draw: the filter pane and the grid as plates, so the page does not jump when it resolves.
 *
 * @need show the filter page's shell while its filters and results have not arrived
 * @group tv
 * @sample TvFilterGridPageNoStateYetSample
 */
@Composable
fun TvFilterGridPageLoading(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(start = tvContentGutterStart(), top = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_filter_page_gap)),
    ) {
        TvFilterPaneSkeleton(Modifier.width(dimensionResource(TvR.dimen.tv_filter_pane_width)))
        TvPosterGrid(
            count = 0,
            posterAt = { null },
            loading = true,
            entry = remember { FocusRequester() },
            entryIndex = 0,
            listKey = null,
            onCellFocused = {},
            modifier = Modifier.weight(1f),
            cellModifier = ::tvFilterCellModifier,
        )
    }
}
