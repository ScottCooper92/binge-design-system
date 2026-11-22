package com.binge.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One entry of a [BingeOverflowMenu]. [icon] is a Filled glyph. A [destructive] entry is drawn last and in the error
 * colour, whatever place the caller gave it; a disabled one is dimmed and takes no tap.
 */
@Immutable
class OverflowItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * An anchored overflow menu: a Material 3 [DropdownMenu] of [items], each a label with a Filled icon. A tap runs the
 * item's action and then closes the menu through [onDismissRequest]. Destructive items sort after the rest, in the
 * order given, and are red, so the dangerous choice is never the first one under the thumb.
 *
 * Put it inside the same `Box` as the button that opens it, so it anchors there. A menu of choices that set a value is
 * a [BingeChoiceSheet] instead; this is for actions on the current page.
 *
 * @need let the user pick one of a few actions from a more button, with the destructive one last
 * @group bars
 * @sample BingeOverflowMenuSample
 * @replaces a hand-built DropdownMenu with its own error colouring
 */
@Composable
fun BingeOverflowMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<OverflowItem>,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest, modifier = modifier) {
        OverflowMenuItems(items = items, onDismissRequest = onDismissRequest)
    }
}

@Composable
internal fun ColumnScope.OverflowMenuItems(items: List<OverflowItem>, onDismissRequest: () -> Unit) {
    val (destructive, plain) = items.partition { it.destructive }
    (plain + destructive).forEach { item ->
        val colour = if (item.destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        DropdownMenuItem(
            text = { Text(item.label) },
            leadingIcon = { Icon(item.icon, contentDescription = null) },
            onClick = {
                onDismissRequest()
                item.onClick()
            },
            enabled = item.enabled,
            colors =
                MenuDefaults.itemColors(
                    textColor = colour,
                    leadingIconColor = colour,
                ),
        )
    }
}
