package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.MonthYearPickerContent
import com.binge.designsystem.component.MonthYearPickerMode
import com.binge.designsystem.component.MonthYearSelection
import com.binge.designsystem.preview.ScreenshotTheme
import java.time.Month
import java.time.YearMonth
import java.util.Locale

private val SampleYears = 1990..2030
private const val SAMPLE_YEAR = 2018

@Composable
private fun MonthYearPickerSampleFrame(
    mode: MonthYearPickerMode,
    selection: MonthYearSelection,
    yearsOpen: Boolean = false,
    minimum: YearMonth? = null,
    maximum: YearMonth? = null,
) {
    ScreenshotTheme {
        MonthYearPickerContent(
            title = "From",
            mode = mode,
            selection = selection,
            yearRange = SampleYears,
            minimum = minimum,
            maximum = maximum,
            yearsOpen = yearsOpen,
            onYearsOpenChange = {},
            onSelectionChange = {},
            onConfirm = {},
            onDismiss = {},
            locale = Locale.UK,
        )
    }
}

/** Year-only: a scrolling year grid, nothing chosen, so the confirm action is off. */
@Composable
fun MonthYearPickerYearEmptySample() {
    MonthYearPickerSampleFrame(MonthYearPickerMode.Year, MonthYearSelection())
}

/** Year-only with a year chosen: the grid opens scrolled to it and confirm is on. */
@Composable
fun MonthYearPickerYearSample() {
    MonthYearPickerSampleFrame(MonthYearPickerMode.Year, MonthYearSelection(year = SAMPLE_YEAR))
}

/** Month-only: twelve short month names with one chosen. */
@Composable
fun MonthYearPickerMonthSample() {
    MonthYearPickerSampleFrame(MonthYearPickerMode.Month, MonthYearSelection(month = Month.MARCH))
}

/** Both parts: the year stepper over the month grid, with a month and year chosen. */
@Composable
fun MonthYearPickerMonthAndYearSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SAMPLE_YEAR, month = Month.MARCH),
    )
}

/** Both parts, nothing chosen yet: the stepper shows the latest year and confirm is off. */
@Composable
fun MonthYearPickerMonthAndYearEmptySample() {
    MonthYearPickerSampleFrame(MonthYearPickerMode.MonthAndYear, MonthYearSelection())
}

/** Both parts with the year list open, the stepper arrows off while a year is being chosen. */
@Composable
fun MonthYearPickerYearsOpenSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SAMPLE_YEAR, month = Month.MARCH),
        yearsOpen = true,
    )
}

/** The stepper at the start of the range: the previous-year arrow is off. */
@Composable
fun MonthYearPickerRangeStartSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SampleYears.first, month = Month.JANUARY),
    )
}

/**
 * An end that must not precede its start (March 2018): the boundary year's earlier months are dimmed
 * and cannot be picked, and the previous-year arrow is off because no earlier year is offered.
 */
@Composable
fun MonthYearPickerBoundedSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SAMPLE_YEAR, month = Month.JUNE),
        minimum = YearMonth.of(SAMPLE_YEAR, Month.MARCH),
    )
}

/** The same bound with the year list open: years before the minimum are not offered. */
@Composable
fun MonthYearPickerBoundedYearsOpenSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SAMPLE_YEAR, month = Month.JUNE),
        yearsOpen = true,
        minimum = YearMonth.of(SAMPLE_YEAR, Month.MARCH),
    )
}

/** A start that must not follow its end (March 2018), seen from the boundary year: later months are dimmed. */
@Composable
fun MonthYearPickerMaximumSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SAMPLE_YEAR, month = Month.JANUARY),
        maximum = YearMonth.of(SAMPLE_YEAR, Month.MARCH),
    )
}

/**
 * A selection whose year the bounds have since moved past — the paired-field flow this dialog is
 * built for, where the other field's own bound changed after this one was picked. The headline
 * shows the same coerced year as the stepper and month grid below it, and confirm stays off.
 */
@Composable
fun MonthYearPickerStaleSelectionSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SampleYears.first, month = Month.JUNE),
        minimum = YearMonth.of(SAMPLE_YEAR, Month.MARCH),
    )
}

/**
 * A selection whose year stays in range but whose month has since fallen before the minimum: the
 * chosen month's cell is both selected (bordered pill) and dimmed/inert at once, the one combined
 * state new in this PR's `isEnabled` wiring.
 */
@Composable
fun MonthYearPickerSelectionDimmedSample() {
    MonthYearPickerSampleFrame(
        MonthYearPickerMode.MonthAndYear,
        MonthYearSelection(year = SAMPLE_YEAR, month = Month.JANUARY),
        minimum = YearMonth.of(SAMPLE_YEAR, Month.JUNE),
    )
}
