package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import com.binge.designsystem.R

/**
 * A dialog that tells the user why something failed and what they can do about it: a [title] saying what happened, a [message]
 * with the cause and the next step, and one text button that acknowledges it. It is an acknowledgement, not a confirmation, so
 * it has exactly one action; for a choice use [BingeConfirmDialog]. It does not go away by itself. [onDismiss] runs for the
 * button, Back and a tap outside. A long [message] scrolls rather than clipping.
 *
 * With [dismissible] false, only the button closes it: Back and a tap outside do nothing. For a message the user must not
 * lose by a stray tap, such as a secret shown once (an API key). The button then needs a label that says what closing means.
 *
 * Use it for any message the user must read to act, however short. A result that needs no action is a snackbar: see
 * `docs/messages.md`. The visible body is [BingeMessageDialogContent], so screenshot tests can render it.
 */
@Composable
fun BingeMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.action_ok),
    dismissible: Boolean = true,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        properties = DialogProperties(dismissOnBackPress = dismissible, dismissOnClickOutside = dismissible),
        title = { Text(title) },
        text = { MessageBody(message, Modifier.verticalScroll(rememberScrollState())) },
        confirmButton = { BingeTextButton(label = confirmLabel, onClick = onDismiss) },
    )
}

/** Stateless body of [BingeMessageDialog], apart from the modal window, so a screenshot can render it. */
@Composable
fun BingeMessageDialogContent(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmLabel: String = stringResource(R.string.action_ok),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        MessageBody(
            message,
            Modifier
                .heightIn(max = dimensionResource(R.dimen.message_dialog_body_max_height))
                .verticalScroll(rememberScrollState()),
        )
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
            BingeTextButton(label = confirmLabel, onClick = onDismiss)
        }
    }
}

@Composable
private fun MessageBody(message: String, modifier: Modifier) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
