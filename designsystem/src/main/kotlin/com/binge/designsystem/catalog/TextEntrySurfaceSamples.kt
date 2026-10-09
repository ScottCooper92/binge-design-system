@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.binge.designsystem.component.TextEntrySurface
import com.binge.designsystem.preview.ScreenshotTheme

private const val ENTRY_TITLE = "Add a note"
private const val ENTRY_HINT = "Write something…"
private const val SUBMIT_LABEL = "Save"
private const val ENTRY_MAX_LENGTH = 120

/**
 * Public samples for [TextEntrySurface] — the host-agnostic title + field + cancel/submit surface
 * (see the convention on [MediaCardRatedSample]). Default, error, and counter renders cover the three
 * states of the field's support slot, and the masked one a secret. Each one's starting text says what
 * its state is (the masked one's title, since its text is hidden), so the sample needs no caption, and
 * each keeps its own text after that, so the field can be typed in on a device.
 */
@Composable
fun TextEntrySurfaceSample() {
    var value by remember { mutableStateOf("Default: type here to try the field. Save turns on once there is text.") }
    ScreenshotTheme {
        TextEntrySurface(
            title = ENTRY_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = SUBMIT_LABEL,
            hint = ENTRY_HINT,
        )
    }
}

/** Error state — an inline error message replaces the support slot and the field outline reddens. */
@Composable
fun TextEntrySurfaceErrorSample() {
    var value by remember { mutableStateOf("Error: the message below replaces the support line and the outline turns red.") }
    ScreenshotTheme {
        TextEntrySurface(
            title = ENTRY_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = SUBMIT_LABEL,
            hint = ENTRY_HINT,
            error = "Note couldn't be saved",
        )
    }
}

/** Counter state — a `maxLength` adds the trailing `n / max` character counter under the field. */
@Composable
fun TextEntrySurfaceCounterSample() {
    var value by remember { mutableStateOf("Counter: the count below tracks this text against its limit.") }
    ScreenshotTheme {
        TextEntrySurface(
            title = ENTRY_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = SUBMIT_LABEL,
            hint = ENTRY_HINT,
            maxLength = ENTRY_MAX_LENGTH,
        )
    }
}

/**
 * Masked state: a secret's value is drawn as dots, on one line, with the password keyboard. The title says what is
 * masked, since the value itself cannot.
 */
@Composable
fun TextEntrySurfaceMaskedSample() {
    var value by remember { mutableStateOf("4f9c2a7e1b") }
    ScreenshotTheme {
        TextEntrySurface(
            title = "Masked: an API key, drawn as dots",
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = SUBMIT_LABEL,
            minLines = 1,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
    }
}
