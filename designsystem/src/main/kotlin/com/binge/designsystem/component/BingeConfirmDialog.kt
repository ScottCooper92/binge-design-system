package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R

/**
 * Canonical Binge confirm dialog: a title, a body message, and a confirm/dismiss action pair.
 * Stateless — the caller owns the visibility flag and reacts to [onConfirm]/[onDismiss].
 *
 * Set [destructive] for irreversible actions (sign-out, delete) to tone the confirm action with
 * the error colour. The visible body is delegated to [BingeConfirmDialogContent] so it can be
 * rendered directly in screenshot tests (the [AlertDialog] modal window does not capture).
 */
@Composable
fun BingeConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.action_cancel),
    destructive: Boolean = false,
    extraContent: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(title) },
        text = {
            if (extraContent == null) {
                Text(message)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                    Text(message)
                    extraContent()
                }
            }
        },
        confirmButton = {
            ConfirmActions(
                confirmLabel = confirmLabel,
                dismissLabel = dismissLabel,
                destructive = destructive,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
            )
        },
    )
}

/**
 * Stateless body of [BingeConfirmDialog], split out so screenshot tests can render the dialog's
 * visible content directly — the modal [AlertDialog] window itself does not capture in previews.
 */
@Composable
fun BingeConfirmDialogContent(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.action_cancel),
    destructive: Boolean = false,
    extraContent: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        extraContent?.invoke()
        ConfirmActions(
            confirmLabel = confirmLabel,
            dismissLabel = dismissLabel,
            destructive = destructive,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ConfirmActions(
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.padding_s),
            Alignment.End,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BingeOutlinedButton(label = dismissLabel, onClick = onDismiss)
        BingeFilledButton(
            label = confirmLabel,
            onClick = onConfirm,
            destructive = destructive,
        )
    }
}
