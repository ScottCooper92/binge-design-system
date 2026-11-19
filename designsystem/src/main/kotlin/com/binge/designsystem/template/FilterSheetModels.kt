package com.binge.designsystem.template

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.LocalDate

/** One choice in a filter section: a stable [id] the template hands back, the words to show and an optional leading [icon]. */
@Immutable
data class FilterOptionUi(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
)

/** The control a [FilterSectionUi] draws inline in the sheet's body. */
sealed interface FilterControlUi {
    /** Exactly one of [options] is chosen, or none while [selectedId] is null. Drawn as [com.binge.designsystem.component.BingeChoiceChip]s. */
    @Immutable
    data class SingleChoice(
        val options: List<FilterOptionUi>,
        val selectedId: String?,
    ) : FilterControlUi

    /** Any of [options] can be included or excluded, as include/exclude chips. An id in neither set is off. */
    @Immutable
    data class MultiChoice(
        val options: List<FilterOptionUi>,
        val includedIds: Set<String>,
        val excludedIds: Set<String>,
    ) : FilterControlUi

    /**
     * A two-thumb slider over [valueRange] in [steps] steps, the chosen [values] read above it. A thumb at the top reads
     * [openEndLabel] ("240+ min") when there is one. [valueLabel] words a value; the two descriptions are what a screen
     * reader hears on each thumb.
     */
    @Immutable
    data class Range(
        val values: ClosedFloatingPointRange<Float>,
        val valueRange: ClosedFloatingPointRange<Float>,
        val steps: Int,
        val valueLabel: (Float) -> String,
        val startThumbDescription: String,
        val endThumbDescription: String,
        val openEndLabel: String? = null,
    ) : FilterControlUi

    /**
     * Quick [presets] as chips with the pair of From and To fields under them, headed [customTitle], so the fields read as a
     * second way to set the same range. A preset is chosen when [selectedPresetId] names it.
     */
    @Immutable
    data class DateRange(
        val presets: List<FilterOptionUi>,
        val selectedPresetId: String?,
        val customTitle: String,
        val bounds: FilterDateBoundsUi,
    ) : FilterControlUi
}

/** The From and To fields of a [FilterControlUi.DateRange]: the dates, the words, and the picker's limits. */
@Immutable
data class FilterDateBoundsUi(
    val from: LocalDate?,
    val to: LocalDate?,
    val fromLabel: String,
    val toLabel: String,
    val emptyLabel: String,
    val fromClearDescription: String,
    val toClearDescription: String,
    val today: LocalDate,
    val yearRange: IntRange? = null,
    val valueLabel: ((date: LocalDate, isEnd: Boolean) -> String)? = null,
)

/** A titled group of controls in the sheet's body. The [id] is what a callback names it by. */
@Immutable
data class FilterSectionUi(
    val id: String,
    val title: String,
    val control: FilterControlUi,
)

/** A filter that opens in a detail of its own: its [title], its current value in words as [summary], and a leading [icon]. */
@Immutable
data class FilterRowUi(
    val id: String,
    val title: String,
    val summary: String,
    val icon: ImageVector,
)
