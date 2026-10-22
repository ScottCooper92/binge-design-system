package com.binge.designsystem.tv.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemColors
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.ListItemScale
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.theme.tvStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A titled set of mutually exclusive options, stacked so a long label has the row's width to itself.
 *
 * **OK is the commit.** The chosen option carries a [TvSelectedTick], and moving focus across the rows changes
 * nothing until the user presses. Pressing the option already chosen is a no-op, guarded here so no caller has to
 * remember to. With no [choices] it draws nothing, title included.
 *
 * [initialFocusedLabel] seeds one row as focused for a frame; production passes null. [arrival] makes the first row
 * the one the page lands on. [choiceIcon] gives a choice the icon its row leads with, on its tinted plate: the
 * choice's own where each has one, otherwise the setting's. Null draws no icon.
 */
@Composable
fun <T> TvOptionGroup(
    title: String,
    choices: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    initialFocusedLabel: String? = null,
    arrival: TvArrivalFocus? = null,
    choiceIcon: (T) -> ImageVector? = { null },
) {
    if (choices.isEmpty()) return
    Column(
        modifier = modifier.width(dimensionResource(TvR.dimen.tv_text_field_width)).selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        choices.forEachIndexed { index, (key, label) ->
            val isSelected = key == selected
            TvOptionRow(
                label = label,
                selected = isSelected,
                onClick = { if (!isSelected) onSelect(key) },
                initiallyFocused = label == initialFocusedLabel,
                icon = choiceIcon(key),
                modifier = if (index == 0 && arrival != null) Modifier.tvArrivalTarget(arrival) else Modifier,
            )
        }
    }
}

/**
 * One option, as a tv-material `ListItem`: its label, and a [TvSelectedTick] when [selected].
 *
 * Focus is the fill and selection is the tick, and the two are independent: the current value keeps its tick while
 * focus passes over it. The chosen option's label and tick take the accent at rest, and invert with the fill. The
 * row never scales on focus. [trailingContent] sits before the tick, for a short value the option carries. [initiallyFocused]
 * paints the row focused for a frame, which fires no focus events; production passes false. [supportingText] is one line
 * under the label, ellipsized: what choosing the option does. While [saving], the tick's place holds a spinner instead, in
 * the tick's box so nothing moves, and the row announces that it is in progress: for a choice that applies at once but takes
 * a moment to land. The caller decides what a press does meanwhile.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyFocused: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null,
    icon: ImageVector? = null,
    supportingText: String? = null,
    saving: Boolean = false,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val savingDescription = stringResource(DesR.string.cd_list_item_loading)
    ListItem(
        // Passed through so the row announces its state. The colours below give selection no container of its
        // own: the container belongs to focus, and the tick is the selection.
        selected = selected,
        onClick = onClick,
        headlineContent = {
            Text(text = label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = supportingText?.let { { SupportingLine(it) } },
        leadingContent = icon?.let { { TvRowIcon(icon = it, isFocused = focused) } },
        trailingContent =
            if (selected || saving || trailingContent != null) {
                { OptionTrailing(selected = selected, saving = saving, trailing = trailingContent) }
            } else {
                null
            },
        shape = ListItemDefaults.shape(shape = BingeShapes.TvListItem),
        colors =
            tvRowListItemColors(
                emphasis = if (focused) TvRowEmphasis.Focused else TvRowEmphasis.Resting,
                restingContainer = MaterialTheme.colorScheme.surface,
                restingContent = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            ),
        scale = ListItemScale.None,
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused }
                // One of a set, so it announces as a radio button, as the checkbox row announces as a checkbox.
                .semantics {
                    role = Role.RadioButton
                    this.selected = selected
                    if (saving) stateDescription = savingDescription
                },
    )
}

/**
 * One yes-or-no setting as a row the remote can reach: a box that takes a tick when [checked], then its [label].
 * OK flips it through [onCheckedChange].
 *
 * Not [enabled], it is dimmed and OK does nothing, but it keeps focus so the remote does not lose its place. An [icon]
 * follows the box on its tinted plate, and [supportingText] adds one ellipsized line saying what the setting does.
 * [initiallyFocused] paints the row focused for a frame; production passes false.
 *
 * [excluded] makes it a third state, ruled out rather than chosen: the box carries the excluded mark in place of the
 * tick, and the row announces as indeterminate and excluded. [leading] replaces [icon] with a mark of the caller's own,
 * such as a logo, in the same place.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    initiallyFocused: Boolean = false,
    icon: ImageVector? = null,
    supportingText: String? = null,
    excluded: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val excludedDescription = stringResource(DesR.string.cd_option_excluded)
    ListItem(
        selected = false,
        onClick = { onCheckedChange(!checked) },
        enabled = enabled,
        headlineContent = {
            Text(text = label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = supportingText?.let { { SupportingLine(it) } },
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
            ) {
                CheckboxMark(checked = checked, excluded = excluded)
                if (leading != null) leading() else icon?.let { TvRowIcon(icon = it, isFocused = focused) }
            }
        },
        shape = ListItemDefaults.shape(shape = BingeShapes.TvListItem),
        colors =
            tvRowListItemColors(
                emphasis = if (focused) TvRowEmphasis.Focused else TvRowEmphasis.Resting,
                restingContainer = Color.Transparent,
                restingContent = MaterialTheme.colorScheme.onSurface,
            ),
        scale = ListItemScale.None,
        modifier =
            modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .onFocusChanged { focused = it.isFocused }
                .semantics {
                    role = Role.Checkbox
                    toggleableState = if (excluded) ToggleableState.Indeterminate else ToggleableState(checked)
                    if (excluded) stateDescription = excludedDescription
                },
    )
}

/**
 * A `ListItem`'s colours for a row at [emphasis], handed over as its **resting** pair so a hoisted emphasis paints
 * even though the row owns no focus flag. The focused pair is the full focus fill whatever the emphasis.
 *
 * Selection takes the same colours as its unselected counterpart. A row passes its selection to `ListItem` so a
 * screen reader hears it, and selection is marked by [TvSelectedTick], never by a container of its own.
 */
