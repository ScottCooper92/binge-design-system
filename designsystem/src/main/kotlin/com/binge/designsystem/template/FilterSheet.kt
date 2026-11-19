package com.binge.designsystem.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeActionFooter
import com.binge.designsystem.component.BingeAdaptiveSheet
import com.binge.designsystem.component.BingeSheetDockingHeader
import com.binge.designsystem.component.BingeSheetKind
import com.binge.designsystem.component.BingeSheetTopBar
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.ItemGroup
import com.binge.designsystem.component.ListItem
import com.binge.designsystem.component.LocalBingeSheetDock
import com.binge.designsystem.component.bingeSheetPinnedFooter
import java.time.LocalDate

/**
 * A filter sheet: a title with the count of what is applied and a Clear all, a scrolling body, and a Show results button
 * pinned at the foot. The body is the [sections] (single-choice and include/exclude chips, drawn inline) followed by a
 * [rows] group, one row per filter that opens in a detail of its own, each showing its current value. The sheet draws what
 * it is given and holds no filter state: every callback names a section, a row or an option by the id it was given, and
 * the caller maps that onto its own model. All the copy is a parameter.
 *
 * On a phone it is a bottom sheet that opens part-way, docks behind a top bar when pulled up, and stacks pages on itself;
 * on a wide window it is a side sheet. [content] is the body without the host, for a caller that hosts it itself.
 *
 * @need let the user narrow a catalogue by several filters in one sheet
 * @group sheets
 * @replaces a hand-built filter bottom sheet with chip groups, filter rows and a pinned results button
 */
@Composable
fun FilterSheet(
    title: String,
    clearAllLabel: String,
    showResultsLabel: String,
    sections: List<FilterSectionUi>,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    appliedLabel: String? = null,
    rowsTitle: String? = null,
    rows: List<FilterRowUi> = emptyList(),
    onSingleSelect: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    onOptionClick: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    onOptionLongClick: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    optionLongClickLabel: String? = null,
    onRangeChange: (sectionId: String, values: ClosedFloatingPointRange<Float>) -> Unit = { _, _ -> },
    onDateBoundChange: (sectionId: String, isEnd: Boolean, date: LocalDate?) -> Unit = { _, _, _ -> },
    onRowClick: (rowId: String) -> Unit = {},
) {
    BingeAdaptiveSheet(
        kind = BingeSheetKind.Task,
        onDismissRequest = onDismiss,
        modifier = modifier,
        skipPartiallyExpanded = false,
        dockable = true,
    ) {
        FilterSheetContent(
            title = title,
            clearAllLabel = clearAllLabel,
            showResultsLabel = showResultsLabel,
            sections = sections,
            onApply = onApply,
            onClearAll = onClearAll,
            appliedLabel = appliedLabel,
            rowsTitle = rowsTitle,
            rows = rows,
            onSingleSelect = onSingleSelect,
            onOptionClick = onOptionClick,
            onOptionLongClick = onOptionLongClick,
            optionLongClickLabel = optionLongClickLabel,
            onRangeChange = onRangeChange,
            onDateBoundChange = onDateBoundChange,
            onRowClick = onRowClick,
        )
    }
}

/**
 * The body of [FilterSheet] without its host, so a sheet that already exists (or a side pane, or a frame) can hold it. The
 * header docks into a top bar inside a docking sheet and stays a plain header anywhere else. [clearAllEnabled] says whether
 * Clear all does anything; it is on while there is an [appliedLabel] to show.
 *
 * @need show the filter sheet's contents inside a host the caller already has
 * @group sheets
 */
@Composable
fun ColumnScope.FilterSheetContent(
    title: String,
    clearAllLabel: String,
    showResultsLabel: String,
    sections: List<FilterSectionUi>,
    onApply: () -> Unit,
    onClearAll: () -> Unit,
    appliedLabel: String? = null,
    rowsTitle: String? = null,
    rows: List<FilterRowUi> = emptyList(),
    onSingleSelect: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    onOptionClick: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    onOptionLongClick: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    optionLongClickLabel: String? = null,
    onRangeChange: (sectionId: String, values: ClosedFloatingPointRange<Float>) -> Unit = { _, _ -> },
    onDateBoundChange: (sectionId: String, isEnd: Boolean, date: LocalDate?) -> Unit = { _, _, _ -> },
    onRowClick: (rowId: String) -> Unit = {},
    clearAllEnabled: Boolean = appliedLabel != null,
) {
    val callbacks =
        FilterControlHandlers(
            onSingleSelect = onSingleSelect,
            onOptionClick = onOptionClick,
            onOptionLongClick = onOptionLongClick,
            optionLongClickLabel = optionLongClickLabel,
            onRangeChange = onRangeChange,
            onDateBoundChange = onDateBoundChange,
        )
    BingeSheetDockingHeader(
        header = {
            FilterSheetHeader(
                title = title,
                appliedLabel = appliedLabel,
                clearAllLabel = clearAllLabel,
                clearAllEnabled = clearAllEnabled,
                onClearAll = onClearAll,
            )
        },
        dockedTopBar = { onClose ->
            BingeSheetTopBar(
                title = title,
                subtitle = appliedLabel,
                onClose = onClose,
                actions = { BingeTextButton(label = clearAllLabel, onClick = onClearAll, enabled = clearAllEnabled) },
            )
        },
    )
    Column(
        modifier =
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
    ) {
        sections.forEach { section ->
            FilterSheetSection(title = section.title) {
                FilterSectionControl(section = section, callbacks = callbacks)
            }
        }
        if (rows.isNotEmpty()) {
            FilterSheetSection(title = rowsTitle) {
                ItemGroup(
                    title = null,
                    rows =
                        rows.map { row ->
                            ListItem(icon = row.icon, label = row.title, detail = row.summary, onClick = { onRowClick(row.id) })
                        },
                )
            }
        }
    }
    // Drawn after the body, so while the sheet is part-way open the lifted footer covers it, not the reverse.
    Column(
        modifier = Modifier.bingeSheetPinnedFooter(LocalBingeSheetDock.current, MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        BingeActionFooter(label = showResultsLabel, onClick = onApply)
    }
}

@Composable
private fun FilterSheetHeader(
    title: String,
    appliedLabel: String?,
    clearAllLabel: String,
    clearAllEnabled: Boolean,
    onClearAll: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = dimensionResource(R.dimen.filter_sheet_padding_h),
                    vertical = dimensionResource(R.dimen.padding_sm),
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            if (appliedLabel != null) {
                Text(
                    text = appliedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_xxs)),
                )
            }
        }
        BingeTextButton(label = clearAllLabel, onClick = onClearAll, enabled = clearAllEnabled)
    }
}

/** A titled group in the body; a null [title] leaves the heading out. */
@Composable
private fun FilterSheetSection(title: String?, content: @Composable () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.filter_sheet_padding_h),
                    end = dimensionResource(R.dimen.filter_sheet_padding_h),
                    top = dimensionResource(R.dimen.filter_sheet_section_padding_top),
                    bottom = dimensionResource(R.dimen.filter_sheet_section_padding_bottom),
                ),
    ) {
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(dimensionResource(R.dimen.filter_sheet_section_title_gap)))
        }
        content()
    }
}
