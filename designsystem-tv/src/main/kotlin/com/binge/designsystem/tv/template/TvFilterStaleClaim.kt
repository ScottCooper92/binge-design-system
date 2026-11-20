package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.tv.focus.LocalTvUserMoves
import com.binge.designsystem.tv.focus.TvUserMoves

/**
 * Focus for the entry cell after a stale-results retry succeeds. Try again holds focus until the line leaves with the success, and
 * Compose then gives it to the first focusable in the shell, the top bar's tab. So the success arms one claim, and the entry cell's next
 * placement, which the grid shifting up into the line's space always causes, spends it: one request, no loop. A move the user made at
 * any point after Try again was pressed keeps it from being spent, as in `TvRetryHandoff`.
 */
@Stable
internal class TvFilterStaleClaim(
    private val moves: TvUserMoves,
) {
    private var pressedAtMove = 0
    private var armed = false

    fun begin() {
        pressedAtMove = moves.count
        armed = false
    }

    fun arm() {
        armed = true
    }

    fun onEntryPlaced(request: () -> Unit) {
        if (!armed) return
        armed = false
        if (moves.count == pressedAtMove) request()
    }
}

/**
 * A stale-results retry kept for as long as it is in flight. The line stays while the refresh loads, with its button busy, rather
 * than leaving and coming back if the retry fails too: on TV the button holds focus, and a node that leaves loses it.
 */
@Stable
internal class TvFilterStaleRetry {
    var retrying by mutableStateOf(false)
        private set

    fun begin() {
        retrying = true
    }

    fun settle() {
        retrying = false
    }
}

/** A [TvFilterStaleRetry] that settles once the refresh stops, whether it succeeded or failed again. */
@Composable
internal fun rememberTvFilterStaleRetry(refreshing: Boolean): TvFilterStaleRetry {
    val retry = remember { TvFilterStaleRetry() }
    LaunchedEffect(refreshing) { if (!refreshing) retry.settle() }
    return retry
}

@Composable
internal fun rememberTvFilterStaleClaim(
    retrying: Boolean,
    refreshing: Boolean,
    stale: Boolean,
    hasResults: Boolean,
): TvFilterStaleClaim {
    val moves = LocalTvUserMoves.current
    val claim = remember(moves) { TvFilterStaleClaim(moves) }
    // Read in composition: the retry settles in an effect of the same frame, so by then it no longer says it was retrying.
    LaunchedEffect(retrying, refreshing, stale) {
        if (retrying && !refreshing && !stale && hasResults) claim.arm()
    }
    return claim
}
