package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * One season in a [SeasonPickerSheet]: its [number] (the key [onSelect][SeasonPickerSheet] is handed), its [label] ("Season 2", "Specials")
 * and the [subtitle] under it (its episode count, its year, where it stands). The caller owns the words.
 */
@Immutable
class SeasonRow(
    val number: Int,
    val label: String,
    val subtitle: String,
)

/**
 * The chip in a series page's Episodes header that names the season shown ([label]) and opens the picker on a tap. It is as wide as its label up
 * to a cap, then the label ends in an ellipsis. A show with one season has nothing to choose, so the caller draws no dropdown.
 *
 * @need name the season shown and open the season picker
 * @group detail
 * @replaces a hand-built chip with a down arrow
 * @sample SeasonDropdownSample
 */
@Composable
fun SeasonDropdown(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .widthIn(max = dimensionResource(R.dimen.season_dropdown_max_width))
            .clip(BingeShapes.Chip)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(
                start = dimensionResource(R.dimen.chip_padding_h),
                end = dimensionResource(R.dimen.padding_xs),
                top = dimensionResource(R.dimen.padding_xs),
                bottom = dimensionResource(R.dimen.padding_xs),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.padding_xs)))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * Every season of a series as a task sheet: a row for each of [rows] with its label and subtitle, the [selected] one marked with a check.
 * [onSelect] is handed the tapped row's number, and [onDismiss] is called when the sheet is dismissed. The sheet itself is a modal, so its
 * rows are [SeasonPickerContent], which a screenshot can render.
 *
 * @need let a person choose a season of a series from a sheet
 * @group detail
 * @replaces a hand-built sheet of season rows
 * @sample SeasonPickerContentSample
 */
@Composable
fun SeasonPickerSheet(
    rows: List<SeasonRow>,
    selected: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = onDismiss) { nav ->
        TaskSheetTopClearance(nav)
        SeasonPickerContent(rows, selected, onSelect)
    }
}

/**
 * The rows of a [SeasonPickerSheet], without the sheet around them: one row for each of [rows], the [selected] season marked with a
 * check, in a group that scrolls when there are more than fit.
 *
 * @need list a series' seasons to choose from
 * @group detail
 * @replaces a hand-built list of season rows
 * @sample SeasonPickerContentSample
 */
@Composable
fun SeasonPickerContent(
    rows: List<SeasonRow>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    ItemGroup(
        title = null,
        rows = rows.map { season ->
            val isSelected = season.number == selected
            ListItem(
                icon = Icons.Filled.Tv,
                label = season.label,
                detail = season.subtitle,
                supportingMaxLines = 3,
                selected = isSelected,
                onClick = { onSelect(season.number) },
                trailingContent = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                } else {
                    { Spacer(Modifier) }
                },
            )
        },
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.padding_m), vertical = dimensionResource(R.dimen.padding_s)),
    )
}
