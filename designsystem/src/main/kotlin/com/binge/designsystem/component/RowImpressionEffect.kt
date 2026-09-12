package com.binge.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Reports each row of a [LazyListState] list as it reaches the viewport, by the item key the caller
 * gave it.
 *
 * Detection has to live here because `layoutInfo` is the only place layout is visible, but the
 * *decision* to log does not: this reports what it saw and the ViewModel decides what that means, so
 * `core/designsystem` stays analytics-free (#2269).
 *
 * "Reached the viewport" is any part of the row, not a visible fraction. A rail the user scrolled to
 * the edge of and turned back from is one they saw, and a fraction threshold would need a second
 * number nobody could justify from the data it produces.
 *
 * Reports every key every time it becomes visible, including one scrolled back to — deduplication is
 * the caller's, because only it knows what window "once" is measured over.
 *
 * String keys only: a row keyed on anything else is skipped rather than reported as something a
 * caller cannot match on.
 */
@Composable
fun RowImpressionEffect(listState: LazyListState, onRowVisible: (key: String) -> Unit) {
    // The effect suspends in `collect` before it ever calls this, so a lambda captured at launch
    // would go on reporting into a recomposed-away callback for as long as the list is scrolled.
    val currentOnRowVisible by rememberUpdatedState(onRowVisible)
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.mapNotNull { it.key as? String } }
            .distinctUntilChanged()
            .collect { keys -> keys.forEach(currentOnRowVisible) }
    }
}
