package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.R

/**
 * A full-width row for a `sh.calvin.reorderable`-backed `LazyColumn`: [content] plus a trailing drag
 * handle. A drag gesture has no TalkBack equivalent, so the row also exposes Move up/Move down as
 * [CustomAccessibilityAction]s read from the screen reader's rotor — pass `null` for whichever is
 * unavailable at an end of the list.
 *
 * [handleModifier] is the caller's own `Modifier.draggableHandle()`/`longPressDraggableHandle()`: an
 * extension on `ReorderableCollectionItemScope`, only callable from inside that scope's lambda, so it
 * can't be built in here — this component takes no dependency on the reorderable library itself, so a
 * companion app can reuse the same row shell against its own list.
 */
@Composable
fun ReorderableHandleRow(
    handleModifier: Modifier,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val moveUpLabel = stringResource(R.string.cd_reorder_move_up)
    val moveDownLabel = stringResource(R.string.cd_reorder_move_down)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(R.dimen.min_touch_target))
            .semantics {
                customActions = listOfNotNull(
                    onMoveUp?.let { move ->
                        CustomAccessibilityAction(moveUpLabel) {
                            move()
                            true
                        }
                    },
                    onMoveDown?.let { move ->
                        CustomAccessibilityAction(moveDownLabel) {
                            move()
                            true
                        }
                    },
                )
            }.padding(horizontal = dimensionResource(R.dimen.padding_m)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        content()
        // clearAndSetSemantics: the handle's own node would otherwise announce as an unlabelled button
        // in the same row the Move up/Move down actions above already cover.
        IconButton(onClick = {}, modifier = handleModifier.clearAndSetSemantics {}) {
            Icon(
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = stringResource(R.string.cd_reorder_handle),
            )
        }
    }
}
