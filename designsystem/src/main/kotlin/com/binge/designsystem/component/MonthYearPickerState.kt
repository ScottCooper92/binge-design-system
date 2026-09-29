package com.binge.designsystem.component

import com.binge.designsystem.fullMonthName
import java.time.Month
import java.util.Locale

/** Which parts of a date a [MonthYearPickerDialog] asks for. */
enum class MonthYearPickerMode {
    Year,
    Month,
    MonthAndYear,
    ;

    val asksYear: Boolean get() = this != Month
    val asksMonth: Boolean get() = this != Year
}

/** What the picker holds; a part the [MonthYearPickerMode] does not ask for stays `null`. */
data class MonthYearSelection(
    val year: Int? = null,
    val month: Month? = null,
) {
    /** Whether every part [mode] asks for has been chosen, which is what enables the confirm action. */
    fun isCompleteFor(mode: MonthYearPickerMode): Boolean = (!mode.asksYear || year != null) && (!mode.asksMonth || month != null)
}

/** The year the stepper shows: the chosen one, else the latest the range offers. */
internal fun MonthYearSelection.displayYear(range: IntRange): Int = (year ?: range.last).coerceIn(range)

/**
 * Picking a month while the picker also asks for a year pins the year the stepper was showing, so
 * what the user saw is what they get.
 */
internal fun MonthYearSelection.pickMonth(
    month: Month,
    mode: MonthYearPickerMode,
    range: IntRange,
): MonthYearSelection = if (mode.asksYear) copy(month = month, year = displayYear(range)) else copy(month = month)

internal fun MonthYearSelection.pickYear(year: Int): MonthYearSelection = copy(year = year)

internal fun canStepYear(
    current: Int,
    delta: Int,
    range: IntRange,
): Boolean = current + delta in range

internal fun MonthYearSelection.stepYear(delta: Int, range: IntRange): MonthYearSelection =
    if (canStepYear(displayYear(range), delta, range)) copy(year = displayYear(range) + delta) else this

/**
 * The first cell to scroll the year grid to, one row above the one holding [year] so it is not
 * pinned to the top edge. With no year chosen it lands on the range's last row.
 */
internal fun yearGridStartIndex(
    year: Int?,
    range: IntRange,
    columns: Int,
): Int {
    val target = (year ?: range.last).coerceIn(range) - range.first
    return ((target / columns) - 1).coerceAtLeast(0) * columns
}

/**
 * What the header shows for the current pick: the parts [mode] asks for, in reading order, with
 * [empty] standing in for one not chosen yet, and [empty] alone when nothing is.
 */
internal fun pickerHeadline(
    mode: MonthYearPickerMode,
    selection: MonthYearSelection,
    locale: Locale,
    empty: String,
): String {
    val month = selection.month?.let { fullMonthName(it, locale) }
    val year = selection.year?.toString()
    return when (mode) {
        MonthYearPickerMode.Year -> year ?: empty
        MonthYearPickerMode.Month -> month ?: empty
        MonthYearPickerMode.MonthAndYear ->
            if (month == null && year == null) empty else "${month ?: empty} ${year ?: empty}"
    }
}
