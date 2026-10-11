@file:CatalogGroup("Text entry")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.component.TextEntrySurface
import com.binge.designsystem.preview.ScreenshotTheme

private const val ENTRY_TITLE = "Add a note"
private const val ENTRY_HINT = "Write something…"
private const val SUBMIT_LABEL = "Save"
private const val ENTRY_MAX_LENGTH = 120
private const val COMMENT_TITLE = "Add a comment"
private const val COMMENT_SUBMIT_LABEL = "Comment"
private const val COMMENT_COLLAPSIBLE = "Collapsible: the button on the title row expands this surface to fill its host."
private const val COMMENT_LONG_TAIL =
    "Tried a fresh grab and it still desyncs. The audio is fine for the first few " +
        "minutes, then it drifts about half a second ahead and keeps sliding from there. " +
        "Happens on both the web player and the cast target, so it looks like the source " +
        "file rather than playback. Re-encoding the audio track or re-pulling the release " +
        "would probably fix it. Let me know if you want a sample clip."
private val ISSUE_TYPES = listOf("Video", "Audio", "Other")

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

/** Empty state: only the hint shows, and the submit button stays disabled until there is text. */
@Composable
fun TextEntrySurfaceEmptySample() {
    var value by remember { mutableStateOf("") }
    ScreenshotTheme {
        TextEntrySurface(
            title = COMMENT_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = COMMENT_SUBMIT_LABEL,
            hint = "Empty: only this hint shows, and Comment stays off until there is text.",
        )
    }
}

/** Submitting state: the field is disabled and the submit button shows its progress while the call is in flight. */
@Composable
fun TextEntrySurfaceSubmittingSample() {
    ScreenshotTheme {
        TextEntrySurface(
            title = "Edit comment",
            value = "Submitting: the field is locked while Save shows its progress.",
            onValueChange = {},
            onSubmit = {},
            onCancel = {},
            submitLabel = "Save",
            isSubmitting = true,
        )
    }
}

/** Long content: the field grows to its capped line count and scrolls the rest. */
@Composable
fun TextEntrySurfaceLongContentSample() {
    var value by remember { mutableStateOf("Long content: the field grows to its line cap, then scrolls the rest. $COMMENT_LONG_TAIL") }
    ScreenshotTheme {
        TextEntrySurface(
            title = COMMENT_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = COMMENT_SUBMIT_LABEL,
        )
    }
}

/** The header slot carrying a row of type chips between the title and the field, as an issue report uses it. */
@Composable
fun TextEntrySurfaceWithTypePickerSample() {
    var value by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(ISSUE_TYPES.first()) }
    ScreenshotTheme {
        TextEntrySurface(
            title = "Report an issue",
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = "Report",
            hint = "Header slot: the chips above sit between the title and this field.",
            header = {
                Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.chip_spacing))) {
                    ISSUE_TYPES.forEach { label ->
                        BingeFilterChip(label = label, selected = type == label, onClick = { type = label })
                    }
                }
            },
        )
    }
}

/** Collapsed with an `onExpandToggle`: the title row carries the expand affordance. Tapping it flips to the expanded form. */
@Composable
fun TextEntrySurfaceCollapsibleSample() {
    var value by remember { mutableStateOf(COMMENT_COLLAPSIBLE) }
    var expanded by remember { mutableStateOf(false) }
    ScreenshotTheme {
        TextEntrySurface(
            title = COMMENT_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = COMMENT_SUBMIT_LABEL,
            expanded = expanded,
            onExpandToggle = { expanded = !expanded },
        )
    }
}

/**
 * Expanded: the surface fills its host's bounded height, here a phone-sized one, and the field takes the
 * space left. The title row's affordance collapses it again.
 */
@Composable
fun TextEntrySurfaceExpandedSample() {
    var value by remember {
        mutableStateOf("Expanded: the surface fills its host's height, and the field takes the space left. $COMMENT_LONG_TAIL")
    }
    var expanded by remember { mutableStateOf(true) }
    ScreenshotTheme(modifier = Modifier.height(dimensionResource(R.dimen.text_entry_surface_sample_expanded_height))) {
        TextEntrySurface(
            title = COMMENT_TITLE,
            value = value,
            onValueChange = { value = it },
            onSubmit = {},
            onCancel = {},
            submitLabel = COMMENT_SUBMIT_LABEL,
            expanded = expanded,
            onExpandToggle = { expanded = !expanded },
        )
    }
}