@Composable
fun tvRowListItemColors(
    emphasis: TvRowEmphasis,
    restingContainer: Color,
    restingContent: Color,
): ListItemColors {
    val container = emphasis.containerColor(resting = restingContainer)
    val content = emphasis.contentColor(resting = restingContent)
    val focusedContainer = MaterialTheme.tvStyle.focusContainer
    val focusedContent = MaterialTheme.tvStyle.onFocusContainer
    return ListItemDefaults.colors(
        containerColor = container,
        contentColor = content,
        focusedContainerColor = focusedContainer,
        focusedContentColor = focusedContent,
        selectedContainerColor = container,
        selectedContentColor = content,
        focusedSelectedContainerColor = focusedContainer,
        focusedSelectedContentColor = focusedContent,
    )
}

/** A row's second line: one line, ellipsized, saying what the option does. */
@Composable
private fun SupportingLine(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
}

/** The tick reads `ListItem`'s content colour rather than re-deriving it, so it inverts with the focus fill. */
@Composable
private fun OptionTrailing(
    selected: Boolean,
    saving: Boolean,
    trailing: (@Composable () -> Unit)?,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        trailing?.invoke()
        when {
            saving ->
                Box(modifier = Modifier.size(dimensionResource(TvR.dimen.tv_selected_tick_size)), contentAlignment = Alignment.Center) {
                    TvRowStatus(badgeCount = null, badgeAlert = false, loading = true)
                }
            selected -> TvSelectedTick(tint = LocalContentColor.current)
        }
    }
}

/** An outline and a tick, or the excluded mark, in the row's own content colour, so both read on the focus fill and off it. */
@Composable
private fun CheckboxMark(checked: Boolean, excluded: Boolean) {
    val color = LocalContentColor.current
    Box(
        modifier =
            Modifier
                .size(dimensionResource(TvR.dimen.tv_checkbox_size))
                .border(dimensionResource(TvR.dimen.tv_checkbox_border), color, BingeShapes.ElementExtraSmall),
        contentAlignment = Alignment.Center,
    ) {
        if (excluded) {
            TvExcludedMark(tint = color)
        } else if (checked) {
            TvSelectedTick(tint = color)
        }
    }
}
