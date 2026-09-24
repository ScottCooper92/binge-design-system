package com.binge.designsystem.catalog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.component.ReorderableHandleRow
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
