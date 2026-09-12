package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Dialog for naming and creating a new list. The caller keeps the dialog open for the whole
 * create→add round trip and drives [isSubmitting] from the in-flight signal, so the confirm action
 * shows a spinner instead of dismissing immediately — the caller dismisses on the success event.
 *
 * The visible body is delegated to [CreateListDialogContent] so it can be rendered directly in
 * screenshot tests, since the modal [AlertDialog] window itself does not capture in previews.
 */
@Composable
fun CreateListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isSubmitting: Boolean = false,
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_list_title)) },
        text = {
            CreateListNameField(
                name = name,
                onNameChange = { name = it },
                isSubmitting = isSubmitting,
            )
        },
        confirmButton = {
            CreateListActions(
                name = name,
                isSubmitting = isSubmitting,
                onConfirm = onConfirm,
                onDismiss = onDismiss,
            )
        },
    )
}

/** Stateless body of [CreateListDialog], split out so screenshot tests can render it directly. */
@Composable
internal fun CreateListDialogContent(
    name: String,
    onNameChange: (String) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    isSubmitting: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        Text(
            text = stringResource(R.string.create_list_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        CreateListNameField(
            name = name,
            onNameChange = onNameChange,
            isSubmitting = isSubmitting,
        )
        CreateListActions(
            name = name,
            isSubmitting = isSubmitting,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun CreateListNameField(
    name: String,
    onNameChange: (String) -> Unit,
    isSubmitting: Boolean,
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        label = { Text(stringResource(R.string.create_list_name_label)) },
        singleLine = true,
        enabled = !isSubmitting,
    )
}

@Composable
private fun CreateListActions(
    name: String,
    isSubmitting: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val trimmed = name.trim()
    val canSubmit = trimmed.isNotEmpty() && !isSubmitting
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(R.dimen.padding_s),
            Alignment.End,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BingeTextButton(
            label = stringResource(R.string.create_list_cancel),
            onClick = onDismiss,
            enabled = !isSubmitting,
        )
        BingeTextButton(
            label = stringResource(R.string.create_list_confirm),
            onClick = { if (canSubmit) onConfirm(trimmed) },
            enabled = canSubmit,
            loading = isSubmitting,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateListDialog() {
    BingeExpressiveTheme {
        CreateListDialogContent(name = "", onNameChange = {}, onConfirm = {}, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCreateListDialogSubmitting() {
    BingeExpressiveTheme {
        CreateListDialogContent(
            name = "Weekend watchlist",
            onNameChange = {},
            onConfirm = {},
            onDismiss = {},
            isSubmitting = true,
        )
    }
}
