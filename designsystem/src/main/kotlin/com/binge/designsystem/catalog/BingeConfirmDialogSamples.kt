package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeConfirmDialogContent
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
            onDismiss = {},
        )
    }
}

/** Destructive tone — the confirm action is tinted with the error colour for irreversible actions. */
@Composable
fun BingeConfirmDialogDestructiveSample() {
    ScreenshotTheme {
        BingeConfirmDialogContent(
            title = "Sign out?",
            message = "You'll need to sign in again to access your account.",
            confirmLabel = "Sign out",
            onConfirm = {},
            onDismiss = {},
            destructive = true,
        )
    }
}
