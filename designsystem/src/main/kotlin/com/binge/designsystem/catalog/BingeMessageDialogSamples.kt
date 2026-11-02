@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeMessageDialogContent
import com.binge.designsystem.preview.ScreenshotTheme

private const val TITLE = "The backup wasn't made"
private const val MESSAGE = "The server may need 5 GiB free where it keeps backups. Free up space there, then try again."

/** A message dialog: what happened, the cause and the next step, and one button that acknowledges it. */
@Composable
fun BingeMessageDialogSample() {
    ScreenshotTheme {
        BingeMessageDialogContent(title = TITLE, message = MESSAGE, onDismiss = {})
    }
}

/** A message long enough to run past the body's height: it scrolls inside the dialog instead of clipping. */
@Composable
fun BingeMessageDialogLongSample() {
    ScreenshotTheme {
        BingeMessageDialogContent(
            title = TITLE,
            message = (1..LONG_PARAGRAPHS).joinToString("\n\n") { "$MESSAGE The log has the full list of what was checked." },
            onDismiss = {},
        )
    }
}

private const val LONG_PARAGRAPHS = 6
