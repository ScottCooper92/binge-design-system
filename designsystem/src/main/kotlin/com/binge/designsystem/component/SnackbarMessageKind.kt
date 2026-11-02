package com.binge.designsystem.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult

/**
 * The kind of message a snackbar carries, which determines how long it stays on screen. Every
 * snackbar in the app picks a kind so its [SnackbarDuration] is a deliberate, type-driven choice
 * rather than the accidental Compose default.
 */
enum class SnackbarMessageKind {
    /** A "done" acknowledgement (favourited, added to list, requested, rating saved, …). */
    Confirmation,

    /** A failure, or anything offering a retry/follow-up action the user must be able to reach. */
    Error,

    /** A blocking operation in progress (e.g. a gallery download) that stays until it resolves. */
    InProgress,
    ;

    val duration: SnackbarDuration
        get() =
            when (this) {
                Confirmation -> SnackbarDuration.Short
                Error -> SnackbarDuration.Long
                InProgress -> SnackbarDuration.Indefinite
            }
}

/**
 * Shows [message] with the [SnackbarDuration] dictated by [kind], so call sites express the message
 * *type* and never re-litigate the duration. A snackbar is for a result: one short sentence, readable in full at 1.3x
 * font, with at most one action. Text the user must read to act (why it failed and what to do) is a [BingeMessageDialog], not a longer snackbar;
 * see `docs/messages.md`. [actionLabel] forces [SnackbarMessageKind.Error] so an
 * actionable snackbar is always readable long enough to tap its action.
 */
suspend fun SnackbarHostState.showSnackbar(
    message: String,
    kind: SnackbarMessageKind,
    actionLabel: String? = null,
): SnackbarResult =
    showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = (if (actionLabel != null) SnackbarMessageKind.Error else kind).duration,
    )
