package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.CollectionInfo
import androidx.compose.ui.semantics.CollectionItemInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.collectionInfo
import androidx.compose.ui.semantics.collectionItemInfo
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvIllustration
import com.binge.designsystem.tv.component.TvOptionRow
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvLazyFocusGroup
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * A [TvListPaneBoard]'s end pane: the row's hero or illustration, its name, what it means, its options and a
 * closing note. The only place a value changes; the list only names rows. Nothing to describe leaves it empty.
 *
 * It scrolls, because a column that cannot scroll starves its last children into slivers that are still
 * focusable, and it is lazy, because a filter's options can run to hundreds (every provider of a region, say) and an
 * eager column composes them all and loads every logo the moment the pane swaps in. Only the options in view, and
 * a screenful either side, are composed. ← from any option goes to [backToListFocus], the described row.
 */
@Composable
internal fun TvListPane(
    row: TvPaneRow?,
    backToListFocus: FocusRequester,
    modifier: Modifier = Modifier,
    initialFocusedOptionLabel: String? = null,
) {
    if (row == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }
    row.paneContent?.let { content ->
        Box(modifier = modifier.fillMaxSize()) { content() }
        return
    }
    val hasOptions = row.options.isNotEmpty() || row.pinnedOptions.isNotEmpty()
    // Pinned options are controls, so a first → into the pane lands on the first value, one ↑ from them. Re-entry still
    // resumes on the option last focused: the restorer falls back to the first value only when nothing was saved.
    val routesToFirstValue = row.pinnedOptions.isNotEmpty() && row.options.isNotEmpty()
    val firstValueFocus = remember { FocusRequester() }
    // Keyed on the row, so a described row opens at its top rather than where the last one was scrolled to.
    val listState = key(row.key) { rememberLazyListState() }
    val sectionGap = dimensionResource(DesR.dimen.padding_m)
    val optionsLead = sectionGap + dimensionResource(DesR.dimen.padding_s)
    val optionGap = dimensionResource(TvR.dimen.tv_option_row_gap)
    // The options' direct semantics parent is the lazy column, so the group and its counts sit there. The counts are the
    // options only, not the label and body around them, and the lazy layout's own collection info yields to this one.
    // An action is not one of the choices, so it is neither counted nor given a place.
    val pinnedChoices = row.pinnedOptions.count { !it.action }
    val optionCount = pinnedChoices + row.options.count { !it.action }
    val optionsGroup =
        Modifier
            .selectableGroup()
            .semantics { collectionInfo = CollectionInfo(rowCount = optionCount, columnCount = 1) }
    val optionWidth = Modifier.width(dimensionResource(TvR.dimen.tv_list_pane_option_width))
    // On the lazy column, the focus group the options are inside, so it saves an option rather than the column.
    val restorer = if (hasOptions) Modifier.tvLazyFocusGroup(fallback = firstValueFocus.takeIf { routesToFirstValue }) else Modifier
    TvStableFocusScroll {
        Box(
            modifier =
                modifier
                    .fillMaxSize()
                    .then(
                        if (!hasOptions) {
                            Modifier
                        } else {
                            Modifier
                                .focusProperties { left = backToListFocus }
                                .focusGroup()
                        },
                    ),
        ) {
            LazyColumn(
                state = listState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .then(restorer)
                        .then(if (!hasOptions || row.multiSelect) Modifier else optionsGroup),
                contentPadding = PaddingValues(vertical = dimensionResource(TvR.dimen.tv_focus_ring_bleed)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // The spacing between siblings is each item's own top inset, not an arrangement's, because the options sit
                // closer together than the sections around them do and a lazy column has the one spacing.
                val hero = row.hero
                val icon = row.icon
                val hasHeader = hero != null || icon != null
                if (hasHeader) {
                    item(key = "header") {
                        when {
                            hero != null -> hero()
                            icon != null -> TvIllustration(icon = icon)
                        }
                    }
                }
                item(key = "label") {
                    Text(
                        text = row.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = if (hasHeader) Modifier.padding(top = sectionGap) else Modifier,
                    )
                }
                if (row.body.isNotEmpty()) {
                    item(key = "body") {
                        Text(
                            text = row.body,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = sectionGap),
                        )
                    }
                }
                if (hasOptions) {
                    itemsIndexed(row.pinnedOptions, key = { index, _ -> "pinned-$index" }) { index, option ->
                        val inset = if (index == 0) optionsLead else optionGap
                        val position = row.pinnedOptions.take(index).count { !it.action }
                        PaneOption(option, row.multiSelect, initialFocusedOptionLabel, optionWidth.padding(top = inset), position)
                    }
                    if (routesToFirstValue) {
                        item(key = "divider") { PinnedDivider(Modifier.padding(top = optionGap)) }
                    }
                    itemsIndexed(row.options, key = { index, _ -> "option-$index" }) { index, option ->
                        val inset = if (index == 0 && row.pinnedOptions.isEmpty()) optionsLead else optionGap
                        val entry = if (routesToFirstValue && index == 0) Modifier.focusRequester(firstValueFocus) else Modifier
                        PaneOption(
                            option,
                            row.multiSelect,
                            initialFocusedOptionLabel,
                            optionWidth.padding(top = inset).then(entry),
                            pinnedChoices + row.options.take(index).count { !it.action },
                        )
                    }
                    row.clear?.let { action ->
                        item(key = "clear") {
                            TvButton(
                                label = action.label,
                                onClick = action.onClick,
                                enabled = action.enabled,
                                modifier = optionWidth.padding(top = optionGap),
                            )
                        }
                    }
                }
                row.note?.let { note ->
                    item(key = "note") { PaneNote(note = note, announce = row.announceNote, modifier = Modifier.padding(top = sectionGap)) }
                }
            }
        }
    }
}

