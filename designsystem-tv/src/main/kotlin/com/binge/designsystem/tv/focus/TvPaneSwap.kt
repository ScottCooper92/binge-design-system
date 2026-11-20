package com.binge.designsystem.tv.focus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp

/**
 * Carries focus across the filter pane swapping between the category list and a category's values. The view holding
 * focus is removed in the same pass that composes the next one, and the next one's rows compose during measure, so
 * focus would fall to the bar first. [begin], called as the swap is decided, moves focus to a held node that is
 * always in the pane, and the incoming view's entry row takes it once it reports being placed. A direction press
 * ends the wait early: the press is spent giving the entry its focus, and goes no further.
 */
@Stable
internal class TvPaneSwap(
    private val entry: FocusRequester,
) {
    internal val hold = FocusRequester()
    internal var swapping by mutableStateOf(false)
        private set
    private var heldFocus = false
    private var target: String? = null

    /** Call before the view changes, while the outgoing row still holds focus. [target] is the view it changes to: a category's key, or null for the list. */
    fun begin(target: String?) {
        this.target = target
        swapping = true
        runCatching { hold.requestFocus() }
    }

    /**
     * Call from an entry row as it is placed, with the view it belongs to. The incoming view's row takes focus in that
     * layout pass, not a frame later; the outgoing view's, which can still be laid out once more, is ignored.
     */
    fun noteEntryPlaced(view: String?) {
        if (swapping && view == target) claimEntry()
    }

    internal fun noteHoldFocus(focused: Boolean) {
        if (focused) {
            heldFocus = true
        } else if (heldFocus) {
            finish()
        }
    }

    internal fun finish() {
        heldFocus = false
        swapping = false
    }

    internal fun claimEntry() {
        runCatching { entry.requestFocus() }
        finish()
    }
}

@Composable
internal fun rememberTvPaneSwap(entry: FocusRequester): TvPaneSwap {
    return remember(entry) { TvPaneSwap(entry) }
}

/** The node that holds focus through a swap: always in the pane, with no size, and focusable only while one is under way. */
@Composable
internal fun TvPaneHold(swap: TvPaneSwap) {
    Box(
        Modifier
            .size(Dp.Hairline)
            .focusProperties { canFocus = swap.swapping }
            .focusRequester(swap.hold)
            .onFocusChanged { swap.noteHoldFocus(it.isFocused) }
            .onPreviewKeyEvent { event ->
                val pressed = event.type == KeyEventType.KeyDown && swap.swapping
                if (pressed) swap.claimEntry()
                pressed
            }.focusable(),
    )
}

/**
 * Marks a row as the view's entry: it takes [entry], claims the shell's arrival when a tab switch brought the screen on, and
 * says when it has been placed so a held focus can move to it.
 */
internal fun Modifier.tvPaneEntry(entry: FocusRequester, onPlaced: () -> Unit): Modifier =
    tvShellArrivalTarget(entry).onGloballyPositioned { onPlaced() }
