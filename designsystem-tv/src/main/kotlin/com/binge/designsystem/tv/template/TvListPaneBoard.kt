package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.ListItemScale
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvRowEmphasis
import com.binge.designsystem.tv.component.TvRowStatus
import com.binge.designsystem.tv.component.hasTvRowStatus
import com.binge.designsystem.tv.component.tvRowEmphasis
import com.binge.designsystem.tv.component.tvRowListItemColors
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvEndFocusDirection
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.uppercaseLocalised
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * One option in a row's pane: a label, a tick when [selected], and the call that commits it. [focusRequester] is
 * set when an overlay this option opens needs to hand focus back to it on close. [icon] leads the option when one
 * fits the choice itself (a glyph per option, not one for the whole row).
 *
 * In a multi-select row an option can be [excluded] as well as [selected]: ruled out rather than chosen, with the
 * excluded mark in its box. The caller decides what OK cycles through. [leading] is a mark of the caller's own, such as
 * a logo, in place of [icon]; both apply to multi-select options only.
 *
 * An [action] does something rather than being chosen, such as a pinned shortcut that opens a screen: it renders and
 * announces as a button, with no tick or box, and [selected] and [excluded] do not apply.
 */
@Immutable
data class TvPaneOption(
    val label: String,
    val selected: Boolean = false,
    val onSelect: () -> Unit = {},
    val focusRequester: FocusRequester? = null,
    val icon: ImageVector? = null,
    val excluded: Boolean = false,
    val leading: (@Composable () -> Unit)? = null,
    val action: Boolean = false,
)

/**
 * A row of a [TvListPaneBoard]: its [label] in the list, and in the pane what it means, its [options] and a closing
 * [note]. No options makes the row a read-out: the value lives in [body], and [note] says where to change it.
 *
 * The pane leads with [hero] when there is one (a code to scan, say), otherwise with [icon] on its plate, otherwise
 * with neither. [icon] also leads the row's name in the list, plain, in the row's content colour. [announceNote] makes the note a polite live region, for a note that reports an outcome rather than
 * a static hint, which would otherwise be read again every time focus reached the row.
 *
 * [body] and [note] are the pane's description, so they wrap to as many lines as they need. The list's labels and details
 * are one line each, ellipsized.
 *
 * [detail] is the row's current value on its line in the list, for a row whose value is a choice (the chosen option,
 * On or Off), so the list can be read without moving into each pane. Null leaves the row one line.
 *
 * A row can serve as a filter. [multiSelect] makes the options checkboxes, announced with their toggled state, and
 * [TvPaneOption.onSelect] then toggles; the default keeps them mutually exclusive, as radio rows. [clear] puts a button
 * under the options. An empty [body] draws no description, so the options sit directly under the name. A filter shows
 * what it has applied through [detail], such as "3 selected" or "Any". [pinnedOptions] sit above the options behind a
 * divider: controls that act on the values, or shortcuts, kept apart so they are not taken for one of them.
 *
 * The row's end can carry a status: [loading] while its value is on its way, else [badgeAlert] when it needs the user,
 * else [badgeCount] when something is waiting. Each takes width from the label, which ellipsizes rather than wraps.
 *
 * A row can open something of its own. [onClick] is what OK on it does; null keeps OK on the row as a move onto it.
 * [paneContent] replaces the whole pane description, such as a [TvListPaneListPreview] of the list OK opens, and a
 * row with both steps in on → as well as OK, since its pane shows the next level rather than controls to move into.
 * [iconPainter] leads the row in place of [icon], for a glyph the caller resolves itself, such as an app's own.
 */
@Immutable
data class TvPaneRow(
    val key: String,
    val label: String,
    val body: String = "",
    val note: String? = null,
    val options: List<TvPaneOption> = emptyList(),
    val icon: ImageVector? = null,
    val hero: (@Composable () -> Unit)? = null,
    val announceNote: Boolean = false,
    val detail: String? = null,
    val multiSelect: Boolean = false,
    val clear: TvPaneAction? = null,
    val badgeCount: Int? = null,
    val badgeAlert: Boolean = false,
    val loading: Boolean = false,
    val pinnedOptions: List<TvPaneOption> = emptyList(),
    val onClick: (() -> Unit)? = null,
    val iconPainter: (@Composable () -> Painter)? = null,
    val paneContent: (@Composable () -> Unit)? = null,
)

