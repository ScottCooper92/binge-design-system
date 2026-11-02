package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R

/**
 * One toggle: a [label] with an optional [supportingText], and a [Switch] at the end, for a single setting or an option a
 * dialog applies, such as the switch in [BingeConfirmDialog]'s `extraContent` that offers a more severe outcome. For a
 * list of choices to tick, use [CheckboxRow].
 *
 * The row owns the toggle semantics ([Role.Switch] through [Modifier.toggleable]) and is at least `min_touch_target` tall, so the
 * whole row is the target; the [Switch] itself is a visual indicator, its own `onCheckedChange` null, because exactly one node
 * should announce the state. M3 only gives a [Switch] a minimum touch size when it handles the change itself, so a bare switch in
 * a hand-rolled row is the 32dp the switch draws. The row has no horizontal padding: it takes the edges of the container it
 * sits in, so it lines up with the text above it; pad it with [modifier] where it stands alone. A [supportingText] is
 * [supportingMaxLines] lines, one by default: say what the switch does.
 */
@Composable
fun SwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    supportingMaxLines: Int = 1,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(R.dimen.min_touch_target))
            .toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(vertical = dimensionResource(R.dimen.checkbox_row_padding_v)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m)),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!supportingText.isNullOrBlank()) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = supportingMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = null, enabled = enabled, colors = bingeSwitchColors())
    }
}
