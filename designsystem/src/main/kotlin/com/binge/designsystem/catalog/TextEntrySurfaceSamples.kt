package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.TextEntrySurface
import com.binge.designsystem.preview.ScreenshotTheme

private const val ENTRY_TITLE = "Add a note"
private const val ENTRY_VALUE = "Looks great on the 4K release."
private const val ENTRY_HINT = "Write something…"
private const val SUBMIT_LABEL = "Save"
private const val ENTRY_MAX_LENGTH = 120

/**
 * Public samples for [TextEntrySurface] — the host-agnostic title + field + cancel/submit surface
 * (see the convention on [MediaCardRatedSample]). Default, error, and counter renders cover the three
 * states of the field's support slot.
 */
@Composable
fun TextEntrySurfaceSample() {
    ScreenshotTheme {
        TextEntrySurface(
            title = ENTRY_TITLE,
            value = ENTRY_VALUE,
            onValueChange = {},
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
    ScreenshotTheme {
        TextEntrySurface(
            title = ENTRY_TITLE,
            value = ENTRY_VALUE,
            onValueChange = {},
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
    ScreenshotTheme {
        TextEntrySurface(
            title = ENTRY_TITLE,
            value = ENTRY_VALUE,
            onValueChange = {},
            onSubmit = {},
            onCancel = {},
            submitLabel = SUBMIT_LABEL,
            hint = ENTRY_HINT,
            maxLength = ENTRY_MAX_LENGTH,
        )
    }
}