/** The pane's closing note, read politely when [announce] says it reports an outcome. */
@Composable
private fun PaneNote(
    note: String,
    announce: Boolean,
    modifier: Modifier = Modifier,
) {
    Text(
        text = note,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .padding(top = dimensionResource(DesR.dimen.padding_xs))
                .then(if (announce) Modifier.semantics { liveRegion = LiveRegionMode.Polite } else Modifier),
    )
}

/**
 * One option as the row's kind of choice: a checkbox in a multi-select row, a radio-style row otherwise, and a plain
 * row that announces as a button for a [TvPaneOption.action], which is not one of the choices.
 */
@Composable
private fun PaneOption(
    option: TvPaneOption,
    multiSelect: Boolean,
    initialFocusedOptionLabel: String?,
    entryModifier: Modifier,
    position: Int,
) {
    val optionModifier =
        (option.focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
            .then(entryModifier)
            .then(
                if (multiSelect || option.action) {
                    Modifier
                } else {
                    Modifier.semantics {
                        collectionItemInfo = CollectionItemInfo(rowIndex = position, rowSpan = 1, columnIndex = 0, columnSpan = 1)
                    }
                },
            )
    if (option.action) {
        // The side sheet's row: a plain row with the option's look that announces as a button, with no selection state.
        TvSideSheetRow(
            label = option.label,
            onClick = option.onSelect,
            icon = option.icon,
            initiallyFocused = option.label == initialFocusedOptionLabel,
            modifier = optionModifier,
        )
    } else if (multiSelect) {
        TvCheckboxRow(
            label = option.label,
            checked = option.selected,
            onCheckedChange = { option.onSelect() },
            initiallyFocused = option.label == initialFocusedOptionLabel,
            icon = option.icon,
            excluded = option.excluded,
            leading = option.leading,
            modifier = optionModifier,
        )
    } else {
        TvOptionRow(
            label = option.label,
            selected = option.selected,
            onClick = option.onSelect,
            initiallyFocused = option.label == initialFocusedOptionLabel,
            icon = option.icon,
            modifier = optionModifier,
        )
    }
}

/** The line between the pinned options and the options, so a control is not mistaken for one of the values. */
@Composable
private fun PinnedDivider(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .width(dimensionResource(TvR.dimen.tv_list_pane_option_width))
                .padding(vertical = dimensionResource(DesR.dimen.padding_s))
                .height(dimensionResource(DesR.dimen.hairline_thickness))
                .background(MaterialTheme.colorScheme.border),
    )
}
