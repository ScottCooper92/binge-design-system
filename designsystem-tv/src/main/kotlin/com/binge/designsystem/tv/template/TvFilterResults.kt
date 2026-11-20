package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TV_POSTER_GRID_COLUMNS
import com.binge.designsystem.tv.component.TvCompactButton
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.component.TvPosterGrid
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.TvRetryHold
import com.binge.designsystem.tv.focus.rememberTvRetryHandoff
import com.binge.designsystem.R as DesR

/** What the results side of a [TvFilterGridPage] says, supplied as words so the page owns none. */
class TvFilterGridCopy(
    val empty: String,
    val stale: String,
    val retry: String,
)

/**
 * The results beside the filter pane: the poster grid, a message with a retry when the query failed, or the empty message when it
 * found nothing. [listKey] is the filter set, so new results arrive at the top of a fresh scroll. [refreshing] is whether a refresh
 * is in flight, which lets a stale-results retry stay busy until it settles.
 */
@Composable
internal fun <T : Any> TvFilterResults(
    results: TvGridResults<T>,
    refreshing: Boolean,
    cell: (index: Int, item: T) -> TvPosterCell,
    copy: TvFilterGridCopy,
    listKey: Any?,
    entry: FocusRequester,
    entryIndex: Int,
    onCellFocused: (Int) -> Unit,
    onRetry: () -> Unit,
    onRetryStale: () -> Unit,
) {
    val loading = results is TvGridResults.Loading
    val failed = results is TvGridResults.Error
    val items = results as? TvGridResults.Items<T>
    val count = items?.count ?: 0
    val staleFlag = items?.stale == true
    val staleRetry = rememberTvFilterStaleRetry(refreshing)
    val stale = staleFlag || (items != null && staleRetry.retrying)
    val staleButton = remember { FocusRequester() }
    val staleClaim = rememberTvFilterStaleClaim(staleRetry.retrying, refreshing, staleFlag, count > 0)
    // Try again leaves as the retry starts, so a held node keeps focus until the retried cells are placed and take it.
    val retry = rememberTvRetryHandoff(
        showingFailure = failed || results is TvGridResults.Empty,
        hasResults = count > 0,
        takeFocus = { runCatching { entry.requestFocus() } },
    )
    when {
        results is TvGridResults.Error -> Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
            // Try again carries the grid's entry requester, so entering the grid from the pane finds a node while it is the only one.
            TvOverlayArrivalFocusEffect(entry)
            Text(results.message ?: stringResource(results.kind.messageRes), color = MaterialTheme.colorScheme.onSurface)
            TvCompactButton(
                label = copy.retry,
                onClick = {
                    retry.begin()
                    onRetry()
                },
                modifier = Modifier.focusRequester(entry),
            )
        }
        results is TvGridResults.Empty -> Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(copy.empty, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        }
        else -> Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        ) {
            if (stale) {
                TvFilterStaleLine(
                    text = copy.stale,
                    retryLabel = copy.retry,
                    retrying = staleRetry.retrying,
                    button = staleButton,
                    onRetry = {
                        staleRetry.begin()
                        staleClaim.begin()
                        onRetryStale()
                    },
                )
            }
            TvPosterGrid(
                count = count,
                posterAt = { index -> items?.at?.invoke(index)?.let { item -> cell(index, item) } },
                loading = loading,
                entry = entry,
                entryIndex = entryIndex,
                listKey = listKey,
                // While the hold has focus the entry cell is drawn focused, so the outline never disappears for the skeleton.
                seededIndex = entryIndex.takeIf { retry.holding },
                // Keyed by index: the paged list refreshes under the grid, so an id read live can be asked for an index that is gone,
                // and a source can repeat a title across pages. The grid already starts afresh for each filter set.
                onCellFocused = onCellFocused,
                cellModifier = { index ->
                    val base = if (stale && index < TV_POSTER_GRID_COLUMNS) {
                        // A directional search from a column the line's button does not span would pass over it to the top bar.
                        tvFilterCellModifier(index).focusProperties { up = staleButton }
                    } else {
                        tvFilterCellModifier(index)
                    }
                    // Only the entry cell, once results are in, can take `entry`; a loading plate placing says nothing.
                    if (!loading && index == entryIndex) {
                        base.onGloballyPositioned {
                            retry.cellPlaced()
                            staleClaim.onEntryPlaced { runCatching { entry.requestFocus() } }
                        }
                    } else {
                        base
                    }
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
    TvRetryHold(retry)
}

/**
 * The results are the earlier query's because the new one failed to load: the sentence and a Try again, in place above the grid, which
 * stays where it was, focus included. The button is reached with ↑ from any cell of the first row, which routes there rather than leaving it to geometry.
 * Where it holds focus as the line leaves, [TvFilterStaleClaim] hands focus to the grid's entry cell.
 */
@Composable
private fun TvFilterStaleLine(
    text: String,
    retryLabel: String,
    retrying: Boolean,
    button: FocusRequester,
    onRetry: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = MaterialTheme.colorScheme.onSurface)
        TvCompactButton(
            label = retryLabel,
            onClick = { if (!retrying) onRetry() },
            modifier = Modifier.focusRequester(button),
            loading = retrying,
        )
    }
}
