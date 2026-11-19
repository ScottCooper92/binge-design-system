@file:CatalogGroup("Text entry")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.component.BingeFloatingComposer
import com.binge.designsystem.preview.ScreenshotTheme

private const val SAMPLE_PLACEHOLDER = "Add a comment…"
private const val SAMPLE_SEND = "Send"
private const val SAMPLE_LONG_TEXT =
    "The subtitles drift out of sync about halfway through episode three, and they stay a second behind until the credits. " +
        "It happens on the TV and on the phone, so it is not the player. The file is the same one the server shows as direct play, " +
        "so I do not think a transcode is involved. I have not tried another release yet."

/**
 * The composer with nothing typed: the placeholder, a leading action and a Send that is not yet enabled. The field and the chip
 * work in the catalog app, and Send enables as soon as there is text.
 */
@Composable
fun BingeFloatingComposerSample() {
    var text by remember { mutableStateOf("") }
    ScreenshotTheme {
        BingeFloatingComposer(
            value = text,
            onValueChange = { text = it },
            onSend = { text = "" },
            placeholder = SAMPLE_PLACEHOLDER,
            sendDescription = SAMPLE_SEND,
            leadingActions = { BingeFilterChip(label = "Resolve", selected = false, onClick = {}) },
        )
    }
}

/** A short comment typed: Send is enabled. */
@Composable
fun BingeFloatingComposerTypedSample() {
    var text by remember { mutableStateOf("Fixed in the latest build.") }
    ScreenshotTheme {
        BingeFloatingComposer(
            value = text,
            onValueChange = { text = it },
            onSend = { text = "" },
            placeholder = SAMPLE_PLACEHOLDER,
            sendDescription = SAMPLE_SEND,
            leadingActions = { BingeFilterChip(label = "Resolve", selected = false, onClick = {}) },
        )
    }
}

/** A comment past five lines: the card stops growing and the field scrolls inside it. */
@Composable
fun BingeFloatingComposerLongTextSample() {
    var text by remember { mutableStateOf(SAMPLE_LONG_TEXT) }
    ScreenshotTheme {
        BingeFloatingComposer(
            value = text,
            onValueChange = { text = it },
            onSend = { text = "" },
            placeholder = SAMPLE_PLACEHOLDER,
            sendDescription = SAMPLE_SEND,
            leadingActions = { BingeFilterChip(label = "Resolve", selected = false, onClick = {}) },
        )
    }
}

/** A send in flight: Send shows its spinner and the field is held, so the text cannot change under it. */
@Composable
fun BingeFloatingComposerSendingSample() {
    ScreenshotTheme {
        BingeFloatingComposer(
            value = "Fixed in the latest build.",
            onValueChange = {},
            onSend = {},
            placeholder = SAMPLE_PLACEHOLDER,
            sendDescription = SAMPLE_SEND,
            readOnly = true,
            sending = true,
            leadingActions = { BingeFilterChip(label = "Resolve", selected = false, onClick = {}) },
        )
    }
}

/**
 * With the keyboard open: a tall window with a plate where the keyboard sits, and the card above it. The card reads the
 * keyboard as a bottom inset, so this frame gives it one, as the window would.
 */
@Composable
fun BingeFloatingComposerKeyboardOpenSample() {
    var text by remember { mutableStateOf("Fixed in the latest") }
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SAMPLE_WINDOW_HEIGHT)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SAMPLE_KEYBOARD_HEIGHT)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            BingeFloatingComposer(
                value = text,
                onValueChange = { text = it },
                onSend = { text = "" },
                placeholder = SAMPLE_PLACEHOLDER,
                sendDescription = SAMPLE_SEND,
                windowInsets = WindowInsets(bottom = SAMPLE_KEYBOARD_HEIGHT),
                leadingActions = { BingeFilterChip(label = "Resolve", selected = false, onClick = {}) },
            )
        }
    }
}

private val SAMPLE_WINDOW_HEIGHT = 560.dp
private val SAMPLE_KEYBOARD_HEIGHT = 280.dp
