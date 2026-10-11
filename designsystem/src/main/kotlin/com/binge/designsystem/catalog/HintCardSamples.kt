@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.HintCard
import com.binge.designsystem.preview.ScreenshotTheme

/** A usage hint over a reorderable list — the default lightbulb icon, wrapping to two lines. */
@Composable
fun HintCardSample() {
    ScreenshotTheme {
        HintCard(
            text = "Default: the lightbulb icon, and text that wraps onto more lines once it runs past the " +
                "width of the card, like this.",
        )
    }
}

/** A one-line note about scope, with an info icon in place of the default. */
@Composable
fun HintCardInfoSample() {
    ScreenshotTheme {
        HintCard(text = "Info: any icon can stand in for the lightbulb.", icon = Icons.Filled.Info)
    }
}

/** The opt-in dismissible form — a close control in the top-right corner, over wrapping text. */
@Composable
fun HintCardDismissibleSample() {
    var visible by remember { mutableStateOf(true) }
    ScreenshotTheme {
        if (visible) {
            HintCard(
                text = "Dismissible: the close button in the corner hides the card, and longer text wraps clear " +
                    "of it.",
                onDismissRequest = { visible = false },
            )
        } else {
            TextButton(onClick = { visible = true }) { Text("Show the hint again") }
        }
    }
}

/** A dismissible hint with an action beneath it, trailed by an open-in-new glyph for an outside destination. */
@Composable
fun HintCardActionSample() {
    var visible by remember { mutableStateOf(true) }
    ScreenshotTheme {
        if (visible) {
            HintCard(
                text = "Action: a button under the text, here with the glyph for a destination outside the app.",
                onDismissRequest = { visible = false },
                actionLabel = "Open Binge",
                onAction = {},
                actionIcon = Icons.AutoMirrored.Filled.OpenInNew,
            )
        } else {
            TextButton(onClick = { visible = true }) { Text("Show the hint again") }
        }
    }
}
