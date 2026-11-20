package com.binge.designsystem.tv.template

import androidx.compose.runtime.Immutable

/**
 * The words the filter pane draws, which the app owns: the header line, the count row of a multi-select, the scale's
 * bound tabs and the "Any" that stands for no choice.
 *
 * [filterCount] and [selectedCount] are handed the count and return the whole phrase, so the app words the plural
 * ("2 filters", "1 selected"). [excludedFormat] has one `%1$s` for what is ruled out ("not %1$s").
 */
@Immutable
class TvFilterPaneCopy(
    val noFilters: String,
    val filterCount: (Int) -> String,
    val noneSelected: String,
    val selectedCount: (Int) -> String,
    val clear: String,
    val any: String,
    val excludedFormat: String,
    val minimum: String,
    val maximum: String,
)