/** Whether → on the row runs its [TvPaneRow.onClick] rather than moving into a pane that has nothing to focus. */
internal val TvPaneRow.stepsIn: Boolean
    get() = onClick != null && paneContent != null

/** A button the board draws for an action that is not an option: what it says, what it does, and whether it is live. */
@Immutable
data class TvPaneAction(
    val label: String,
    val onClick: () -> Unit,
    val enabled: Boolean = true,
)

/**
 * A titled block of a [TvListPaneBoard]'s rows. The title is a read-out and never takes focus. [note] sits under it,
 * also a read-out, for a line that holds for the whole group, such as where its values are changed. It shows beside
 * the rows whichever one is described, so it is not repeated on each.
 */
@Immutable
data class TvPaneGroup(
    val title: String,
    val rows: List<TvPaneRow>,
    val note: String? = null,
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
 * which [TvListPaneBoardSkeleton] reserves. [clearAll] adds a button under the last row that acts on the board as a
 * whole; it takes focus without changing the described row.
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
    clearAll: TvPaneAction? = null,
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
        entry = if (hosting == TvPageHosting.Overlay) describedRowFocus else null,
        actionScrolls = false,
        copy = {
            TvPaneList(
                groups = groups,
                describedKey = describedRow?.key,
                onFocusRow = onFocusRow,
                describedRowFocus = describedRowFocus,
                onStepIn = describedRow?.takeIf { it.stepsIn }?.onClick,
                initialColumnHasFocus = initialListHasFocus,
                clearAll = clearAll,
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
 * scroll so a focus fill at either end is not shaved by the column's edge. [onStepIn] runs on → in place of the
 * move into the pane, for a described row whose pane previews where it leads.
 */
@Composable
internal fun TvPaneList(
    groups: List<TvPaneGroup>,
    describedKey: String?,
    onFocusRow: (String) -> Unit,
    describedRowFocus: FocusRequester,
    modifier: Modifier = Modifier,
    onStepIn: (() -> Unit)? = null,
    initialColumnHasFocus: Boolean = false,
    clearAll: TvPaneAction? = null,
) {
    val endDirection = tvEndFocusDirection()
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
                    .focusProperties {
                        onEnter = { runCatching { describedRowFocus.requestFocus() } }
                        onExit = {
                            if (onStepIn != null && requestedFocusDirection == endDirection) {
                                cancelFocusChange()
                                onStepIn()
                            }
                        }
                    }.focusGroup()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = dimensionResource(TvR.dimen.tv_focus_ring_bleed)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_list_pane_row_gap)),
        ) {
            groups.forEach { group ->
                TvPaneGroupTitle(group.title, group.note)
                group.rows.forEach { row ->
                    val described = row.key == describedKey
                    TvPaneListRow(
                        row = row,
                        isFirst = row.key == firstRowKey,
                        emphasis = tvRowEmphasis(isDescribedRow = described, columnHasFocus = columnHasFocus),
                        onFocused = { onFocusRow(row.key) },
                        modifier =
                            Modifier
                                .tvListPaneFirstRowAnchor(row.key == firstRowKey)
                                .then(if (described) Modifier.focusRequester(describedRowFocus) else Modifier),
                    )
                }
            }
            clearAll?.let { action ->
                TvPaneListAction(
                    action = action,
                    modifier = Modifier.padding(top = dimensionResource(TvR.dimen.tv_list_pane_group_gap)),
                )
            }
        }
    }
}

/** A group's title and its note, inset to line up with the row labels under them and held clear of the first row's fill. */
@Composable
private fun TvPaneGroupTitle(title: String, note: String?) {
    val start = dimensionResource(TvR.dimen.tv_list_pane_row_padding_horizontal)
    val gap = dimensionResource(TvR.dimen.tv_list_pane_header_bottom_gap)
    Text(
        text = title.uppercaseLocalised(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier
                .padding(start = start, top = dimensionResource(TvR.dimen.tv_list_pane_group_gap))
                .then(if (note == null) Modifier.padding(bottom = gap) else Modifier),
    )
    note?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = start, bottom = gap),
        )
    }
}

