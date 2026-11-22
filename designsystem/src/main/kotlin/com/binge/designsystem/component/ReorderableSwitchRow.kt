package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow

/**
 * A [ReorderableHandleRow] holding a named, switchable item: an optional [icon] plate, the [label] with a [caption] under
 * it, and a [Switch] before the drag handle. It keeps the handle row's Move up and Move down actions, so [onMoveUp] and
 * [onMoveDown] are null at the ends of the list, and [handleModifier] is the caller's own drag handle.
 *
 * Who owns the tap depends on [onClick]. With none, the whole row toggles the switch, and the switch inside it takes no
 * tap of its own, so a screen reader announces one control. With one, a tap on the row runs it (open an editor, say) and
 * the switch alone changes [checked], as it must stay reachable.
 *
 * @need let the user reorder a list of items and switch each one on or off
 * @group rows
 * @replaces a hand-built reorderable row with a switch
 */
@Composable
fun ReorderableSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    handleModifier: Modifier,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    modifier: Modifier = Modifier,
    caption: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val tap =
        if (onClick != null) {
            Modifier.clickable(enabled = enabled, onClick = onClick)
        } else {
            Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
        }
    ReorderableHandleRow(
        handleModifier = handleModifier,
        onMoveUp = onMoveUp,
        onMoveDown = onMoveDown,
        modifier = modifier.then(tap),
    ) {
        RowBody(label = label, caption = caption, icon = icon)
        Switch(
            checked = checked,
            onCheckedChange = if (onClick != null) onCheckedChange else null,
            enabled = enabled,
            colors = bingeSwitchColors(),
        )
    }
}

@Composable
private fun RowScope.RowBody(
    label: String,
    caption: String?,
    icon: ImageVector?,
) {
    if (icon != null) BingeIconPlate(icon)
    Column(modifier = Modifier.weight(1f)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
