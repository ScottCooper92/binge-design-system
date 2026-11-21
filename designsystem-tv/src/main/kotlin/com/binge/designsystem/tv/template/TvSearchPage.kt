package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.tv.component.TvSearchField
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.focus.tvShellArrivalTarget
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

/** One poster in the search grid, a result or a recommendation alike: [key] identifies it, [onClick] opens it. */
class TvSearchCell(
    val key: String,
    val imageUrl: String?,
    val title: String,
    val onClick: () -> Unit,
    /** The rail a recommendation belongs to, handed back through `onCellFocused` for impression reporting; null for a result. */
    val rowId: String? = null,
)

/** What the results pane is showing, which is also what the page's focus and plates follow. */
sealed interface TvSearchState {
    /** The first load, before there is a list of searches or recommendations: both draw as plates. */
    data object LoadingIdle : TvSearchState

    /** No query: the grid shows the recommendations the page was given. */
    data object Idle : TvSearchState

    /** A query is out: the grid draws plates in its cells. */
    data object LoadingResults : TvSearchState

    /** The cells are the results for [query]. */
    data class Results(
        val query: String,
    ) : TvSearchState

    /** Nothing matched [query]: [headline] and [body] say so above the recommendations. */
    data class Empty(
        val query: String,
        val headline: String,
        val body: String,
    ) : TvSearchState

    /** The load failed: the grid gives way to the [kind]'s message, with Try again when [onRetry] is set. */
    data class Failed(
        val kind: ErrorKind,
        val onRetry: (() -> Unit)?,
        val title: String? = null,
        val message: String? = null,
    ) : TvSearchState
}

/**
 * The two-pane TV search: the field and one list of searches down the left, a poster grid on the right. The grid shows
 * recommendations until there is a query, then results, and pins the focused row to its top. The page owns the field
 * (the system keyboard and voice), the focus hand-offs between the three, and the plates; the app owns the words and what
 * a press opens.
 *
 * Focus enters on the cell it last held, else the suggestion, else the field. Search or a suggestion moves focus to the grid
 * at once, and a failed load's Try again carries it back when the retry lands.
 *
 * @param query the app's query, for the field's first text and any later change the app makes itself.
 * @param queryRevision moves when the app replaces the query, so the field takes [query]; typing never moves it.
 * @param suggestions the list under the field, in order; the app merges history and trending and ranks it.
 * @param cells the grid's cells for [state]: results for [TvSearchState.Results], recommendations otherwise.
 * @param onCellFocused focus arrived on a cell, with the cell's `rowId`, for impression reporting; it is deduplicated by the app.
 * @param onResultsVisitEnded one query's results were left, with the deepest cell index reached, or null if none was.
 *
 * @need search on a TV: a field, a list of searches and a poster grid of results
 * @group tv
 * @replaces a hand-built two-pane search screen with its own focus hand-offs
 */
