package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(R.dimen.min_touch_target))
                .toggleable(value = checked, enabled = enabled, role = Role.Checkbox, onValueChange = onCheckedChange)
                .padding(
                    horizontal = dimensionResource(R.dimen.checkbox_row_padding_h),
                    vertical = dimensionResource(R.dimen.checkbox_row_padding_v),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.checkbox_row_gap)),
        ) {
            // The Row owns the toggle semantics; the checkbox is a visual indicator only.
            Checkbox(checked = checked, onCheckedChange = null, enabled = enabled)
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

/**
 * [CheckboxRow] under its old names, `onToggle` and `subtitle`. Its second line and callback are `supportingText` and
 * `onCheckedChange` now, the names the TV rows use. Kotlin cannot hold two overloads that differ only in parameter
 * names, nor tell them apart for a call that passes three arguments by position, so this one takes `subtitle` where
 * `onCheckedChange` sits and `onToggle` late, ahead of the trailing slot. Call it by name, as the old signature was.
 */
@Deprecated(
    message = "Renamed: onToggle is onCheckedChange and subtitle is supportingText.",
    replaceWith = ReplaceWith(
        "CheckboxRow(label, checked, onCheckedChange = onToggle, modifier = modifier, supportingText = subtitle, " +
            "enabled = enabled, showDivider = showDivider, icon = icon, iconTint = iconTint, " +
            "leadingContent = leadingContent, trailingContent = trailingContent)",
    ),
)
@Suppress("ModifierParameter", "LongParameterList")
@Composable
fun CheckboxRow(
    label: String,
    checked: Boolean,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showDivider: Boolean = false,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    onToggle: (Boolean) -> Unit,
    trailingContent: (@Composable () -> Unit)? = null,
) = CheckboxRow(
    label = label,
    checked = checked,
    onCheckedChange = onToggle,
    modifier = modifier,
    supportingText = subtitle,
    enabled = enabled,
    showDivider = showDivider,
    icon = icon,
    iconTint = iconTint,
    leadingContent = leadingContent,
    trailingContent = trailingContent,
)
