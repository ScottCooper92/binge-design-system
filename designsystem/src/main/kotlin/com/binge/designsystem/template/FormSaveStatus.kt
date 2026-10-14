package com.binge.designsystem.template

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.R

/**
 * Where a save-as-made form stands: an edit screen applies each change as it is made, with no Save button, and
 * reports the last write through this. Pass it as [FormScreen]'s `saveStatus`.
 *
 * [Saving] and [Saved] are a short status in the bar. [Failed] keeps the change on screen, unsent, and shows a
 * snackbar with Retry that stays until the next write succeeds, so a failure is never missed. Nothing here undoes
 * the change: the form holds it, and Retry sends it again.
 */
@Immutable
sealed interface FormSaveStatus {
    /** Nothing written yet, or the status has been cleared. No status is drawn. */
    data object Idle : FormSaveStatus

    /** A change is on its way to the server. */
    data object Saving : FormSaveStatus

    /** The last change was written. It stays until the next one starts. */
    data object Saved : FormSaveStatus

    /** The last change was not written. It stays on screen; [onRetry] sends it again. */
    data class Failed(
        val onRetry: () -> Unit,
    ) : FormSaveStatus
}

/**
 * The inline "Saving…" or "Saved", for a bar's trailing slot. A failure has no inline text: [FormSaveFailureSnackbar]
 * says so. [FormScreen] draws this itself; a page built on another scaffold places it in the bar's actions.
 */
@Composable
fun RowScope.FormSaveStatusText(status: FormSaveStatus) {
    val label =
        when (status) {
            FormSaveStatus.Saving -> stringResource(R.string.form_save_saving)
            FormSaveStatus.Saved -> stringResource(R.string.form_save_saved)
            else -> null
        }
    if (label != null) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            // Polite, so a screen reader reads each change of status without cutting in on what it is reading.
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_s)).semantics { liveRegion = LiveRegionMode.Polite },
        )
    }
}

/**
 * Keeps the failure snackbar up for as long as [status] is [FormSaveStatus.Failed]. It is shown indefinitely, with
 * Retry, and goes when the status moves on: Retry sends the change again, so the next status is Saving.
 *
 * It is shown again whenever it goes without being answered, since another message on the host dismisses the one
 * showing, and after a Retry that fails at once, which leaves the status Failed and this effect unrestarted. Retry
 * calls the latest `onRetry`, not the one the effect started with.
 */
@Composable
fun FormSaveFailureSnackbar(status: FormSaveStatus, snackbarHostState: SnackbarHostState) {
    val failed = status as? FormSaveStatus.Failed
    val onRetry = rememberUpdatedState(failed?.onRetry)
    val message = stringResource(R.string.form_save_failed)
    val retry = stringResource(R.string.form_save_retry)
    LaunchedEffect(failed != null) {
        if (failed == null) return@LaunchedEffect
        // Cancelled when the status leaves Failed, which takes the snackbar down with it.
        while (true) {
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = retry, duration = SnackbarDuration.Indefinite)
            if (result == SnackbarResult.ActionPerformed) onRetry.value?.invoke()
        }
    }
}
