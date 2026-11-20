package com.binge.designsystem.tv.template

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.binge.designsystem.tv.component.TvFilterCategory
import com.binge.designsystem.tv.component.TvFilterValue
import com.binge.designsystem.tv.focus.TvPaneHold
import com.binge.designsystem.tv.focus.rememberTvPaneSwap
import com.binge.designsystem.tv.focus.tvEntryFocusGroup

/**
 * The [TvFilterPane] with the state and the focus handling it needs, so a page lays it out and does nothing else for it: which
 * category is open, where focus was last in the pane (saved, so Back from a title returns there), and the swap that carries focus
 * between the category list and a category's values (see `TvPaneSwap`) with its hold node.
 *
 * Back leaves an open category in one press, onto its row in the list. A category that empties itself, a seeded company cleared,
 * leaves the list the way Back does, so focus goes to the list rather than to recovery. [entry] is the requester the pane's entry row
 * takes; the page routes entry to it. [onClearAll] runs after the header's Clear so the page can empty its filters; focus then goes
 * to the first category.
 *
 * @need lay out a filter pane that remembers its category and carries focus across its views
 * @group tv
 * @replaces a hand-built filter pane with its own open-category state and Back handling
 * @sample TvFilterPaneGenresSample
 */
@Composable
fun TvFilterPaneHost(
    categories: List<TvFilterCategory>,
    copy: TvFilterPaneCopy,
    filterCount: Int,
    entry: FocusRequester,
    onClearAll: () -> Unit,
    onToggle: (categoryKey: String, valueKey: String) -> Unit,
    onClearCategory: (categoryKey: String) -> Unit,
    modifier: Modifier = Modifier,
    ranges: Map<String, TvFilterRangeSpec> = emptyMap(),
    matchAllKey: String = TV_FILTER_KEY_MATCH_ALL,
    anyKey: String = TV_FILTER_KEY_ANY,
    pinnedRow: (option: TvFilterValue, selected: Int) -> TvFilterPinnedRowSpec? = { _, _ -> null },
    valueExtras: (categoryKey: String, value: TvFilterValue) -> TvFilterValueExtras? = { _, _ -> null },
    seed: TvFilterSeed = TvFilterSeed(),
    rangeSeed: TvFilterRangeSeed = TvFilterRangeSeed(),
) {
    var openKey by rememberSaveable { mutableStateOf<String?>(null) }
    var categoryKey by rememberSaveable { mutableStateOf<String?>(null) }
    var valueKey by rememberSaveable { mutableStateOf<String?>(null) }
    val open = categories.firstOrNull { it.key == openKey }
    // The pane swaps between the list and a category's values: the focused row goes with the old view, so focus is held
    // on a node that stays until the new view's entry row is placed, rather than falling to the bar first.
    val swap = rememberTvPaneSwap(entry)

    fun drill(key: String?) {
        swap.begin(key)
        if (key != null) categoryKey = key
        valueKey = null
        openKey = key
    }
    // A category that empties itself, a seeded company cleared, leaves the list: leave its view the way Back does, so focus
    // goes to the list rather than to recovery.
    LaunchedEffect(openKey, open) {
        if (openKey != null && open == null) drill(null)
    }
    BackHandler(enabled = open != null) { drill(null) }
    Box(modifier.tvEntryFocusGroup(entry)) {
        TvFilterPane(
            categories = categories,
            open = open,
            position = TvFilterPosition(categoryKey, valueKey),
            entry = entry,
            copy = copy,
            filterCount = filterCount,
            onClearAll = {
                onClearAll()
                swap.begin(null)
                categoryKey = null
            },
            onDrill = { drill(it) },
            onPosition = {
                categoryKey = it.category
                valueKey = it.value
            },
            onToggle = onToggle,
            onClearCategory = onClearCategory,
            ranges = ranges,
            matchAllKey = matchAllKey,
            anyKey = anyKey,
            pinnedRow = pinnedRow,
            valueExtras = valueExtras,
            seed = seed,
            rangeSeed = rangeSeed,
            onEntryPlaced = swap::noteEntryPlaced,
            entryLooksFocused = swap.swapping,
        )
        TvPaneHold(swap)
    }
}
