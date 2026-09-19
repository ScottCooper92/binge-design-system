package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/**
 * Re-claims focus after **this press** swaps the focused subtree out from under itself — sign-in mints a confirm
 * button, confirm/sign-out swaps in the other signed state, Back rebuilds the home destination. Compose's
 * focus-recovery search otherwise lands on the nav rail; the fix each time was the same
 * arm-a-latch-then-refocus dance, hand-copied across two files.
 *
 * The latch is **armed by the action**, not by mount or by the state change itself. That is load-bearing: a
 * background state change (a stats refresh, a session poll) must reach the same "signed in" state without yanking
 * focus. Only the press that caused the swap arms the latch, so only it re-focuses — once per arm, via a
 * retry that keeps offering focus until the replacement takes it or the retry window runs out. This is why
 * neither `restoreTvOverlayFocus`/`TvOverlayArrivalFocusEffect` (unconditional on mount/key) nor
 * `TvArrivalFocusEffect` (offered every frame until taken) fit — both would fire on a background change.
 *
 * @see TvSwapFocusEffect for the driving effect and the frame-wait reasoning.
 */
@Stable
class TvSwapFocus internal constructor(
    val requester: FocusRequester,
) {
    /** True between [arm] and the effect firing — the guard that keeps a background state change off focus. */
    var armed by mutableStateOf(false)
        private set

    /** Record that this press caused a subtree swap; the effect re-focuses once the replacement is ready. */
    fun arm() {
        armed = true
    }

    internal fun disarm() {
        armed = false
    }
}

/**
 * The swap-focus holder for one surface, stable across recomposition. Thread [requester][TvSwapFocus.requester]
 * onto the replacement control, [arm][TvSwapFocus.arm] it on the press, and drive it with [TvSwapFocusEffect].
 */
@Composable
fun rememberTvSwapFocus(): TvSwapFocus {
    val requester = remember { FocusRequester() }
    return remember { TvSwapFocus(requester) }
}

/**
 * A swap-focus holder that drives an **existing** [requester] rather than minting its own — for a replacement
 * control whose requester is owned elsewhere (the shell's rail requester, aimed by the rail itself as well as
 * by the go-home swap).
 */
@Composable
fun rememberTvSwapFocus(requester: FocusRequester): TvSwapFocus = remember(requester) { TvSwapFocus(requester) }

/**
 * Offer focus to the replacement until it lands, once [ready] becomes true while [swap] is armed — then disarm.
 * Keyed on [ready] and [key], so a caller that gates on a recomposition label (the shell's
 * `currentKey == homeKey`) passes that key to re-evaluate when it changes rather than on a frame wait alone.
 *
 * Delegates to [restoreTvOverlayFocus] for the frame-wait-then-retry shape, load-bearing for the same measured
 * reason: the press disposes the focused node and swaps a new subtree in, so a request fired before the
 * replacement control is laid out is lost — and a single retry one frame later is not guaranteed either, since
 * the replacement may take longer than one frame to compose (a reload racing the arrival). Offering until
 * granted, bounded the same way [restoreTvOverlayFocus] is, is what the single mount-time request never was.
 */
@Composable
fun TvSwapFocusEffect(
    swap: TvSwapFocus,
    ready: Boolean,
    key: Any? = Unit,
) {
    LaunchedEffect(ready, key) {
        if (!swap.armed || !ready) return@LaunchedEffect
        try {
            restoreTvOverlayFocus(swap.requester)
        } finally {
            swap.disarm()
        }
    }
}
