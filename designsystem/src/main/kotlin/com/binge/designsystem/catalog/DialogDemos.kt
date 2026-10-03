@file:OnePerScreen
@file:CatalogGroup("Dialogs")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeConfirmDialog
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.CreateListDialog
import com.binge.designsystem.preview.ScreenshotTheme
import kotlinx.coroutines.delay

private const val SUBMIT_DEMO_MILLIS = 2_000L

private enum class OpenDialog { Confirm, Destructive, CreateList }

/**
 * Every dialog in the design system, each opened for real from its own button, which a screenshot of
 * the dialog's body cannot show: the window, the scrim, and the ways out. The line below the buttons
 * reports how the last one closed. Creating a list runs its submitting state for two seconds first.
 */
@Composable
fun DialogsDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf<OpenDialog?>(null) }
        var outcome by remember { mutableStateOf("Not opened yet") }
        var submitting by remember { mutableStateOf(false) }

        fun close(how: String) {
            open = null
            submitting = false
            outcome = how
        }
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeOutlinedButton(label = "Confirm dialog", onClick = { open = OpenDialog.Confirm })
            BingeOutlinedButton(label = "Destructive confirm dialog", onClick = { open = OpenDialog.Destructive })
            BingeOutlinedButton(label = "Create list dialog", onClick = { open = OpenDialog.CreateList })
            Text(outcome)
        }
        when (open) {
            OpenDialog.Confirm ->
                BingeConfirmDialog(
                    title = "Remove from list?",
                    message = "This title will be removed from the list. You can add it again later.",
                    confirmLabel = "Remove",
                    onConfirm = { close("Confirmed") },
                    onDismiss = { close("Cancelled") },
                )
            OpenDialog.Destructive ->
                BingeConfirmDialog(
                    title = "Delete list?",
                    message = "This removes the list and everything in it.",
                    confirmLabel = "Delete",
                    destructive = true,
                    onConfirm = { close("Confirmed") },
                    onDismiss = { close("Cancelled") },
                )
            OpenDialog.CreateList -> {
                CreateListDialog(
                    onDismiss = { close("Cancelled") },
                    onConfirm = { name ->
                        submitting = true
                        outcome = "Creating “$name”"
                    },
                    isSubmitting = submitting,
                )
                if (submitting) {
                    LaunchedEffect(Unit) {
                        delay(SUBMIT_DEMO_MILLIS)
                        submitting = false
                        close("Created")
                    }
                }
            }
            null -> Unit
        }
    }
}
