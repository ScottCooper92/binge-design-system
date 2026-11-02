@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.binge.designsystem.component.BingeConfirmDialogContent
import com.binge.designsystem.component.bingeSwitchColors
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Confirm-dialog body in its default tone. Targets the stateless [BingeConfirmDialogContent]; the
 * outer `BingeConfirmDialog` is an `AlertDialog` (modal window) that doesn't capture in screenshots.
 */
@Composable
fun BingeConfirmDialogSample() {
    ScreenshotTheme {
        BingeConfirmDialogContent(
            title = "Remove from list?",
            message = "This title will be removed from the list. You can add it again later.",
            confirmLabel = "Remove",
            onConfirm = {},
            onDismissRequest = {},
        )
    }
}

/** Destructive tone — the confirm text takes the error colour for irreversible actions. */
@Composable
fun BingeConfirmDialogDestructiveSample() {
    ScreenshotTheme {
        BingeConfirmDialogContent(
            title = "Sign out?",
            message = "You'll need to sign in again to access your account.",
            confirmLabel = "Sign out",
            onConfirm = {},
            onDismissRequest = {},
            destructive = true,
        )
    }
}

/** A milder or a more severe outcome as a switch in the body, off: the confirm is the milder one. */
@Composable
fun BingeConfirmDialogSwitchOffSample() {
    SwitchDialogFrame(initiallyOn = false)
}

/** The switch on: the message says what it adds, and the confirm's label follows it. Still two actions on one row. */
@Composable
fun BingeConfirmDialogSwitchOnSample() {
    SwitchDialogFrame(initiallyOn = true)
}

/** The switch dialog with the long labels a large font or a translation brings: the actions stay on one row, each wrapping inside its button. */
@Composable
fun BingeConfirmDialogSwitchWorstCaseSample() {
    SwitchDialogFrame(initiallyOn = true, longLabels = true)
}

@Composable
private fun SwitchDialogFrame(initiallyOn: Boolean, longLabels: Boolean = false) {
    // Each sample starts at its own state, which is all a frame sees, and then toggles for real.
    var deleteFiles by remember { mutableStateOf(initiallyOn) }
    val milder = if (longLabels) "Remove the request only" else "Remove request"
    val severe = if (longLabels) "Remove it and delete the files from the server" else "Remove and delete files"
    ScreenshotTheme {
        BingeConfirmDialogContent(
            title = "Remove this request?",
            message =
                if (deleteFiles) {
                    "The request and its files on the server are removed. This cannot be undone."
                } else {
                    "The request is removed. Its files stay on the server."
                },
            confirmLabel = if (deleteFiles) severe else milder,
            dismissLabel = if (longLabels) "Keep the request" else "Cancel",
            onConfirm = {},
            onDismissRequest = {},
            destructive = true,
            extraContent = {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .toggleable(value = deleteFiles, role = Role.Switch, onValueChange = { deleteFiles = it }),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Also delete the files from the server", modifier = Modifier.weight(1f))
                    Switch(checked = deleteFiles, onCheckedChange = null, colors = bingeSwitchColors())
                }
            },
        )
    }
}
