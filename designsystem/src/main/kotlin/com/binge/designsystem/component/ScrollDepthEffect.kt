package com.binge.designsystem.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** The deepest index seen so far, held outside snapshot state: nothing composes on it. */
private class DeepestIndex {
    var value: Int? = null
}

/**
 * Tracks how far down a [LazyListState] list a visit got, and reports it **once, on leave**.
 *
 * Detection lives here for the same reason [RowImpressionEffect]'s does — `layoutInfo` is the only
 * place layout is visible — while what the number means stays with the caller's ViewModel.
 *
 * Reporting on dispose rather than as the depth grows is the whole design. One event per threshold
 * crossed is the same event under a name that makes it uncountable: a visit reaching row 12 would emit
 * four, and no dashboard could then say how many *visits* got that far.
 *
 * The disposal that fires this is the same one the caller starts its visit in, so the window the depth
 * describes and the window an impression is deduplicated over are one boundary by construction, not two
 * that have to be kept in step.
 *
 * A surface that laid out nothing — an error or empty state — reports `null` rather than zero: it had no
 * depth to reach, which is not the same as a visit that opened it and scrolled nothing.
 */
@Composable
fun ScrollDepthEffect(listState: LazyListState, onVisitEnded: (deepestIndex: Int?) -> Unit) {
    ScrollDepthEffect(onVisitEnded) {
        listState.layoutInfo.visibleItemsInfo
            .lastOrNull()
            ?.index
    }
}

/** The grid form of [ScrollDepthEffect]. Same contract; `LazyGridState` shares no supertype with the list. */
@Composable
fun ScrollDepthEffect(gridState: LazyGridState, onVisitEnded: (deepestIndex: Int?) -> Unit) {
    ScrollDepthEffect(onVisitEnded) {
        gridState.layoutInfo.visibleItemsInfo
            .lastOrNull()
            ?.index
    }
}

@Composable
private fun ScrollDepthEffect(onVisitEnded: (Int?) -> Unit, lastVisibleIndex: () -> Int?) {
    // The report happens in onDispose, after any recomposition that replaced the lambda.
    val currentOnVisitEnded by rememberUpdatedState(onVisitEnded)
    val deepest = remember { DeepestIndex() }
    // Keyed on Unit, so the effect runs for the surface's whole life holding whatever it first captured.
    val currentLastVisibleIndex by rememberUpdatedState(lastVisibleIndex)
    LaunchedEffect(Unit) {
        snapshotFlow { currentLastVisibleIndex() }
            .distinctUntilChanged()
            .collect { index ->
                if (index != null && index > (deepest.value ?: -1)) deepest.value = index
            }
    }
    DisposableEffect(Unit) {
        onDispose { currentOnVisitEnded(deepest.value) }
    }
}
