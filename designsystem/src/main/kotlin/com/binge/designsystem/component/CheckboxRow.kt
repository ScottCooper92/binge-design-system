package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import com.binge.designsystem.R

/**
 * One row of a checklist — a leading [Checkbox], a [label] with an optional [subtitle], and an
 * optional [trailingContent] slot (a status chip, say) — sized and spaced for a long, scannable
 * list (a season picker, a candidate list) rather than a one-off toggle. Reach for this instead of
 * hand-rolling a checkbox [Row] per screen.
 *
 * Pass [showDivider] on every row but the list's last: a long run of otherwise-identical rows (a
 * 30-plus-season show) is what actually gets hard to track item-by-item, and a hairline separator
 * between rows is what fixes that — row height and padding alone don't, and this component's own
 * chrome stays the same length either way.
 *
 * The row owns the toggle semantics ([Role.Checkbox] via [Modifier.toggleable]) itself, which is
 * why the child [Checkbox]'s own `onCheckedChange` is `null` below — exactly one node should
 * announce state to a screen reader, not two.
 */
@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onToggle)
                .padding(
                    horizontal = dimensionResource(R.dimen.checkbox_row_padding_h),
                    vertical = dimensionResource(R.dimen.checkbox_row_padding_v),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.checkbox_row_gap)),
        ) {
            // The Row owns the toggle semantics; the checkbox is a visual indicator only.
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailingContent?.invoke()
        }
        if (showDivider) {
            HorizontalDivider(
                thickness = dimensionResource(R.dimen.hairline_thickness),
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.padding(start = dimensionResource(R.dimen.checkbox_row_padding_h)),
            )
        }
    }
}
