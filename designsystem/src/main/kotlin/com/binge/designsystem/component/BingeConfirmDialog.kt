package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
 * Canonical Binge confirm dialog: a title, a body message, and Cancel and confirm as plain text buttons, the
 * platform's own dialog shape. Stateless — the caller owns the visibility flag and reacts to
 * [onConfirm]/[onDismissRequest].
 *
 * Set [destructive] for irreversible actions (sign-out, delete): the confirm text takes the error colour. The visible body is delegated to
 * [BingeConfirmDialogContent] so screenshot tests can render it (the [AlertDialog] window does not capture).
 */
@Composable
fun BingeConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = stringResource(R.string.action_cancel),
    destructive: Boolean = false,
    extraContent: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
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
                onDismissRequest = onDismissRequest,
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
    onDismissRequest: () -> Unit,
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
            onDismissRequest = onDismissRequest,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConfirmActions(
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    // A wrapping row: side by side while both fit, and one per line, still end-aligned, when a long label or a large
    // font would squeeze the confirm into a few words a line.
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s), Alignment.End),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        BingeTextButton(label = dismissLabel, onClick = onDismissRequest)
        BingeTextButton(label = confirmLabel, onClick = onConfirm, destructive = destructive)
    }
}
