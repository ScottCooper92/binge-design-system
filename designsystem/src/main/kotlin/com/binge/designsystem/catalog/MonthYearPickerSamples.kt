package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.MonthYearPickerContent
import com.binge.designsystem.component.MonthYearPickerMode
import com.binge.designsystem.component.MonthYearSelection
import com.binge.designsystem.preview.ScreenshotTheme
import java.time.Month
import java.util.Locale

private val SampleYears = 1990..2030
private const val SAMPLE_YEAR = 2018

@Composable
private fun MonthYearPickerSampleFrame(
    mode: MonthYearPickerMode,
    selection: MonthYearSelection,
    yearsOpen: Boolean = false,
) {
    ScreenshotTheme {
        MonthYearPickerContent(
            title = "From",
            mode = mode,
            selection = selection,
            yearRange = SampleYears,
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
