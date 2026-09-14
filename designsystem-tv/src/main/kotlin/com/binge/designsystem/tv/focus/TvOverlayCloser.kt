package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester

/**
 * Closes a TV overlay (a sheet, filter panel, or debug pane) the one correct way: latch the close, dispose the
 * overlay, return focus one frame after disposal, then reset the latch.
 *
 * That four-step latch was copy-pasted thirteen times across twelve screens, near character-for-character. It
 * exists because acting inline loses the disposal race — an inline `restoreTvOverlayFocus` fires while the
 * focus-trapped overlay is still composed and is swallowed, the "focus falls to the rail" class. Thirteen
 * copies was thirteen places to forget a teardown path — one fix missed the third of three — or to leave a
 * latch armed to fire a stale request later. This is that latch, once.
 *
 * Call [TvOverlayCloser.close] from every dismissal path — the `BackHandler`, the sheet `onDismiss`, a
 * navigate-away. It is idempotent within one close (a second [TvOverlayCloser.close] mid-flight is a no-op) and
 * re-armable across opens (the latch resets so the next open closes too).
 *
 * All thirteen close latches restore unconditionally; `TvDiscoverScreen`'s content-pane guard belongs to
 * its separate `awaitingRequery` re-focus (a load-gated re-request after a filter change), not to the close, so
 * there is no `shouldRestore` parameter here.
 *
 * @param restoreTo the requester focus returns to once the overlay disposes (the grid/action-row entry).
 * @param onClose disposes the overlay (`sheet = null`, `debugOpen = false`), run before the focus return.
 */
@Composable
fun rememberTvOverlayCloser(restoreTo: FocusRequester, onClose: () -> Unit): TvOverlayCloser {
    var closing by remember { mutableStateOf(false) }
    val currentOnClose by rememberUpdatedState(onClose)
    LaunchedEffect(closing) {
        if (!closing) return@LaunchedEffect
        currentOnClose()
        restoreTvOverlayFocus(restoreTo)
        // Reset so the overlay can be opened and closed again — an un-reset latch stays armed and either never
        // re-fires (a stuck close) or fires a stale request on a later recomposition.
        closing = false
    }
    // Stable identity so a `BackHandler`/`onDismiss` holding [TvOverlayCloser.close] is not re-armed every
    // recomposition; it wraps the one latch setter above.
    return remember { TvOverlayCloser { closing = true } }
}

/** Handle for [rememberTvOverlayCloser]. [close] arms the close latch; safe to call from any dismissal path. */
@Stable
class TvOverlayCloser internal constructor(
    private val arm: () -> Unit,
) {
    /** Arm the close: dispose the overlay, then return focus a frame later. Idempotent within one close. */
    fun close() {
        arm()
    }
}
