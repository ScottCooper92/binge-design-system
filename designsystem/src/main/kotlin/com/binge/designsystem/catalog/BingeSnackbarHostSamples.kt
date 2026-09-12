package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeSnackbar
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Binge's snackbar pill with an accent action. Targets the stateless [BingeSnackbar] directly: an
 * M3 `SnackbarHost` is empty at frame 0, so `BingeSnackbarHost` itself would screenshot blank.
 */
@Composable
fun BingeSnackbarSample() {
    ScreenshotTheme {
        BingeSnackbar(
            message = "Removed from watchlist",
            actionLabel = "Undo",
            onActionClick = {},
            showDismissAction = false,
            onDismiss = {},
        )
    }
}

/** Message-only with a trailing dismiss icon — the error/no-action form. */
@Composable
fun BingeSnackbarDismissSample() {
    ScreenshotTheme {
        BingeSnackbar(
            message = "Couldn't complete that action. Please try again.",
            actionLabel = null,
            onActionClick = {},
            showDismissAction = true,
            onDismiss = {},
        )
    }
}
