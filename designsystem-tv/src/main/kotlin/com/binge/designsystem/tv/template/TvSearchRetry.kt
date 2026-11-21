package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import com.binge.designsystem.tv.focus.LocalTvUserMoves
import com.binge.designsystem.tv.focus.TvUserMoves

/**
 * A pressed Try again, held until the grid it brings back takes focus from the failure plate. The plate stays composed,
 * unseen, while the retry loads and its entry cell is placed, because removing the focused plate would drop focus to the
 * search field for a frame. The request is for one query, and ends when the query changes, the user presses a direction,
 * or focus leaves the plate.
 */
@Stable
internal class TvSearchRetry(
    private val moves: TvUserMoves,
) {
    var of by mutableStateOf<String?>(null)
        private set
    var entryPlaced by mutableStateOf(false)
        private set
    var lastFailure by mutableStateOf<TvSearchState.Failed?>(null)
        private set
    private var pressedAtMove = 0

    fun begin(query: String) {
        of = query
        pressedAtMove = moves.count
    }

    fun end() {
        of = null
    }

    fun noteEntryPlaced() {
        entryPlaced = true
    }

    fun noteFailure(failure: TvSearchState.Failed) {
        entryPlaced = false
        lastFailure = failure
    }

    internal fun userMoved() = moves.count != pressedAtMove

    /** Whether the retried content is in with cells to land on: results for the query, or the idle content of a cold start. */
    fun arrived(content: TvSearchState, hasCells: Boolean): Boolean =
        of?.let { content.isResultsFor(it) || content is TvSearchState.Idle } == true && hasCells

    /** Whether the plate stays composed: a retry is pending, no failure shows, and the content is loading or has arrived. */
    fun handingOff(content: TvSearchState, hasCells: Boolean): Boolean {
        val loading = content is TvSearchState.LoadingIdle || content is TvSearchState.LoadingResults
        return of != null && content !is TvSearchState.Failed && (loading || arrived(content, hasCells))
    }
}

/** The retry for the results pane, and the effect that gives the grid focus once its entry cell is placed. */
@Composable
internal fun rememberTvSearchRetry(
    content: TvSearchState,
    query: String,
    hasCells: Boolean,
    gridEntry: FocusRequester,
): TvSearchRetry {
    val moves = LocalTvUserMoves.current
    val retry = remember(moves) { TvSearchRetry(moves) }
    LaunchedEffect(retry.of, content, query, retry.entryPlaced) {
        val pending = retry.of ?: return@LaunchedEffect
        when {
            query != pending || retry.userMoved() -> retry.end()
            retry.handingOff(content, hasCells) && retry.arrived(content, hasCells) && retry.entryPlaced -> {
                runCatching { gridEntry.requestFocus() }
                retry.end()
            }
        }
    }
    return retry
}

internal fun TvSearchState.isResultsFor(query: String): Boolean =
    (this as? TvSearchState.Results)?.query == query || (this as? TvSearchState.Empty)?.query == query
