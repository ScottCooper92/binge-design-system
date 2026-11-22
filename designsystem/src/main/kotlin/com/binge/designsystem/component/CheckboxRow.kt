package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.R

/**
 * One row of a checklist — a leading [Checkbox], an optional [leadingContent] (a person's avatar, say), a [label]
 * with an optional [supportingText], and an optional [trailingContent] slot (a status chip, say) — sized and spaced for a
 * long, scannable list (a season picker, a candidate list) rather than a one-off toggle. Reach for this instead of
 * hand-rolling a checkbox [Row] per screen.
 *
 * The [supportingText] is [supportingMaxLines] lines, one by default, ellipsized: say what ticking the row does, in a few words. Raise [supportingMaxLines] for a row whose line has to be read in full. An [icon] leads the label on the same tinted plate a settings row uses, tinted [iconTint] or `primary`. A
 * [leadingContent] replaces it when the row needs something else, such as an avatar.
 *
 * The row merges its descendants' semantics, so a decorative [leadingContent] such as an avatar should clear its own
 * (`Modifier.clearAndSetSemantics {}`) when the [label] already names what it shows.
 *
 * Pass [showDivider] on every row but the list's last: a long run of otherwise-identical rows (a
 * 30-plus-season show) is what actually gets hard to track item-by-item, and a hairline separator
 * between rows is what fixes that — row height and padding alone don't, and this component's own
 * chrome stays the same length either way.
 *
 * The row owns the toggle semantics ([Role.Checkbox] via [Modifier.toggleable]) itself, which is
 * why the child [Checkbox]'s own `onCheckedChange` is `null` below — exactly one node should
 * announce state to a screen reader, not two.
 *
 * @need pick several items from a long checklist
 * @group inputs
 * @replaces a hand-built Row of a checkbox and text
 */
@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    supportingMaxLines: Int = 1,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    CheckRowLayout(
        label = label,
        toggle = Modifier.toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange),
        indicator = { Checkbox(checked = checked, onCheckedChange = null, enabled = enabled) },
        modifier = modifier,
        supportingText = supportingText,
        enabled = enabled,
        showDivider = showDivider,
        icon = icon,
        iconTint = iconTint,
        supportingMaxLines = supportingMaxLines,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
    )
}

/**
 * [CheckboxRow] for a row that stands for a group: [state] is On when every item under it is ticked, Off when none is, and
 * Indeterminate when some are. A tap calls [onClick], and the caller decides what the next state is, usually all on from
 * Off or Indeterminate and all off from On. Everything else is [CheckboxRow]'s: the same chrome, divider, icon and slots.
 *
 * @need let one row tick or clear a whole group of items, showing when only some are ticked
 * @group inputs
 * @replaces a hand-built row with a tri-state checkbox
 */
@Composable
fun TriStateCheckboxRow(
    label: String,
    state: ToggleableState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    supportingMaxLines: Int = 1,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    CheckRowLayout(
        label = label,
        toggle = Modifier.triStateToggleable(state = state, enabled = enabled, role = Role.Checkbox, onClick = onClick),
        indicator = { TriStateCheckbox(state = state, onClick = null, enabled = enabled) },
        modifier = modifier,
        supportingText = supportingText,
        enabled = enabled,
        showDivider = showDivider,
        icon = icon,
        iconTint = iconTint,
        supportingMaxLines = supportingMaxLines,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
    )
}

/** The row both forms draw: [toggle] owns the semantics and the tap, [indicator] is only the box. */
@Composable
private fun CheckRowLayout(
    label: String,
    toggle: Modifier,
    indicator: @Composable () -> Unit,
    modifier: Modifier,
    supportingText: String?,
    enabled: Boolean,
    showDivider: Boolean,
    icon: ImageVector?,
    iconTint: Color?,
    supportingMaxLines: Int,
    leadingContent: (@Composable () -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .then(toggle)
                .padding(
                    horizontal = dimensionResource(R.dimen.checkbox_row_padding_h),
                    vertical = dimensionResource(R.dimen.checkbox_row_padding_v),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.checkbox_row_gap)),
        ) {
            // The Row owns the toggle semantics; the checkbox is a visual indicator only.
            indicator()
            if (leadingContent != null) {
                leadingContent()
            } else if (icon != null) {
                BingeIconPlate(
                    painter = rememberVectorPainter(icon),
                    tint = iconTint,
                    modifier = Modifier.alpha(if (enabled) 1f else DISABLED_ALPHA),
                )
            }
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
            trailingContent?.let { trailing ->
                val gap = dimensionResource(R.dimen.row_trailing_control_gap) - dimensionResource(R.dimen.checkbox_row_gap)
                Box(Modifier.padding(start = gap)) { trailing() }
            }
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
