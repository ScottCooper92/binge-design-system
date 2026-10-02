package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.HintCard
import com.binge.designsystem.preview.ScreenshotTheme

/** A usage hint over a reorderable list — the default lightbulb icon, wrapping to two lines. */
@Composable
fun HintCardSample() {
    ScreenshotTheme {
        HintCard(
            text = "Press and hold an item, then drag to reorder. Screen reader users can use the Move up and " +
                "Move down actions instead.",
        )
    }
}

/** A one-line note about scope, with an info icon in place of the default. */
@Composable
fun HintCardInfoSample() {
    ScreenshotTheme {
        HintCard(text = "Changes only affect this device.", icon = Icons.Filled.Info)
    }
}

/** The opt-in dismissible form — a close control in the top-right corner, over wrapping text. */
@Composable
fun HintCardDismissibleSample() {
    ScreenshotTheme {
        HintCard(
            text = "Press and hold an item, then drag to reorder. Screen reader users can use the Move up and " +
                "Move down actions instead.",
            onDismiss = {},
        )
    }
}

/** A dismissible hint with an action beneath it, trailed by an open-in-new glyph for an outside destination. */
@Composable
fun HintCardActionSample() {
    ScreenshotTheme {
        HintCard(
            text = "Connect this app to Binge to request titles from your library.",
            onDismiss = {},
            actionLabel = "Open Binge",
            onAction = {},
            actionIcon = Icons.AutoMirrored.Filled.OpenInNew,
        )
    }
}
