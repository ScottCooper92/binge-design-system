package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeBottomSheet
import com.binge.designsystem.component.BingeConfirmDialog
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeModalSideSheet
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Live demos for the modal components, which the screenshot lane cannot capture (see the convention
 * on `…Demo()` in the catalog app). Each opens the real component, in its own window, from a button
 * and reports how it was dismissed, so the scrim and the dismissal paths can be tried by hand.
 */
@Composable
private fun ModalDemoHost(
    trigger: String,
    outcome: String,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        BingeFilledButton(label = trigger, onClick = onOpen)
        Text(outcome)
    }
}

/** The bottom sheet, opened for real: dismiss it by dragging, tapping the scrim, back, or Done. */
@Composable
fun BingeBottomSheetDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf(false) }
        var outcome by remember { mutableStateOf("Not opened yet") }
        ModalDemoHost(trigger = "Open bottom sheet", outcome = outcome, onOpen = { open = true })
        if (open) {
            BingeBottomSheet(onDismissRequest = {
                open = false
                outcome = "Dismissed by gesture, scrim or back"
            }) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                ) {
                    Text("A real modal bottom sheet")
                    BingeTextButton(label = "Done", onClick = {
                        open = false
                        outcome = "Dismissed by Done"
                    })
                }
            }
        }
    }
}

/** The confirm dialog, opened for real, reporting whether it was confirmed or cancelled. */
@Composable
fun BingeConfirmDialogDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf(false) }
        var outcome by remember { mutableStateOf("Not opened yet") }
        ModalDemoHost(trigger = "Open confirm dialog", outcome = outcome, onOpen = { open = true })
        if (open) {
            BingeConfirmDialog(
                title = "Delete list?",
                message = "This removes the list and everything in it.",
                confirmLabel = "Delete",
                destructive = true,
                onConfirm = {
                    open = false
                    outcome = "Confirmed"
                },
                onDismiss = {
                    open = false
                    outcome = "Cancelled"
                },
            )
        }
    }
}

/** The modal side sheet, opened for real: dismiss it with back or its own Close button. */
@Composable
fun BingeModalSideSheetDemo() {
    ScreenshotTheme {
        var open by remember { mutableStateOf(false) }
        var outcome by remember { mutableStateOf("Not opened yet") }
        ModalDemoHost(trigger = "Open side sheet", outcome = outcome, onOpen = { open = true })
        if (open) {
            BingeModalSideSheet(onDismissRequest = {
                open = false
                outcome = "Dismissed"
            }) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
                ) {
                    Text("A real modal side sheet")
                    BingeTextButton(label = "Close", onClick = {
                        open = false
                        outcome = "Closed by button"
                    })
                }
            }
        }
    }
}
