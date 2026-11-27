package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * One season in a [SeasonPickerSheet]: its [number] (the key [onSelect][SeasonPickerSheet] is handed), its [label] ("Season 2", "Specials")
 * and the [subtitle] under it (its episode count, its year, where it stands). [key] is the same season as text, for an app that identifies its
 * seasons by a string (two seasons can share a number, or have none); it defaults to the number. The caller owns the words.
 * [posterUrl] and [overview] are optional: when any row of a picker has either, every row is drawn with a poster beside its text and the
 * overview, up to two lines, under it.
 */
@Immutable
class SeasonRow(
    val number: Int,
    val label: String,
    val subtitle: String,
    val key: String = number.toString(),
    val posterUrl: String? = null,
    val overview: String? = null,
) {
    /** True when the row has artwork or a synopsis to show, which is what moves the picker to its poster rows. */
    internal val isRich: Boolean get() = posterUrl != null || !overview.isNullOrBlank()
}

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
 * [onSelect] is handed the tapped row's number, and [onDismiss] is called when the sheet is dismissed. An app that keys its seasons by text passes
 * [selectedKey], which then decides which row is marked, and [onSelectKey], which is handed the tapped row's key along with [onSelect]. The sheet itself is a modal, so its
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
    modifier: Modifier = Modifier,
    selectedKey: String? = null,
    onSelectKey: ((key: String) -> Unit)? = null,
) {
    BingeAdaptiveSheet(kind = BingeSheetKind.Task, onDismissRequest = onDismiss, modifier = modifier) { nav ->
        TaskSheetTopClearance(nav)
        SeasonPickerContent(rows, selected, onSelect, selectedKey = selectedKey, onSelectKey = onSelectKey)
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
    selectedKey: String? = null,
    onSelectKey: ((key: String) -> Unit)? = null,
) {
    if (rows.any { it.isRich }) {
        RichSeasonRows(rows, selected, onSelect, modifier, selectedKey, onSelectKey)
        return
    }
    ItemGroup(
        title = null,
        rows = rows.map { season ->
            val isSelected = if (selectedKey != null) season.key == selectedKey else season.number == selected
            ListItem(
                icon = Icons.Filled.Tv,
                label = season.label,
                detail = season.subtitle,
                supportingMaxLines = 3,
                selected = isSelected,
                onClick = {
                    onSelect(season.number)
                    onSelectKey?.invoke(season.key)
                },
                trailingContent = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                } else {
                    // An empty slot, so the row draws no chevron: ItemGroup puts one in a row that has no trailing content.
                    { Spacer(Modifier) }
                },
            )
        },
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.padding_m), vertical = dimensionResource(R.dimen.padding_s)),
    )
}

/** The picker's rows when the seasons have artwork or a synopsis: a poster, the name, the subtitle and two lines of overview, scrolled to the shown season. */
@Composable
private fun RichSeasonRows(
    rows: List<SeasonRow>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    selectedKey: String?,
    onSelectKey: ((key: String) -> Unit)?,
) {
    val requesters = remember(rows) { rows.map { BringIntoViewRequester() } }
    val selectedIndex = rows.indexOfFirst { if (selectedKey != null) it.key == selectedKey else it.number == selected }
    LaunchedEffect(rows) {
        // Only on opening: a tap changes the selection, and the list the person is looking at should stay where it is.
        if (selectedIndex > 0) requesters[selectedIndex].bringIntoView()
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimensionResource(R.dimen.padding_m), vertical = dimensionResource(R.dimen.padding_s)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing)),
    ) {
        rows.forEachIndexed { index, season ->
            val isSelected = index == selectedIndex
            ListRow(
                modifier = Modifier.bringIntoViewRequester(requesters[index]),
                onClick = {
                    onSelect(season.number)
                    onSelectKey?.invoke(season.key)
                },
                selected = isSelected,
                leading = { ListRowPoster(imageUrl = season.posterUrl, contentDescription = null) },
                trailingContent = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                } else {
                    null
                },
            ) { contentModifier ->
                Column(modifier = contentModifier) {
                    Text(
                        text = season.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                    if (season.subtitle.isNotBlank()) {
                        Text(
                            text = season.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!season.overview.isNullOrBlank()) {
                        Spacer(Modifier.height(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)))
                        Text(
                            text = season.overview,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
