package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.ListItemScale
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvRowEmphasis
import com.binge.designsystem.tv.component.tvRowEmphasis
import com.binge.designsystem.tv.component.tvRowListItemColors
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.uppercaseLocalised
import com.binge.designsystem.tv.R as TvR

/**
 * One option in a row's pane: a label, a tick when [selected], and the call that commits it. [focusRequester] is
 * set when an overlay this option opens needs to hand focus back to it on close.
 */
@Immutable
data class TvPaneOption(
    val label: String,
    val selected: Boolean = false,
    val onSelect: () -> Unit = {},
    val focusRequester: FocusRequester? = null,
)

/**
 * A row of a [TvListPaneBoard]: its [label] in the list, and in the pane what it means, its [options] and a closing
 * [note]. No options makes the row a read-out: the value lives in [body], and [note] says where to change it.
 *
 * The pane leads with [hero] when there is one (a code to scan, say), otherwise with [icon] on its plate, otherwise
 * with neither. [announceNote] makes the note a polite live region, for a note that reports an outcome rather than
 * a static hint, which would otherwise be read again every time focus reached the row.
 */
@Immutable
data class TvPaneRow(
    val key: String,
    val label: String,
    val body: String,
    val note: String? = null,
    val options: List<TvPaneOption> = emptyList(),
    val icon: ImageVector? = null,
    val hero: (@Composable () -> Unit)? = null,
    val announceNote: Boolean = false,
)

/** A titled block of a [TvListPaneBoard]'s rows. The title is a read-out and never takes focus. */
@Immutable
data class TvPaneGroup(
    val title: String,
    val rows: List<TvPaneRow>,
)

/**
 * The list-and-pane board a TV settings page takes: row names in a column on the start side, and on the end side
 * what the described row means and the options that change it. A two-pane page in the board style under [title].
 *
 * Stateless. [focusedKey] names the row the pane describes, not whether that row holds focus: it holds still while
 * the user moves among the pane's options. The list derives the rest, marking the described row with
 * [TvRowEmphasis]: the full fill while the list has focus, a dim accent once focus moves into the pane. Each row
 * reports focus arriving on it through [onFocusRow], which is where the caller moves [focusedKey].
 *
 * Focus entering the list from outside lands on the described row, so ← from an option returns to the row it
 * belongs to. [initialListHasFocus] and [initialFocusedOptionLabel] seed the two focus observers for a frame, which
 * fires no focus events; production passes neither. The first row carries [TvLayoutAnchors.LIST_PANE_FIRST_ROW],
 * which [TvListPaneBoardSkeleton] reserves.
 */
@Composable
fun TvListPaneBoard(
    title: String?,
    groups: List<TvPaneGroup>,
    focusedKey: String?,
    onFocusRow: (String) -> Unit,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    initialListHasFocus: Boolean = false,
    initialFocusedOptionLabel: String? = null,
) {
    val describedRow = groups.firstNotNullOfOrNull { group -> group.rows.firstOrNull { it.key == focusedKey } }
    // Follows the described row even while focus is in the pane, which makes it the right target from anywhere there.
    val describedRowFocus = remember { FocusRequester() }
    TvTwoPanePage(
        title = title,
        modifier = modifier,
        split = TvTwoPaneSplit.ListDetail,
        style = TvTwoPaneStyle.Board,
        hosting = hosting,
        actionScrolls = false,
        copy = {
            TvPaneList(
                groups = groups,
                describedKey = describedRow?.key,
                onFocusRow = onFocusRow,
                describedRowFocus = describedRowFocus,
                initialColumnHasFocus = initialListHasFocus,
            )
        },
        action = {
            TvListPane(
                row = describedRow,
                backToListFocus = describedRowFocus,
                initialFocusedOptionLabel = initialFocusedOptionLabel,
            )
        },
    )
}

/**
 * The start column: group titles over their rows. It scrolls under focus, with its vertical padding inside the
 * scroll so a focus fill at either end is not shaved by the column's edge.
 */
@Composable
private fun TvPaneList(
    groups: List<TvPaneGroup>,
    describedKey: String?,
    onFocusRow: (String) -> Unit,
    describedRowFocus: FocusRequester,
    modifier: Modifier = Modifier,
    initialColumnHasFocus: Boolean = false,
) {
    var columnHasFocus by remember { mutableStateOf(initialColumnHasFocus) }
    val firstRowKey =
        groups
            .firstOrNull()
            ?.rows
            ?.firstOrNull()
            ?.key
    TvStableFocusScroll {
        Column(
            modifier =
                modifier
                    .fillMaxHeight()
                    // Outside the group, so it observes the group itself: focused while any row holds focus.
                    .onFocusChanged { columnHasFocus = it.hasFocus }
                    // Routes focus as it enters rather than requesting it on mount, so it never takes focus from a
                    // rail that still holds it. It is also what sends ← from an option back to the described row.
                    .tvEntryFocusGroup(describedRowFocus)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = dimensionResource(TvR.dimen.tv_focus_ring_bleed)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_list_pane_row_gap)),
        ) {
            groups.forEach { group ->
                TvPaneGroupTitle(group.title)
                group.rows.forEach { row ->
                    val described = row.key == describedKey
                    TvPaneListRow(
                        label = row.label,
                        emphasis = tvRowEmphasis(isDescribedRow = described, columnHasFocus = columnHasFocus),
                        onFocused = { onFocusRow(row.key) },
                        modifier =
                            Modifier
                                .tvListPaneFirstRowAnchor(row.key == firstRowKey)
                                .then(if (described) Modifier.focusRequester(describedRowFocus) else Modifier),
                    )
                }
            }
        }
    }
}

/** A group's title, inset to line up with the row labels under it and held clear of the first row's fill. */
@Composable
private fun TvPaneGroupTitle(title: String) {
    Text(
        text = title.uppercaseLocalised(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.padding(
                start = dimensionResource(TvR.dimen.tv_list_pane_row_padding_horizontal),
                top = dimensionResource(TvR.dimen.tv_list_pane_group_gap),
                bottom = dimensionResource(TvR.dimen.tv_list_pane_header_bottom_gap),
            ),
    )
}

/**
 * A one-line tv-material `ListItem`: just the row's name, since the pane carries its value. No leading icon, because
 * every row in the column is the same kind of thing. Focus is colour, never scale.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPaneListRow(
    label: String,
    emphasis: TvRowEmphasis,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        // The described row is marked by its emphasis, not by `selected`, which would give it a container of its own.
        selected = false,
        onClick = onFocused,
        headlineContent = {
            Text(text = label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        shape = ListItemDefaults.shape(shape = BingeShapes.TvListItem),
        colors =
            tvRowListItemColors(
                emphasis = emphasis,
                restingContainer = Color.Transparent,
                restingContent = MaterialTheme.colorScheme.onSurface,
            ),
        scale = ListItemScale.None,
        modifier = modifier.fillMaxWidth().onFocusChanged { if (it.isFocused) onFocused() },
    )
}

/**
 * Marks a board's first row as the layout anchor its skeleton is measured against. Shared by the board and
 * [TvListPaneBoardSkeleton], so the two cannot name the anchor differently.
 */
internal fun Modifier.tvListPaneFirstRowAnchor(isFirst: Boolean): Modifier =
    if (isFirst) tvLayoutAnchor(TvLayoutAnchors.LIST_PANE_FIRST_ROW) else this
