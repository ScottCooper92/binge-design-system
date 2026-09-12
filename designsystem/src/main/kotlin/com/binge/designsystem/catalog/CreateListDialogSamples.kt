package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.CreateListDialogContent
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Create-list dialog body in its empty state. Targets the stateless [CreateListDialogContent]; the
 * outer `CreateListDialog` is an `AlertDialog` (modal window) that doesn't capture in screenshots.
 */
@Composable
fun CreateListDialogSample() {
    ScreenshotTheme {
        CreateListDialogContent(
            name = "",
            onNameChange = {},
            onConfirm = {},
            onDismiss = {},
        )
    }
}

/** Submitting state — the confirm action shows a spinner while the create→add round trip runs. */
@Composable
fun CreateListDialogSubmittingSample() {
    ScreenshotTheme {
        CreateListDialogContent(
            name = "Weekend watchlist",
            onNameChange = {},
            onConfirm = {},
            onDismiss = {},
            isSubmitting = true,
        )
    }
}
