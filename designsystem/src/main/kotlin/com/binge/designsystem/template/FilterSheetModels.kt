package com.binge.designsystem.template

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector

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
}

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
