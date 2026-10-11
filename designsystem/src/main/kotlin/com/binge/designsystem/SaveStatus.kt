package com.binge.designsystem

import androidx.compose.runtime.Immutable

/**
 * Where the last change on a save-as-made edit page stands. Such a page has no Save button: each change saves as
 * it is made, and one status line says how that went.
 *
 * This is the one model both platforms' renderers read, so a phone page and a TV page say the same thing. A
 * failed save keeps the change on screen and offers Retry; retrying is a callback on the renderer, not part of
 * the state.
 */
@Immutable
sealed interface SaveStatus {
    /** Nothing has been changed yet, so there is nothing to report. */
    data object Idle : SaveStatus

    /** A change is on its way to wherever it is stored. */
    data object Saving : SaveStatus

    /** The last change was stored. */
    data object Saved : SaveStatus

    /** The last change could not be stored. It stays on screen, unsaved, until a retry succeeds. */
    data object Failed : SaveStatus
}
