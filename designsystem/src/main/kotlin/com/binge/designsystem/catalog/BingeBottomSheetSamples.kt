package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.binge.designsystem.component.TextEntrySurface
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeShapes

private const val SHEET_TITLE = "Add a note"
private const val SHEET_HINT = "Write something…"
private const val SUBMIT_LABEL = "Save"

/**
 * A [TextEntrySurface] inside `BingeBottomSheet` — the pairing `TextEntrySurface`'s own KDoc names
 * as its intended host, with no catalog frame anywhere before this (issue #35). `BingeBottomSheet`
 * wraps a `ModalBottomSheet`, a modal window that doesn't capture in screenshots, so this renders the
 * sheet's own resting chrome — container colour and top corner — directly around the field, the same
 * stateless-content shape [BingeConfirmDialogSample] and [CreateListDialogSample] use for their modal
 * wrappers. A static frame can't show the keyboard open; it's for the resting layout, not a
 * regression guard for a keyboard-jump bug.
 */
@Composable
fun BingeBottomSheetSample() {
    ScreenshotTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(BingeShapes.HeroTop)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            TextEntrySurface(
                title = SHEET_TITLE,
                value = "",
                onValueChange = {},
                onSubmit = {},
                onCancel = {},
                submitLabel = SUBMIT_LABEL,
                hint = SHEET_HINT,
            )
        }
    }
}
