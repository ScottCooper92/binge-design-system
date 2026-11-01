package com.binge.designsystem.tv.focus

import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first

/**
 * Closes a TV overlay (a sheet, filter panel, or debug pane) the one correct way: latch the close, release the
 * overlay's trap and return focus in one request, dispose the overlay, then reset the latch.
 *
 * That four-step latch is easy to copy-paste per screen. It
 * exists because acting inline loses the disposal race — an inline request fires while the
 * focus-trapped overlay is still composed and is swallowed, the "focus falls to the shell" class. Each
 * copy is another place to forget a teardown path, or to leave a latch armed to fire a stale request later.
 * This is that latch, once.
 *
 * An overlay that reports its disposal (a sheet given the closer, or any overlay calling [TvOverlayCloser.ReportDisposal]) gets
 * one request the moment it is gone, which lands in the frame it leaves. One that does not report gets only the request made
 * before [onClose] runs, which a focus trap still composed will swallow; a debug build logs that, so report. Either way
 * [onClose] must dispose the overlay.
 *
 * An overlay whose `tvExitFocusGroup` reads [TvOverlayCloser.trapping] lets the request before [onClose] through as the exit
 * starts. One that does not refuses it, and only a reporting overlay's request after the dispose lands.
 *
 * Call [TvOverlayCloser.close] from every dismissal path — the `BackHandler`, the sheet `onDismissRequest`, a
 * navigate-away. It is idempotent within one close (a second [TvOverlayCloser.close] mid-flight is a no-op) and
 * re-armable across opens (the latch resets so the next open closes too).
 *
 * Closing always restores focus. A screen that must hold back a re-request until its content has loaded owns
 * that as a separate re-focus of its own, not as part of the close, so there is no `shouldRestore` parameter
 * here.
 *
 * @param restoreTo the requester focus returns to once the overlay disposes (the grid/action-row entry).
 * @param onClose disposes the overlay (`sheet = null`, `debugOpen = false`), run before the focus return.
 */
@Composable
fun rememberTvOverlayCloser(restoreTo: FocusRequester, onClose: () -> Unit): TvOverlayCloser {
    var closing by remember { mutableStateOf(false) }
    val currentOnClose by rememberUpdatedState(onClose)
    val debuggable = (LocalContext.current.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    val closer = remember { TvOverlayCloser(arm = { closing = true }) }
    LaunchedEffect(closing) {
        if (!closing) return@LaunchedEffect
        // The trap is already released (see [TvOverlayCloser.trapping]), so a trap wired to it grants this at once. A grant while
        // the overlay is still composed is not the restore, though: its disposal can hand focus elsewhere, so the request after it
        // always runs.
        restoreTo.tryRequestFocus()
        val reportsDisposal = closer.reportsDisposal
        currentOnClose()
        if (reportsDisposal) {
            snapshotFlow { closer.disposed }.first { it }
            restoreTo.tryRequestFocus()
        } else {
            if (debuggable) Log.w(TAG, FALLBACK_MESSAGE)
        }
        // Reset so the overlay can be opened and closed again — an un-reset latch stays armed and either never
        // re-fires (a stuck close) or fires a stale request on a later recomposition.
        closing = false
    }
    // Stable identity so a `BackHandler`/`onDismissRequest` holding [TvOverlayCloser.close] is not re-armed every
    // recomposition; it wraps the one latch setter above.
    return closer
}

/** Handle for [rememberTvOverlayCloser]. [close] arms the close latch; safe to call from any dismissal path. */
@Stable
class TvOverlayCloser internal constructor(
    private val arm: () -> Unit,
) {
    private var released by mutableStateOf(false)

    /** Whether an overlay that reports its disposal is composed now, so the close waits for it instead of retrying. */
    internal var reportsDisposal by mutableStateOf(false)
        private set

    /** Whether that overlay has been disposed since it was last shown. */
    internal var disposed by mutableStateOf(false)
        private set

    /**
     * Tells the closer this overlay is composed, and when it is disposed, so the close makes its one request then. Call it
     * from inside the overlay, for one that does not take the closer itself (a full-screen reader).
     */
    @Composable
    fun ReportDisposal() {
        DisposableEffect(this) {
            overlayShown()
            onDispose { overlayGone() }
        }
    }

    /**
     * False from [close] until the overlay's panel has been disposed, so the trap stays open for the whole exit transition. Read by
     * the panel's `tvExitFocusGroup`; pass the closer to `TvSideSheet`, which wires it.
     */
    val trapping: Boolean get() = !released

    /** Arm the close: release the trap, return focus, dispose the overlay. Idempotent within one close. */
    fun close() {
        released = true
        arm()
    }

    /** The overlay's panel composed: trap again, whatever an earlier close left behind. */
    internal fun overlayShown() {
        released = false
        reportsDisposal = true
        disposed = false
    }

    /** The overlay's panel was disposed, at the end of its exit: the next open traps again. */
    internal fun overlayGone() {
        released = false
        reportsDisposal = false
        disposed = true
    }
}

private const val TAG = "TvOverlayCloser"
private const val FALLBACK_MESSAGE =
    "Closed an overlay that never reported its disposal, so focus was requested once as it closed and a focus trap " +
        "can swallow it: give the overlay the closer, or call ReportDisposal() inside it."
