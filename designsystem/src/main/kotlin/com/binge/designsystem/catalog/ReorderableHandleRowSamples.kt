package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.component.ReorderableHandleRow
import com.binge.designsystem.component.ReorderableSwitchRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for the shared [ReorderableHandleRow] primitive — the screenshot test renders these,
 * the row's one public fixture. [handleModifier] is plain `Modifier` here rather than a real
 * `longPressDraggableHandle()`: these samples show the row's static appearance, not live drag, which
 * needs a `ReorderableCollectionItemScope` only a real reorderable `LazyColumn` provides.
 */
@Composable
fun ReorderableHandleRowSample() {
    ScreenshotTheme {
        ReorderableHandleRow(
            handleModifier = Modifier,
            onMoveUp = {},
            onMoveDown = {},
        ) {
            Text(
                text = "Action",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** The list-start case: no Move up action, since there is nothing above this row to trade places with. */
@Composable
fun ReorderableHandleRowFirstSample() {
    ScreenshotTheme {
        ReorderableHandleRow(
            handleModifier = Modifier,
            onMoveUp = null,
            onMoveDown = {},
        ) {
            Text(
                text = "Action",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * [ReorderableSwitchRow] in its two forms. The first is an item with an editor: a tap on the row opens it and only the
 * switch changes it. The second, a built-in item, has no editor, so the whole row toggles. The first row is at the top
 * of the list, so it has no Move up. The drag handle is a plain modifier here, as in [ReorderableHandleRowSample].
 */
@Composable
fun ReorderableSwitchRowSample() {
    var first by remember { mutableStateOf(true) }
    var second by remember { mutableStateOf(false) }
    ScreenshotTheme {
        Column {
            ReorderableSwitchRow(
                label = "Recently added",
                caption = "Movies · Popular",
                icon = Icons.Filled.ViewCarousel,
                checked = first,
                onCheckedChange = { first = it },
                handleModifier = Modifier,
                onMoveUp = null,
                onMoveDown = {},
                onClick = {},
            )
            ReorderableSwitchRow(
                label = "Upcoming",
                icon = Icons.Filled.ViewCarousel,
                checked = second,
                onCheckedChange = { second = it },
                handleModifier = Modifier,
                onMoveUp = {},
                onMoveDown = null,
            )
        }
    }
}
