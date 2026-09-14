package com.binge.designsystem.component

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * The snackbar pill's dimensional cases on the light + dark colour axis: one line and two, each with
 * a trailing control and without one. The catalog samples carry the public one-line fixtures, so what
 * is here is the heights — 48dp where the button governs, 68dp where the message does.
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

    /**
     * Two lines, the most [SNACKBAR_MAX_LINES] allows. The text's own vertical inset is what carries
     * the pill past its 48dp minimum to M3's 68dp here, so this is the frame that holds that number.
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TwoLineMessage() {
        ScreenshotTheme {
            BingeSnackbar(
                message = "Couldn't add that to your watchlist because your session has expired. Sign in again.",
                actionLabel = null,
                onActionClick = {},
                showDismissAction = false,
                onDismiss = {},
            )
        }
    }

    /**
     * Two lines *and* a trailing control — the combination nothing had rendered. The message
     * carries the pill to 68dp while the button keeps its own 48dp, so this is the frame that shows
     * what `CenterVertically` does with the two of them.
     */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TwoLineWithAction() {
        ScreenshotTheme {
            BingeSnackbar(
                message = "Couldn't add that to your watchlist because your session has expired. Sign in again.",
                actionLabel = "Sign in",
                onActionClick = {},
                showDismissAction = false,
                onDismiss = {},
            )
        }
    }

    /** And the same against the dismiss icon, whose 48dp target is squarer than the text button's. */
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun TwoLineWithDismiss() {
        ScreenshotTheme {
            BingeSnackbar(
                message = "Couldn't add that to your watchlist because your session has expired. Sign in again.",
                actionLabel = null,
                onActionClick = {},
                showDismissAction = true,
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
