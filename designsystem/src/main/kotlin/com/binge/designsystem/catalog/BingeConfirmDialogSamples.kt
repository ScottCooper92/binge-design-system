@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeConfirmDialogContent
import com.binge.designsystem.component.BingeConfirmSevereAction
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

/** Two destructive outcomes: the actions stack, Cancel first, and the worse one is set apart by a tonal red container. */
@Composable
fun BingeConfirmDialogSevereSample() {
    ScreenshotTheme {
        BingeConfirmDialogContent(
            title = "Remove this request?",
            message = "Removing it keeps the files on the server. Removing and deleting the files cannot be undone.",
            confirmLabel = "Remove request",
            onConfirm = {},
            onDismissRequest = {},
            destructive = true,
            severeAction = BingeConfirmSevereAction(label = "Remove and delete files", onClick = {}),
        )
    }
}
