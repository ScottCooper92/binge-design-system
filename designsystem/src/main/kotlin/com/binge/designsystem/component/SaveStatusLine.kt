package com.binge.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.R
import com.binge.designsystem.template.FormSaveStatus

/**
 * The line under a setting that saves as it changes: "Saving…", "Saved", or, when the write could not be sent, the
 * failure with Retry beside it. It draws nothing while [status] is [FormSaveStatus.Idle], so a row that has not been
 * touched has no line at all.
 *
 * It belongs to a single switch or a short group, where the status of the one change should sit by the change. A whole
 * form shows its status in its bar through [FormScreen] instead. Both read [FormSaveStatus], and the engine that drives
 * them is [com.binge.designsystem.save.SaveAsMade].
 *
 * A failure does not undo the change on screen: the caller decides whether to put the saved value back, which is right
 * for a refusal and wrong for a lost connection, where Retry will send it. It is a polite live region, so a screen
 * reader announces each change of status without cutting in on what it is reading.
 */
@Composable
internal fun SaveStatusLine(status: FormSaveStatus, modifier: Modifier = Modifier) {
    if (status == FormSaveStatus.Idle) return
    // Wraps, so the failure and its Retry stack at a large font size rather than clip.
    WrappingRow(
        modifier = modifier
            .padding(
                horizontal = dimensionResource(R.dimen.padding_m),
                vertical = dimensionResource(R.dimen.padding_xs),
            ).semantics {
                liveRegion =
                    LiveRegionMode.Polite
            },
        horizontalSpacing = dimensionResource(R.dimen.padding_s),
    ) {
        when (status) {
            FormSaveStatus.Saving -> StatusText(stringResource(R.string.form_save_saving))
            FormSaveStatus.Saved -> StatusText(stringResource(R.string.form_save_saved))
            is FormSaveStatus.Failed -> {
                StatusText(stringResource(R.string.form_save_failed), failed = true)
                BingeTextButton(label = stringResource(R.string.form_save_retry), onClick = status.onRetry)
            }
            FormSaveStatus.Idle -> Unit
        }
    }
}

@Composable
private fun StatusText(text: String, failed: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = if (failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
