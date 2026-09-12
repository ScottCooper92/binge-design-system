package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * The dark snackbar pill across its three forms — message-only, with an accent action, and with a
 * trailing dismiss icon — on the light + dark colour axis.
 *
 * Targets the stateless [BingeSnackbar] directly: an M3 [androidx.compose.material3.SnackbarHost]
 * is empty at frame 0, so [BingeSnackbarHost] itself would screenshot blank.
 */
class BingeSnackbarHostScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MessageOnly() {
        ScreenshotTheme {
            BingeSnackbar(
                message = "Added to watchlist",
                actionLabel = null,
                onActionClick = {},
                showDismissAction = false,
                onDismiss = {},
            )
        }
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun WithListAction() {
        ScreenshotTheme {
            BingeSnackbar(
                message = "Created My favourites",
                actionLabel = "View list",
                onActionClick = {},
                showDismissAction = false,
                onDismiss = {},
            )
        }
    }
}
