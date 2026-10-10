package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeInitialsAvatar
import com.binge.designsystem.component.CheckboxRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public sample for [CheckboxRow] — a single row, unchecked, no subtitle. See the convention KDoc
 * on [MediaCardRatedSample].
 */
@Composable
fun CheckboxRowSample() {
    var checked by remember { mutableStateOf(false) }
    ScreenshotTheme {
        CheckboxRow(label = "Season 1", checked = checked, onToggle = { checked = it })
    }
}

/**
 * A checklist long enough to show why [CheckboxRow] exists at all: dividers between rows, a
 * subtitle on one, and a checked-but-disabled row with a trailing status — the "already have this
 * one" case a season picker or a candidate list both need.
 */
@Composable
fun CheckboxRowListSample() {
    // Season 4 is disabled, so it keeps its seeded tick whatever is tapped.
    val checked = remember { mutableStateListOf(false, true, false, true, false) }
    ScreenshotTheme {
        Column {
            val labels = listOf("Season 1", "Season 2", "Season 3", "Season 4", "Season 5")
            labels.forEachIndexed { index, label ->
                when (index) {
                    1 ->
                        CheckboxRow(
                            label = label,
                            subtitle = "10 episodes",
                            checked = checked[index],
                            onToggle = { checked[index] = it },
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
                            checked = checked[index],
                            onToggle = { checked[index] = it },
                            showDivider = index < labels.lastIndex,
                        )
                }
            }
        }
    }
}

/** A label long enough to reach the trailing slot: the text stops the trailing gap short of it. */
@Composable
fun CheckboxRowLongLabelSample() {
    var checked by remember { mutableStateOf(true) }
    ScreenshotTheme {
        CheckboxRow(
            label = "Season 12: the one with the longest title in the list",
            checked = checked,
            onToggle = { checked = it },
            trailingContent = {
                Text(text = "Available", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            },
        )
    }
}

/** A checklist of people: each row carries the person's avatar between the checkbox and the name. */
@Composable
fun CheckboxRowPeopleSample() {
    val people = listOf("Noah Kim" to "noah@example.com", "Sana Ito" to "sana@example.com", "Raj Patel" to "raj@example.com")
    val checked = remember { mutableStateListOf(true, false, true) }
    ScreenshotTheme {
        Column {
            people.forEachIndexed { index, (name, email) ->
                CheckboxRow(
                    label = name,
                    subtitle = email,
                    checked = checked[index],
                    onToggle = { checked[index] = it },
                    showDivider = index < people.lastIndex,
                    leadingContent = { BingeInitialsAvatar(name = name, size = dimensionResource(R.dimen.item_group_icon_size)) },
                )
            }
        }
    }
}
