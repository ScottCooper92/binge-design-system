package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.binge.designsystem.component.CheckboxRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [CheckboxRow] — a single row, unchecked, no subtitle. See the convention KDoc
 * on [MediaCardRatedSample].
 */
@Composable
fun CheckboxRowSample() {
    ScreenshotTheme {
        CheckboxRow(label = "Season 1", checked = false, onToggle = {})
    }
}

/**
 * A checklist long enough to show why [CheckboxRow] exists at all: dividers between rows, a
 * subtitle on one, and a checked-but-disabled row with a trailing status — the "already have this
 * one" case a season picker or a candidate list both need.
 */
@Composable
fun CheckboxRowListSample() {
    ScreenshotTheme {
        Column {
            val labels = listOf("Season 1", "Season 2", "Season 3", "Season 4", "Season 5")
            labels.forEachIndexed { index, label ->
                when (index) {
                    1 ->
                        CheckboxRow(
                            label = label,
                            subtitle = "10 episodes",
                            checked = true,
                            onToggle = {},
                            showDivider = index < labels.lastIndex,
                        )
                    3 ->
                        CheckboxRow(
                            label = label,
                            checked = true,
                            enabled = false,
                            onToggle = {},
                            showDivider = index < labels.lastIndex,
                            trailingContent = {
                                Text(
                                    text = "Available",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            },
                        )
                    else ->
                        CheckboxRow(
                            label = label,
                            checked = false,
                            onToggle = {},
                            showDivider = index < labels.lastIndex,
                        )
                }
            }
        }
    }
}
