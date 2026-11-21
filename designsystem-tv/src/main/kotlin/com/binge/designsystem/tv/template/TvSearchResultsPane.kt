package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TvErrorPlate
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** Whether focus is somewhere in the results pane. Not state: only the frame a failure arrives in reads it. */
private class TvSearchPaneFocus(
    var held: Boolean = false,
)

/**
 * The right pane: the grid, or on a failure one message with Try again in its place. The failure removes the cell focus
 * was on, so focus moves to Try again when it was in the pane; focus in the left column stays there. Once Try again is
 * pressed the grid comes back and takes focus.
 */
@Composable
internal fun TvSearchResultsPane(
    state: TvSearchState,
    query: String,
    cells: List<TvSearchCell>,
    gridEntry: FocusRequester,
    lastCellKey: String?,
    seededFocus: TvSearchSeededFocus,
    onCellFocused: (key: String, rowId: String?) -> Unit,
    onResultsVisitEnded: (deepestIndex: Int?) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // One query's results are one visit: a new query, or the results giving way to anything else, ends it.
    val resultsQuery = (state as? TvSearchState.Results)?.query
    val visitDepth = resultsQuery?.let { key(it) { rememberTvVisitDepth(onResultsVisitEnded) } }
    val paneFocus = remember { TvSearchPaneFocus() }
    val retryButton = remember { FocusRequester() }
    val failure = state as? TvSearchState.Failed
    val retry = rememberTvSearchRetry(state, query, cells.isNotEmpty(), gridEntry)
    if (failure != null) SideEffect { retry.noteFailure(failure) }
    val handingOff = retry.handingOff(state, cells.isNotEmpty())
    Column(
        modifier = modifier.onFocusChanged { paneFocus.held = it.hasFocus },
    ) {
        val shownFailure = failure ?: retry.lastFailure.takeIf { handingOff }
        if (shownFailure != null) {
            // Read as the failure arrives, before the grid holding focus is gone.
            val takeFocus = remember(shownFailure) { paneFocus.held }
            if (takeFocus && failure != null) TvOverlayArrivalFocusEffect(retryButton, failure)
            val tryAgain = shownFailure.onRetry
            // Unseen while it hands off, and over the moment focus leaves it: the user has gone elsewhere.
            val plate = if (handingOff) {
                Modifier
                    .alpha(0f)
                    .layout { measurable, constraints -> measureWithoutHeight(measurable, constraints) }
                    .onFocusChanged { if (!it.hasFocus) retry.end() }
            } else {
                Modifier
            }
            Box(modifier = plate) {
                TvErrorPlate(
                    kind = shownFailure.kind,
                    onRetry = tryAgain?.let {
                        {
                            onRetry()
                            retry.begin(query)
                            it()
                        }
                    },
                    title = shownFailure.title,
                    message = shownFailure.message,
                    retryFocusRequester = retryButton,
                    retryInitiallyFocused = seededFocus.retry,
                )
            }
        }
        if (failure == null) {
            // Nothing found: say so, then offer the recommendations underneath rather than a blank pane.
            if (state is TvSearchState.Empty) TvSearchEmpty(headline = state.headline, body = state.body)
            TvSearchGrid(
                cells = cells,
                loading = state is TvSearchState.LoadingIdle || state is TvSearchState.LoadingResults,
                entry = gridEntry,
                entryKey = lastCellKey,
                listKey = state.searchListKey(),
                seededKey = seededFocus.cellKey,
                onEntryPlaced = retry::noteEntryPlaced,
                onCellFocused = { index ->
                    cells.getOrNull(index)?.let { cell -> onCellFocused(cell.key, cell.rowId) }
                    // Only the results count toward depth; recommendations are the app's own to report.
                    visitDepth?.reach(index)
                },
            )
        }
    }
}

/** The no-match message: the search icon struck through, then [headline] and what to try instead in [body]. */
@Composable
private fun TvSearchEmpty(headline: String, body: String) {
    Row(
        modifier = Modifier.padding(bottom = dimensionResource(DesR.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_search_empty_icon)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs))) {
            Text(
                text = headline,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Keys the grid's scroll state, so a new set of cells starts at the top instead of keeping the old offset. */
internal fun TvSearchState.searchListKey(): String =
    when (this) {
        is TvSearchState.Results -> "results:$query"
        is TvSearchState.LoadingResults -> "loading"
        else -> "recommendations"
    }

private fun MeasureScope.measureWithoutHeight(measurable: Measurable, constraints: Constraints): MeasureResult {
    val placeable = measurable.measure(constraints)
    return layout(placeable.width, 0) { placeable.place(0, 0) }
}
