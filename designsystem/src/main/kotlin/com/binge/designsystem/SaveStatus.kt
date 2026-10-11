package com.binge.designsystem

import androidx.compose.runtime.Immutable

/**
 * Where the last change on a save-as-made edit page stands. Such a page has no Save button: each change saves as
 * it is made, and one status line says how that went.
 *
 * This is the one model both platforms' renderers read, the phone's `FormScreen` and the TV's `TvSaveStatus`, so a
 * phone page and a TV page say the same thing. A failed save keeps the change on screen, unsent, and offers Retry
 * until the next write succeeds. Nothing here undoes the change: the page holds it, and Retry sends it again.
 */
@Immutable
sealed interface SaveStatus {
    /** Nothing written yet, or the status has been cleared. No status is drawn. */
    data object Idle : SaveStatus

    /** A change is on its way to wherever it is stored. */
    data object Saving : SaveStatus

    /** The last change was stored. It stays until the next one starts. */
    data object Saved : SaveStatus

    /** The last change was not stored. It stays on screen; [onRetry] sends it again. */
    data class Failed(
        val onRetry: () -> Unit,
    ) : SaveStatus
}
