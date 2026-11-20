package com.binge.designsystem.tv.focus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.Dp

/**
 * Carries focus across a retry that removes the plate holding it. Pressing Try again takes its plate away at once, and
 * Compose then puts focus on whatever is nearest, often a tab outside the pane. So [begin] moves focus to a held node that
 * is never removed, and once the retried results are in and a cell is placed, [TvRetryHandoff.cellPlaced] has the pane's
 * entry take it from there. Focus is on a node at every frame. It ends when the failure shows again, the user moves, or the
 * entry takes focus. The source must pass through a non-failure state on a retry, or the plate would keep the hold unseen.
 */
@Stable
class TvRetryHandoff internal constructor(
    private val moves: TvUserMoves,
) {
    internal val hold = FocusRequester()
    internal var held by mutableStateOf(false)
        private set

    /** Whether the hold has focus now: a retry is under way and the pane's entry has not taken it. The grid draws the ring. */
    val holding: Boolean get() = held
    internal var leftFailure by mutableStateOf(false)
        private set
    internal var placed by mutableStateOf(false)
        private set
    private var pressedAtMove = 0

    /**
     * Call as Try again is pressed, while its plate still holds focus. The hold is always composed and only focusable while
     * [held], so it is attached when this asks and focus moves in the press itself, before the plate goes.
     */
    fun begin() {
        pressedAtMove = moves.count
        placed = false
        leftFailure = false
        held = true
        runCatching { hold.requestFocus() }
    }

    /** Call from a result cell as it is placed. */
    fun cellPlaced() {
        if (held) placed = true
    }

    internal fun movedSinceBegin() = moves.count != pressedAtMove

    internal fun noteLeftFailure() {
        leftFailure = true
    }

    internal fun end() {
        held = false
    }
}

/**
 * The hand-off for one retry site. [showingFailure] is whether a failure or empty plate is on screen, [hasResults] whether
 * results are in, and [takeFocus] asks the pane's entry for focus once a cell is placed. When the failure shows again, the
 * plate's own arrival focus takes over.
 */
@Composable
fun rememberTvRetryHandoff(
    showingFailure: Boolean,
    hasResults: Boolean,
    takeFocus: () -> Unit,
): TvRetryHandoff {
    val moves = LocalTvUserMoves.current
    val handoff = remember(moves) { TvRetryHandoff(moves) }
    val currentTakeFocus by rememberUpdatedState(takeFocus)
    LaunchedEffect(handoff.held, showingFailure, hasResults, handoff.leftFailure, handoff.placed) {
        if (!handoff.held) return@LaunchedEffect
        when {
            handoff.movedSinceBegin() -> handoff.end()
            !showingFailure -> handoff.noteLeftFailure()
            handoff.leftFailure -> handoff.end()
        }
        if (handoff.held && hasResults && handoff.placed) {
            currentTakeFocus()
            handoff.end()
        }
    }
    return handoff
}

/**
 * The node that holds focus through the hand-off, with no size and not drawn. It is composed always, so its requester has a node
 * when [TvRetryHandoff.begin] asks, and takes focus only while the hand-off is held.
 *
 * @need keep focus on a node that is never removed while a retry replaces the plate that held it
 * @group tv
 */
@Composable
fun TvRetryHold(handoff: TvRetryHandoff, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(Dp.Hairline)
            .focusRequester(handoff.hold)
            .focusProperties { canFocus = handoff.held }
            .focusable(),
    )
}
