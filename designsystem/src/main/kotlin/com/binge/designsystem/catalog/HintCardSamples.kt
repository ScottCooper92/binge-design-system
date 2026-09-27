package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
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
