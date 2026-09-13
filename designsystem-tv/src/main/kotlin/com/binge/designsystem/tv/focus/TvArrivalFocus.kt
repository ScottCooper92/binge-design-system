package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged

/**
 * The arrival-focus triple — a requester to aim at, a subtree observer that says whether the surface has
 * taken focus, and the per-frame offer that stops once it has — bundled so the three can no longer disagree.
 *
 * Placing arrival focus is the screen's job: the TV shell has no forward handoff, so a destination (or the
 * page a drill-down pops back to) must claim focus itself. A single mount-time `requestFocus()` fires inside
 * the push transition and fails silently — ~300ms before the outgoing screen's disposal hands focus over, by
 * which point that disposal's geometric reassignment is the writer — so the claim is *offered* each frame
 * until [hasFocus] reports the subtree holds it (see [offerTvArrivalFocus] for the measured reason).
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
    /** True once any node in the arrival target's subtree holds focus — the signal the offer loop stops on. */
    var hasFocus by mutableStateOf(false)
        private set

    /** Record a subtree focus change. Bind to the entry group's observer so it can't drift from [requester]. */
    fun onFocusChanged(focused: Boolean) {
        hasFocus = focused
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
 * Offer arrival focus to [arrival] each frame until its subtree takes it — the one shared home for the
 * mount/pop offer every TV destination places.
 *
 * [enabled] replaces the ad-hoc data guards screens open-coded (`episodes.isNotEmpty()`, `hasProviders`): the
 * effect is keyed on [enabled], so it re-offers when [enabled] flips false→true. A surface whose target has no
 * node until its content resolves (an empty season that later fills, #1705 finding 3) then still gets its offer
 * once the content arrives, instead of a `LaunchedEffect(Unit)` guard that runs once against nothing and never
 * retries.
 */
@Composable
fun TvArrivalFocusEffect(arrival: TvArrivalFocus, enabled: Boolean = true) {
    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect
        offerTvArrivalFocus(arrival.requester) { arrival.hasFocus }
    }
}

/**
 * Place first focus on an overlay one frame after it mounts — the sanctioned effect for a modal/gate/step that
 * pulls focus on open (the overlay case in `docs/tv-foundation.md`, the one thing allowed to), replacing the
 * bare `LaunchedEffect { requestFocus() }`
 * five overlays open-coded.
 *
 * The frame wait is [restoreTvOverlayFocus]'s and is load-bearing: an overlay mounted mid-transition (a dialog
 * inside an `AnimatedContent`, an onboarding step swapping in) has no laid-out node to take a synchronous
 * request, so the bare request fires into that gap and is lost. Named for "first focus" rather than "restore"
 * so arrival and focus-*return* call sites grep apart, over the one implementation. [key] re-runs the placement
 * when the overlay's identity changes (a settings pane re-shown for a different link).
 */
@Composable
fun TvOverlayArrivalFocusEffect(target: FocusRequester, key: Any? = Unit) {
    LaunchedEffect(key) { restoreTvOverlayFocus(target) }
}
