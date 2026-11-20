package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import com.binge.designsystem.tv.component.TvFilterCategory
import com.binge.designsystem.tv.component.TvFilterValue

/**
 * A category whose values are a scale with a lower and an upper bound (a rating, a content rating) rather than a list: its [steps] in
 * order, the stored bounds [from] and [to] (null is "Any"), and [onChange] with the range after a pick, already clamped so a bound that
 * would cross the other has cleared it. [rank] orders bounds when the scale's keys carry a value of their own (a rating's number),
 * and [offScaleLabel] words a stored bound that is on no step.
 */
@Immutable
class TvFilterRangeSpec(
    val steps: List<TvRangeStep>,
    val from: String?,
    val to: String?,
    val onChange: (Pair<String?, String?>) -> Unit,
    val stepIcon: ImageVector? = null,
    val initialBound: TvRangeBound = TvRangeBound.Min,
    val offScaleLabel: (String) -> String = { it },
    val rank: ((String) -> Float)? = null,
)

/**
 * The filter pane: the category list, or the values of the [open] category, or its scale when [ranges] has one for it. Every pick
 * goes straight to [onToggle]; the page decides what a pick does to its query.
 *
 * Focus is the page's contract, and the pane gives it the parts: [entry] is the requester the incoming view's entry row takes,
 * [onEntryPlaced] says the view with that key (null for the list) has placed its entry row, and [entryLooksFocused] draws that row
 * focused while a swap holds focus elsewhere (see `TvPaneSwap`). [position] is the category and value focus was last on.
 *
 * Words come from [copy]. Two slots let an app add what the pane does not know: [pinnedRow] words one pinned option itself (the
 * host app's services row), and [valueExtras] adds a flag, a second line or a code to a value row. [filterCount] is the app's own
 * number of filters set, since it knows which categories always hold a choice.
 *
 * @need show a filter pane of categories, their values and range scales on a TV
 * @group tv
 * @replaces a hand-built left pane of filter categories
 * @sample TvFilterPaneNothingSetSample
 */
@Composable
fun TvFilterPane(
    categories: List<TvFilterCategory>,
    open: TvFilterCategory?,
    position: TvFilterPosition,
    entry: FocusRequester,
    copy: TvFilterPaneCopy,
    filterCount: Int,
    onClearAll: () -> Unit,
    onDrill: (String?) -> Unit,
    onPosition: (TvFilterPosition) -> Unit,
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
    onEntryPlaced: (view: String?) -> Unit = {},
    entryLooksFocused: Boolean = false,
) {
    val viewKey = open?.key
    val range = open?.let { ranges[it.key] }
    when {
        open == null ->
            TvFilterCategoryList(
                categories = categories,
                entryKey = position.category,
                filterCount = filterCount,
                copy = copy,
                onClear = onClearAll,
                onFocused = { onPosition(position.copy(category = it)) },
                onOpen = onDrill,
                entry = entry,
                seed = seed,
                onEntryPlaced = { onEntryPlaced(viewKey) },
                entryLooksFocused = entryLooksFocused,
                modifier = modifier,
            )
        range != null ->
            TvFilterRange(
                label = open.label,
                steps = range.steps,
                from = range.from,
                to = range.to,
                onBack = { onDrill(null) },
                onChange = range.onChange,
                entry = entry,
                copy = copy,
                modifier = modifier,
                stepIcon = range.stepIcon,
                initialBound = range.initialBound,
                offScaleLabel = range.offScaleLabel,
                rank = range.rank,
                seed = rangeSeed,
                onEntryPlaced = { onEntryPlaced(viewKey) },
                entryLooksFocused = entryLooksFocused,
            )
        else ->
            TvFilterValues(
                category = open,
                entryKey = position.value,
                onFocused = { onPosition(position.copy(value = it)) },
                onBack = { onDrill(null) },
                onToggle = { key -> onToggle(open.key, key) },
                // Focus stays in the values view's pinned rows.
                onClearCategory = {
                    onClearCategory(open.key)
                    onPosition(position.copy(value = null))
                },
                entry = entry,
                copy = copy,
                modifier = modifier,
                matchAllKey = matchAllKey,
                anyKey = anyKey,
                pinnedRow = pinnedRow,
                valueExtras = valueExtras,
                seed = seed,
                onEntryPlaced = { onEntryPlaced(viewKey) },
                entryLooksFocused = entryLooksFocused,
            )
    }
}
