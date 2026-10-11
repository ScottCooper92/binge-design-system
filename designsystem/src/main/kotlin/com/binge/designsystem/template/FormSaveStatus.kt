package com.binge.designsystem.template

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.R
import com.binge.designsystem.SaveStatus

/** [FormScreen]'s save status under its first name. Both platforms now read the one [SaveStatus]. */
@Deprecated("FormScreen takes the shared SaveStatus.", ReplaceWith("SaveStatus", "com.binge.designsystem.SaveStatus"))
typealias FormSaveStatus = SaveStatus

/** The inline "Saving…" or "Saved", in the bar's trailing slot. A failure has no inline text: its snackbar says so. */
@Composable
internal fun RowScope.FormSaveStatusText(status: SaveStatus) {
    val label =
        when (status) {
            SaveStatus.Saving -> stringResource(R.string.save_status_saving)
            SaveStatus.Saved -> stringResource(R.string.save_status_saved)
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
 * Keeps the failure snackbar up for as long as [status] is [SaveStatus.Failed]. It is shown indefinitely, with
 * Retry, and is dismissed when the status moves on: Retry sends the change again, so the next status is Saving.
 */
@Composable
internal fun FormSaveFailureSnackbar(status: SaveStatus, snackbarHostState: SnackbarHostState) {
    val failed = status as? SaveStatus.Failed
    val onRetry = rememberUpdatedState(failed?.onRetry)
    val message = stringResource(R.string.save_status_failed)
    val retry = stringResource(R.string.save_status_retry)
    LaunchedEffect(failed != null) {
        if (failed != null) {
            // Cancelled when the status leaves Failed, which takes the snackbar down with it.
            val result = snackbarHostState.showSnackbar(message = message, actionLabel = retry, duration = SnackbarDuration.Indefinite)
            if (result == SnackbarResult.ActionPerformed) onRetry.value?.invoke()
        }
    }
}
