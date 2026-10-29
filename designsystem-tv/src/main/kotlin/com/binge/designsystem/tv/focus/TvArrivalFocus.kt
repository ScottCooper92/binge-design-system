package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * The arrival-focus triple — a requester to aim at, a subtree observer that says whether the surface has
 * taken focus and has been placed, and the one request that claims it — bundled so the three can no longer disagree.
 *
 * Placing arrival focus is the screen's job: the TV shell has no forward handoff, so a destination (or the
 * page a drill-down pops back to) must claim focus itself. The claim is a single request, made once the target
 * has been placed, while the outgoing screen may still hold focus; claiming early means the outgoing screen's nodes
 * are already unfocused when they dispose, so no geometric reassignment runs. See [TvArrivalFocusEffect].
 *
 * Ten screens carried this as a hand-copied requester + `mutableStateOf` + `LaunchedEffect`, each with its own
 * KDoc restating the same race; this is that, once. A screen keeps a single [rememberTvArrivalFocus], threads
 * its [requester] into the entry group (or attaches [tvArrivalTarget] on its own root), wires the observer with
 * [tvArrivalObserver] or `onFocusChanged = holder::onFocusChanged`, and drives it with one [TvArrivalFocusEffect].
 */
@Stable
class TvArrivalFocus internal constructor(
    val requester: FocusRequester,
) {
    /** True once any node in the arrival target's subtree holds focus. */
    var hasFocus by mutableStateOf(false)
        private set

    /**
     * True once the arrival target's subtree has reported its focus state, which it does when its node attaches, so its
     * [requester] has a node to take a request.
     */
    internal var attached by mutableStateOf(false)

    /** The arrival key last seen by the effect, and whether that arrival has made its one request, granted or not. */
    internal var keySeen: Any? = UNSEEN
    internal var spent = false

    /** Record a subtree focus change. Bind to the entry group's observer so it can't drift from [requester]. */
    fun onFocusChanged(focused: Boolean) {
        hasFocus = focused
        attached = true
    }
}

/**
 * The arrival-focus holder for one surface, stable across recomposition. Fresh per composition entry, so a
 * drill-down pop (which disposes and re-runs) starts with [TvArrivalFocus.hasFocus] false and re-offers.
 */
@Composable
fun rememberTvArrivalFocus(): TvArrivalFocus {
    val requester = remember { FocusRequester() }
    return remember { TvArrivalFocus(requester) }
}

/**
 * Report subtree focus for [arrival] on a node its [requester][TvArrivalFocus.requester] is threaded into
 * elsewhere (the common case: the requester rides a shared grid/detail/list child as its entry target, and
 * only the observer is a modifier on that child).
 */
fun Modifier.tvArrivalObserver(arrival: TvArrivalFocus): Modifier = onFocusChanged { arrival.onFocusChanged(it.hasFocus) }

/**
 * Install both [arrival]'s requester and its subtree observer on one node — for a surface whose own root *is*
 * the arrival target (an overlay/gate), rather than one threading the requester into a shared child.
 */
fun Modifier.tvArrivalTarget(arrival: TvArrivalFocus): Modifier = this.focusRequester(arrival.requester).tvArrivalObserver(arrival)

/**
 * Counts the user's own D-pad moves, so an arrival that has not yet claimed focus can tell the user has already chosen
 * somewhere else. The host that sees every key (the shell scaffold) provides one with [LocalTvUserMoves] and calls
 * [note] on each direction press; without a host nothing is ever noted and an arrival always claims.
 */
@Stable
class TvUserMoves {
    /** Moves noted so far. Read when an arrival composes and again when it would claim, so it is not snapshot state. */
    var count: Int = 0
        private set

    /** Records one direction press by the user. */
    fun note() {
        count++
    }
}

/** The [TvUserMoves] the host feeds. The default is never fed, so an arrival without a host claims unconditionally. */
val LocalTvUserMoves = staticCompositionLocalOf { TvUserMoves() }

/**
 * Claims focus for [arrival] once, when its target has been placed: the one arrival request every TV destination makes.
 *
 * It fires once per arrival, keyed on the [arrival] holder and not on recomposition or on the target attaching again, and
 * never while [enabled] is false: a target that only exists once its data has landed opens the gate when it does, and
 * the request fires then. It does nothing if the user has pressed a direction since the arrival composed
 * ([LocalTvUserMoves]) or the arrival already holds focus, so it never pulls focus back from where the user went.
 *
 * A surface that arrives more than once without leaving the composition (the controls coming back after a sheet closes)
 * passes [arrivalKey], which changes with each arrival and makes the next one a fresh request.
 *
 * One request, no retry: the target must be attached from the first frame (a plate that becomes the real control in
 * place), which is what lets it work through a push while the outgoing screen still holds focus.
 */
@Composable
fun TvArrivalFocusEffect(
    arrival: TvArrivalFocus,
    enabled: Boolean = true,
    arrivalKey: Any? = Unit,
) {
    val moves = LocalTvUserMoves.current
    val movesAtArrival = remember(arrival, arrivalKey) { moves.count }
    LaunchedEffect(arrival, enabled, arrival.attached, arrivalKey) {
        if (arrival.keySeen != arrivalKey) {
            arrival.keySeen = arrivalKey
            arrival.spent = false
        }
        if (!enabled || !arrival.attached || arrival.spent) return@LaunchedEffect
        arrival.spent = true
        if (moves.count == movesAtArrival && !arrival.hasFocus) arrival.requester.tryRequestFocus()
    }
}

private val UNSEEN = Any()

/**
 * Place first focus on an overlay or a plate the moment it composes: one request, no wait and no retry. It is the sanctioned
 * effect for a modal, gate or step that pulls focus on open (the overlay case in `docs/tv-foundation.md`), replacing the bare
 * `LaunchedEffect { requestFocus() }` five overlays open-coded.
 *
 * The target must be attached from the first frame, which is what lets one request land, including on a node mounted
 * mid-transition. Named for "first focus" rather than "restore" so arrival and focus-*return* call sites grep apart. [key]
 * re-places the focus when the overlay's identity changes (a settings pane re-shown for a different link).
 */
@Composable
fun TvOverlayArrivalFocusEffect(target: FocusRequester, key: Any? = Unit) {
    LaunchedEffect(key) { target.tryRequestFocus() }
}
