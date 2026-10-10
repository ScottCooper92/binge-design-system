package com.binge.designsystem.component

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** The deepest index seen so far, held outside snapshot state: nothing composes on it. */
private class DeepestIndex {
    var value: Int? = null

    /** The list the last save handed to the registry, which a leave empties; see [deepestIndexSaver]. */
    var lastSaved: ArrayList<Int>? = null

    /** Set once the visit has reported, so a save made after that carries nothing. */
    var reported = false
}

/**
 * Saves the deepest index of a visit that has not reported, as a one-element list; an empty one restores nothing.
 *
 * It saves a live visit whatever the cause. The platform saves one when the activity stops, and a configuration change
 * that reaches the stopped activity restores from that save, so the save cannot wait for the change. But a surface
 * that is left saves its state *before* its effect reports, so that save would carry a visit that is already over into
 * the next one. The report empties the list the last save handed out, so a restore from it starts a fresh visit.
 */
private fun deepestIndexSaver(): Saver<DeepestIndex, ArrayList<Int>> =
    Saver(
        save = { deepest ->
            val index = deepest.value
            if (deepest.reported || index == null) {
                null
            } else {
                arrayListOf(index).also { deepest.lastSaved = it }
            }
        },
        restore = { saved -> DeepestIndex().apply { value = saved.firstOrNull() } },
    )

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
 *
 * A configuration change (a rotation, a theme or locale change) is not a leave: the disposal it causes reports
 * nothing, and the recreated effect carries on with the deepest index seen before it. So one visit is one report,
 * and a visit that went deeper before a rotation and then scrolled back up still reports how deep it went. That holds
 * when the change reaches an activity that is already stopped, because a live visit is saved at the stop. A visit that
 * has reported carries nothing, such as the one the back stack saves when the surface is left.
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
    val activity = LocalActivity.current
    ScrollDepthEffect(onVisitEnded, lastVisibleIndex) { activity?.isChangingConfigurations == true }
}

/** [ScrollDepthEffect] with the configuration-change check passed in, so a JVM test can stand in for the activity. */
@Composable
internal fun ScrollDepthEffect(
    onVisitEnded: (Int?) -> Unit,
    lastVisibleIndex: () -> Int?,
    isChangingConfigurations: () -> Boolean,
) {
    // The report happens in onDispose, after any recomposition that replaced the lambda.
    val currentOnVisitEnded by rememberUpdatedState(onVisitEnded)
    val changing by rememberUpdatedState(isChangingConfigurations)
    val deepest = rememberSaveable(saver = deepestIndexSaver()) { DeepestIndex() }
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
        onDispose {
            if (!changing()) {
                deepest.reported = true
                deepest.lastSaved?.clear()
                currentOnVisitEnded(deepest.value)
            }
        }
    }
}