/**
 * A tv-material `ListItem`: the row's icon, its name, its current detail under the name when it has one, and its status
 * at the end. The icon is plain, in the content colour, so it inverts with the focus fill. Focus is colour, never scale.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPaneListRow(
    row: TvPaneRow,
    isFirst: Boolean,
    emphasis: TvRowEmphasis,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loadingDescription = stringResource(DesR.string.cd_list_item_loading)
    ListItem(
        // The described row is marked by its emphasis, not by `selected`, which would give it a container of its own.
        selected = false,
        onClick = row.onClick ?: onFocused,
        headlineContent = {
            Text(
                text = row.label,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.tvListPaneFirstRowLabelAnchor(isFirst),
            )
        },
        supportingContent =
            row.detail?.let {
                { Text(text = it, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis) }
            },
        leadingContent =
            row.iconPainter?.let { painter ->
                {
                    Icon(
                        painter = painter(),
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(TvR.dimen.tv_list_pane_row_icon)).tvListPaneFirstRowIconAnchor(isFirst),
                    )
                }
            } ?: row.icon?.let { icon ->
                {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(dimensionResource(TvR.dimen.tv_list_pane_row_icon)).tvListPaneFirstRowIconAnchor(isFirst),
                    )
                }
            },
        trailingContent =
            if (hasTvRowStatus(row.badgeCount, row.badgeAlert, row.loading)) {
                { TvRowStatus(badgeCount = row.badgeCount, badgeAlert = row.badgeAlert, loading = row.loading) }
            } else {
                null
            },
        shape = ListItemDefaults.shape(shape = BingeShapes.TvListItem),
        colors =
            tvRowListItemColors(
                emphasis = emphasis,
                restingContainer = Color.Transparent,
                restingContent = MaterialTheme.colorScheme.onSurface,
            ),
        scale = ListItemScale.None,
        modifier =
            modifier
                .fillMaxWidth()
                .onFocusChanged { if (it.isFocused) onFocused() }
                .semantics { if (row.loading) stateDescription = loadingDescription },
    )
}

/** The board-wide action under the list: a row in the list's own shape, which never describes anything. */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvPaneListAction(action: TvPaneAction, modifier: Modifier = Modifier) {
    ListItem(
        selected = false,
        onClick = action.onClick,
        enabled = action.enabled,
        headlineContent = {
            Text(text = action.label, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        shape = ListItemDefaults.shape(shape = BingeShapes.TvListItem),
        colors =
            tvRowListItemColors(
                emphasis = TvRowEmphasis.Resting,
                restingContainer = Color.Transparent,
                restingContent = MaterialTheme.colorScheme.primary,
            ),
        scale = ListItemScale.None,
        modifier = modifier.fillMaxWidth().semantics { role = Role.Button },
    )
}

/**
 * Marks a board's first row as the layout anchor its skeleton is measured against. Shared by the board and
 * [TvListPaneBoardSkeleton], so the two cannot name the anchor differently.
 */
internal fun Modifier.tvListPaneFirstRowAnchor(isFirst: Boolean): Modifier =
    if (isFirst) tvLayoutAnchor(TvLayoutAnchors.LIST_PANE_FIRST_ROW) else this

/** Marks a board's first row's icon, which its skeleton's icon block is measured against. */
internal fun Modifier.tvListPaneFirstRowIconAnchor(isFirst: Boolean): Modifier =
    if (isFirst) tvLayoutAnchor(TvLayoutAnchors.LIST_PANE_FIRST_ROW_ICON) else this

/** Marks a board's first row's label, which its skeleton's label bar is measured against. */
internal fun Modifier.tvListPaneFirstRowLabelAnchor(isFirst: Boolean): Modifier =
    if (isFirst) tvLayoutAnchor(TvLayoutAnchors.LIST_PANE_FIRST_ROW_LABEL) else this