@Composable
fun TvSearchPage(
    query: String,
    queryRevision: Int,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onSubmit: () -> Unit,
    suggestions: List<TvSearchSuggestion>,
    onSuggestionSelected: (String) -> Unit,
    state: TvSearchState,
    cells: List<TvSearchCell>,
    modifier: Modifier = Modifier,
    seededFocus: TvSearchSeededFocus = TvSearchSeededFocus(),
    onCellFocused: (key: String, rowId: String?) -> Unit = { _, _ -> },
    onResultsVisitEnded: (deepestIndex: Int?) -> Unit = {},
) {
    val fieldEntry = remember { FocusRequester() }
    val gridEntry = remember { FocusRequester() }
    val suggestionEntry = remember { FocusRequester() }
    // On the root, ahead of the entry group, so one request from the shell is routed to the remembered cell, suggestion or field.
    val shellEntry = remember { FocusRequester() }
    var fieldText by rememberFieldText(query, queryRevision)
    // The cell focus was last on, so Back from a title returns to it and → from the left column lands on it.
    var lastCellKey by rememberSaveable { mutableStateOf<String?>(null) }
    // Or the suggestion it was last on: one of the two at a time, kept across a page opened over the screen.
    var lastSuggestionKey by rememberSaveable { mutableStateOf<String?>(null) }
    Row(
        modifier = modifier
            .tvShellArrivalTarget(shellEntry)
            .fillMaxSize()
            .tvEntryFocusGroup(
                when {
                    lastCellKey != null -> gridEntry
                    lastSuggestionKey != null -> suggestionEntry
                    else -> fieldEntry
                },
            ).padding(start = tvContentGutterStart(), top = dimensionResource(TvR.dimen.tv_overscan_vertical)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_search_gap)),
    ) {
        TvSearchLeftColumn(
            fieldText = fieldText,
            placeholder = placeholder,
            suggestions = suggestions,
            loading = state is TvSearchState.LoadingIdle,
            fieldEntry = fieldEntry,
            suggestionEntry = suggestionEntry,
            restoredSuggestionKey = lastSuggestionKey,
            seededFocus = seededFocus,
            onSuggestionFocused = {
                lastSuggestionKey = it
                lastCellKey = null
            },
            onFieldChange = {
                fieldText = it
                onQueryChange(it)
            },
            // Focus moves on the key itself: the grid is there from the first frame, and what arrives fills the cell focus is on.
            onSearch = {
                onSubmit()
                lastCellKey = null
                lastSuggestionKey = null
                runCatching { gridEntry.requestFocus() }
            },
            onSuggestionClick = { suggestion ->
                fieldText = suggestion
                onSuggestionSelected(suggestion)
                lastCellKey = null
                lastSuggestionKey = null
                runCatching { gridEntry.requestFocus() }
            },
        )
        TvSearchResultsPane(
            state = state,
            query = query,
            cells = cells,
            gridEntry = gridEntry,
            lastCellKey = lastCellKey,
            seededFocus = seededFocus,
            onCellFocused = { key, rowId ->
                lastCellKey = key
                lastSuggestionKey = null
                onCellFocused(key, rowId)
            },
            onResultsVisitEnded = onResultsVisitEnded,
            // The failed grid's scroll is gone, so the retried one starts at the top: its entry must be a cell that is composed.
            onRetry = { lastCellKey = null },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TvSearchLeftColumn(
    fieldText: String,
    placeholder: String,
    suggestions: List<TvSearchSuggestion>,
    loading: Boolean,
    fieldEntry: FocusRequester,
    suggestionEntry: FocusRequester,
    restoredSuggestionKey: String?,
    seededFocus: TvSearchSeededFocus,
    onSuggestionFocused: (String) -> Unit,
    onFieldChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSuggestionClick: (String) -> Unit,
) {
    Column(modifier = Modifier.width(dimensionResource(TvR.dimen.tv_search_left_width)).fillMaxHeight()) {
        // No mic, and a slimmer field: the outer height wins over the field's own.
        TvSearchField(
            value = fieldText,
            onValueChange = onFieldChange,
            label = placeholder,
            placeholder = placeholder,
            onSearch = onSearch,
            modifier = Modifier
                .height(dimensionResource(TvR.dimen.tv_search_field_height))
                .focusRequester(fieldEntry),
            initiallyFocused = seededFocus.field,
        )
        // The list runs to the bottom of the screen; past that, focus moving down scrolls it.
        val listModifier = Modifier
            .weight(1f)
            .padding(top = dimensionResource(TvR.dimen.tv_search_header_gap))
        if (suggestions.isNotEmpty() || loading) {
            TvSearchSuggestions(
                suggestions = suggestions,
                loading = loading,
                onSuggestionClick = onSuggestionClick,
                initialFocusedKey = seededFocus.suggestionKey,
                entryFocusRequester = suggestionEntry,
                restoredKey = restoredSuggestionKey,
                onRowFocused = onSuggestionFocused,
                firstRowAnchor = TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_SUGGESTION),
                firstIconAnchor = TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_SUGGESTION_ICON),
                modifier = listModifier
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical)),
            )
        }
    }
}
