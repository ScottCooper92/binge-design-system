package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
 * Set [destructive] for irreversible actions (sign-out, delete): the confirm text takes the error colour. When
 * the dialog offers a second, worse outcome, pass it as [severeAction]; the actions then stack, Cancel first and
 * the severe one last, so two red actions are never mistaken for each other. The visible body is delegated to
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
    severeAction: BingeConfirmSevereAction? = null,
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
                severeAction = severeAction,
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
    severeAction: BingeConfirmSevereAction? = null,
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
            severeAction = severeAction,
            onConfirm = onConfirm,
            onDismissRequest = onDismissRequest,
        )
    }
}

/**
 * A second destructive outcome offered beside [BingeConfirmDialog]'s confirm, worse than it (remove a request, against
 * remove it and delete its files). Its label says in full what it does.
 */
data class BingeConfirmSevereAction(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
private fun ConfirmActions(
    confirmLabel: String,
    dismissLabel: String,
    destructive: Boolean,
    severeAction: BingeConfirmSevereAction?,
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    if (severeAction == null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s), Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BingeTextButton(label = dismissLabel, onClick = onDismissRequest)
            BingeTextButton(label = confirmLabel, onClick = onConfirm, destructive = destructive)
        }
    } else {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
            horizontalAlignment = Alignment.End,
        ) {
            BingeTextButton(label = dismissLabel, onClick = onDismissRequest)
            BingeTextButton(label = confirmLabel, onClick = onConfirm, destructive = true)
            // Weight, not colour alone, tells the worse action apart: a tonal red container survives a themed palette.
            BingeTextButton(
                label = severeAction.label,
                onClick = severeAction.onClick,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
            )
        }
    }
}
