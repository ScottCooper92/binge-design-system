package com.binge.designsystem.tv.component

import java.util.Locale

/**
 * A divider, not a comma, between the included and excluded clauses of a summary: a comma would read as one list
 * and make "not" appear to govern only the value it precedes.
 */
private const val SUMMARY_SEPARATOR = " · "

/**
 * The category's chosen values, or [noneLabel] when it has none, for the row's second line.
 *
 * Exclusions must appear, or a category that only excludes reads as "Any" — wrong for "everything except
 * horror". [TvFilterCategory.pinnedOptions] is not read: a control is not a choice. Shared with the filter pane.
 */
fun TvFilterCategory.summary(
    noneLabel: String,
    excludedFormat: String,
    separator: String,
): String {
    val included = values.filter { it.state == TvFilterValueState.Included }
    val excluded = values.filter { it.state == TvFilterValueState.Excluded }
    val parts = buildList {
        included.takeIf { it.isNotEmpty() }?.let { add(it.joinToString(separator) { value -> value.label }) }
        excluded
            .takeIf { it.isNotEmpty() }
            ?.let { add(excludedFormat.format(Locale.getDefault(), it.joinToString(separator) { value -> value.label })) }
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = SUMMARY_SEPARATOR) ?: noneLabel
}
