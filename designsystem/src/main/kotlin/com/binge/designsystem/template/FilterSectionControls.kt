package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeChoiceChip
import com.binge.designsystem.component.BingeDateBoundFields
import com.binge.designsystem.component.BingeRangeSlider
import com.binge.designsystem.component.BingeSearchField
import com.binge.designsystem.component.DateBoundFieldStyle
import com.binge.designsystem.component.IncludeExcludeChip
import com.binge.designsystem.component.IncludeExcludeState
import java.time.LocalDate

/**
 * What the controls of a filter section tell their caller, each naming the section and option by the id it was given. A
 * preset on a [FilterControlUi.DateRange] arrives as [onSingleSelect]. [optionLongClickLabel] is what a screen reader hears
 * as the long press on an include/exclude chip.
 */
class FilterControlHandlers(
    val onSingleSelect: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    val onOptionClick: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    val onOptionLongClick: (sectionId: String, optionId: String) -> Unit = { _, _ -> },
    val optionLongClickLabel: String? = null,
    val onRangeChange: (sectionId: String, values: ClosedFloatingPointRange<Float>) -> Unit = { _, _ -> },
    val onDateBoundChange: (sectionId: String, isEnd: Boolean, date: LocalDate?) -> Unit = { _, _, _ -> },
)

/**
 * [FilterSectionControl] under a search field, when the section has options to search and a [search] is given. The options shown are
 * the ones whose label contains the query, and [FilterSearchUi.noResultsLabel] stands in when none do. Anything else is drawn as it is.
 */
@Composable
internal fun FilterSectionControlWithSearch(
    section: FilterSectionUi,
    callbacks: FilterControlHandlers,
    search: FilterSearchUi?,
) {
    if (search == null || section.control !is FilterControlUi.SingleChoice && section.control !is FilterControlUi.MultiChoice) {
        FilterSectionControl(section = section, callbacks = callbacks)
        return
    }
    val shown = section.matching(search.query)
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.filter_sheet_chip_spacing))) {
        BingeSearchField(
            query = search.query,
            onQueryChange = search.onQueryChange,
            onClear = { search.onQueryChange("") },
            placeholder = search.placeholder,
        )
        if (shown.hasNoOptions()) {
            Text(
                text = search.noResultsLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            FilterSectionControl(section = shown, callbacks = callbacks)
        }
    }
}

/** This section with only the options whose label contains [query], ignoring case. A blank query keeps them all. */
internal fun FilterSectionUi.matching(query: String): FilterSectionUi {
    val needle = query.trim()
    if (needle.isEmpty()) return this

    fun List<FilterOptionUi>.keep() = filter { it.label.contains(needle, ignoreCase = true) }
    return when (val control = control) {
        is FilterControlUi.SingleChoice -> copy(control = control.copy(options = control.options.keep()))
        is FilterControlUi.MultiChoice -> copy(control = control.copy(options = control.options.keep()))
        else -> this
    }
}

private fun FilterSectionUi.hasNoOptions(): Boolean =
    when (val control = control) {
        is FilterControlUi.SingleChoice -> control.options.isEmpty()
        is FilterControlUi.MultiChoice -> control.options.isEmpty()
        else -> false
    }

/** The control a [FilterSectionUi] carries, drawn the same inline in the sheet's body and alone in a section sheet. */
@Composable
internal fun FilterSectionControl(section: FilterSectionUi, callbacks: FilterControlHandlers) {
    when (val control = section.control) {
        is FilterControlUi.SingleChoice ->
            ChipFlow(singleChoice = true) {
                control.options.forEach { option ->
                    BingeChoiceChip(
                        label = option.label,
                        selected = option.id == control.selectedId,
                        onClick = { callbacks.onSingleSelect(section.id, option.id) },
                        icon = option.icon,
                    )
                }
            }

        is FilterControlUi.MultiChoice ->
            ChipFlow(singleChoice = false) {
                control.options.forEach { option ->
                    IncludeExcludeChip(
                        label = option.label,
                        state =
                            when (option.id) {
                                in control.includedIds -> IncludeExcludeState.Include
                                in control.excludedIds ->
                                    if (control.includeOnly) IncludeExcludeState.Neutral else IncludeExcludeState.Exclude
                                else -> IncludeExcludeState.Neutral
                            },
                        onClick = { callbacks.onOptionClick(section.id, option.id) },
                        onLongClick = if (control.includeOnly) null else ({ callbacks.onOptionLongClick(section.id, option.id) }),
                        onLongClickLabel = callbacks.optionLongClickLabel,
                    )
                }
            }

        is FilterControlUi.Range ->
            BingeRangeSlider(
                values = control.values,
                onValuesChange = { callbacks.onRangeChange(section.id, it) },
                valueRange = control.valueRange,
                steps = control.steps,
                valueLabel = control.valueLabel,
                startThumbDescription = control.startThumbDescription,
                endThumbDescription = control.endThumbDescription,
                openEndLabel = control.openEndLabel,
            )

        is FilterControlUi.DateRange -> DateRangeControl(section.id, control, callbacks)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(singleChoice: Boolean, content: @Composable () -> Unit) {
    val spacing = dimensionResource(R.dimen.filter_sheet_chip_spacing)
    FlowRow(
        // The chips of a single choice are radio buttons, so a screen reader reads them as one group.
        modifier = Modifier.fillMaxWidth().then(if (singleChoice) Modifier.selectableGroup() else Modifier),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) { content() }
}

@Composable
private fun DateRangeControl(
    sectionId: String,
    control: FilterControlUi.DateRange,
    callbacks: FilterControlHandlers,
) {
    val bounds = control.bounds
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.filter_sheet_chip_spacing))) {
        // Wraps rather than scrolls: in its own sheet there is room for every preset, and none hides off the edge.
        ChipFlow(singleChoice = true) {
            control.presets.forEach { preset ->
                BingeChoiceChip(
                    label = preset.label,
                    selected = preset.id == control.selectedPresetId,
                    onClick = { callbacks.onSingleSelect(sectionId, preset.id) },
                    icon = preset.icon,
                )
            }
        }
        Text(
            text = control.customTitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = dimensionResource(R.dimen.filter_sheet_chip_spacing)),
        )
        BingeDateBoundFields(
            from = bounds.from,
            to = bounds.to,
            fromLabel = bounds.fromLabel,
            toLabel = bounds.toLabel,
            fromClearDescription = bounds.fromClearDescription,
            toClearDescription = bounds.toClearDescription,
            onFromChange = { callbacks.onDateBoundChange(sectionId, false, it) },
            onToChange = { callbacks.onDateBoundChange(sectionId, true, it) },
            fromMaximum = bounds.to,
            toMinimum = bounds.from,
            today = bounds.today,
            style = DateBoundFieldStyle.Outlined,
            emptyLabel = bounds.emptyLabel,
            valueLabel = bounds.valueLabel,
            yearRange = bounds.yearRange,
        )
    }
}
