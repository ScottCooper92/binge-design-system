@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import com.binge.designsystem.component.BingeMessageDialogContent
import com.binge.designsystem.component.BingeOutlinedButton
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

/**
 * A secret shown once, the case for a dialog that only its button closes (`dismissible = false`): a long key with no spaces,
 * which must wrap rather than clip, and a label that says what closing means. A frame cannot show that Back and a tap outside
 * do nothing; the live demo does.
 */
@Composable
fun BingeMessageDialogShownOnceSample() {
    ScreenshotTheme {
        BingeMessageDialogContent(
            title = "Copy your API key now",
            message = SHOWN_ONCE_MESSAGE,
            confirmLabel = "I have copied it",
            onDismiss = {},
        )
    }
}

internal const val SHOWN_ONCE_MESSAGE =
    "This is the only time it is shown.\n\njf_7c1e9a40b2d84f6a9e3b5c0d1f2a8b647c1e9a40b2d84f6a9e3b5c0d1f2a8b64"

private const val SHOWN_ONCE_KEY = "jf_7c1e9a40b2d84f6a9e3b5c0d1f2a8b647c1e9a40b2d84f6a9e3b5c0d1f2a8b64"

/**
 * The same secret with the thing the user does with it in the dialog's [extraContent]: the key itself and a Copy button, under a
 * short message. The button is the caller's, so it owns what Copy does; here it says "Copied" once pressed.
 */
@Composable
fun BingeMessageDialogKeyWithCopySample() {
    ScreenshotTheme {
        BingeMessageDialogContent(
            title = "Copy your API key now",
            message = "This is the only time it is shown.",
            confirmLabel = "I have copied it",
            onDismiss = {},
            extraContent = { ShownOnceKey() },
        )
    }
}

/** A shown-once key in monospace, wrapping rather than clipping, with a Copy button that confirms in its own label. */
@Composable
internal fun ShownOnceKey() {
    var copied by remember { mutableStateOf(false) }
    Text(text = SHOWN_ONCE_KEY, style = MaterialTheme.typography.bodyMedium, fontFamily = FontFamily.Monospace)
    BingeOutlinedButton(label = if (copied) "Copied" else "Copy", onClick = { copied = true })
}
