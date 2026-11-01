package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay

/**
 * Re-claims focus after **this press** swaps the focused subtree out from under itself — sign-in mints a confirm
 * button, confirm/sign-out swaps in the other signed state, Back rebuilds the home destination. Compose's
 * focus-recovery search otherwise lands on the shell's top bar; the fix each time was the same
 * arm-a-latch-then-refocus dance, hand-copied across two files.
 *
 * The latch is **armed by the action**, not by mount or by the state change itself. That is load-bearing: a
 * background state change (a stats refresh, a session poll) must reach the same "signed in" state without yanking
 * focus. Only the press that caused the swap arms the latch, so only it re-focuses — once per arm, via a
 * request made when the replacement attaches. This is why neither `TvOverlayArrivalFocusEffect` (unconditional on
 * mount/key) nor `TvArrivalFocusEffect` (one request when its target attaches, armed by nothing) fit — both would fire
 * on a background change.
 *
 * @see TvSwapFocusEffect for the driving effect.
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
        attachesAtArm = attaches
        armed = true
    }

    internal fun disarm() {
        armed = false
    }

    /** How many [tvSwapTarget] controls have attached, ever, and how many had when [arm] was called. */
    internal var attaches by mutableIntStateOf(0)
        private set
    private var attachesAtArm = 0

    /** True once a target has attached since the arm: a target already on screen is the control being swapped out. */
    internal val replacementAttached: Boolean get() = attaches > attachesAtArm

    internal fun targetAttached() {
        attaches++
    }
}

/**
 * The swap-focus holder for one surface, stable across recomposition. Put [tvSwapTarget] on the
 * replacement control (it carries the requester and counts the attach), [arm][TvSwapFocus.arm] it on the press, and drive
 * it with [TvSwapFocusEffect]. A control that only takes the requester never counts as attached, so the swap expires
 * without focusing.
 */
@Composable
fun rememberTvSwapFocus(): TvSwapFocus {
    val requester = remember { FocusRequester() }
    return remember { TvSwapFocus(requester) }
}

/**
 * A swap-focus holder that drives an **existing** [requester] rather than minting its own — for a replacement
 * control whose requester is owned elsewhere (a requester the shell owns and aims itself, as well as
 * through the go-home swap). [tvSwapTarget] attaches this same [requester], so the replacement takes the modifier and
 * must not also apply the requester itself.
 */
@Composable
fun rememberTvSwapFocus(requester: FocusRequester): TvSwapFocus = remember(requester) { TvSwapFocus(requester) }

/**
 * Marks the replacement control as [swap]'s target: it carries the requester and tells [swap] it is attached, so the
 * effect makes its one request once there is a node to take it. Put it on every control that can be the replacement; they
 * never compose together.
 */
@Composable
fun Modifier.tvSwapTarget(swap: TvSwapFocus): Modifier {
    LaunchedEffect(swap) { swap.targetAttached() }
    return focusRequester(swap.requester)
}

/**
 * Claims focus for the replacement, once, when its [tvSwapTarget] is attached while [swap] is armed, then disarms. One
 * request, no retry: the arm outlives the press by [SWAP_WINDOW_MS], so a replacement that composes a little later
 * still gets it, and a swap that never produces a target lets go instead of pulling focus on a later, unrelated change.
 */
@Composable
fun TvSwapFocusEffect(swap: TvSwapFocus) {
    LaunchedEffect(swap.armed) {
        if (!swap.armed) return@LaunchedEffect
        delay(SWAP_WINDOW_MS)
        swap.disarm()
    }
    LaunchedEffect(swap.armed, swap.attaches) {
        if (!swap.armed || !swap.replacementAttached) return@LaunchedEffect
        swap.disarm()
        swap.requester.tryRequestFocus()
    }
}

private const val SWAP_WINDOW_MS = 3_000L
