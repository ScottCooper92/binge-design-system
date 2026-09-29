package com.binge.designsystem.component

import com.binge.designsystem.shortMonthName
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Month
import java.util.Locale

class MonthYearPickerStateTest {
    private val range = 1990..2030

    @Test
    fun `each mode is complete only once every part it asks for is chosen`() {
        val yearOnly = MonthYearSelection(year = 2018)
        val monthOnly = MonthYearSelection(month = Month.MARCH)
        val both = MonthYearSelection(year = 2018, month = Month.MARCH)

        assertTrue(yearOnly.isCompleteFor(MonthYearPickerMode.Year))
        assertFalse(monthOnly.isCompleteFor(MonthYearPickerMode.Year))
        assertTrue(monthOnly.isCompleteFor(MonthYearPickerMode.Month))
        assertFalse(yearOnly.isCompleteFor(MonthYearPickerMode.Month))
        assertTrue(both.isCompleteFor(MonthYearPickerMode.MonthAndYear))
        assertFalse(yearOnly.isCompleteFor(MonthYearPickerMode.MonthAndYear))
        assertFalse(monthOnly.isCompleteFor(MonthYearPickerMode.MonthAndYear))
        assertFalse(MonthYearSelection().isCompleteFor(MonthYearPickerMode.Year))
    }

    @Test
    fun `a mode says which parts it asks for`() {
        assertTrue(MonthYearPickerMode.Year.asksYear)
        assertFalse(MonthYearPickerMode.Year.asksMonth)
        assertFalse(MonthYearPickerMode.Month.asksYear)
        assertTrue(MonthYearPickerMode.Month.asksMonth)
        assertTrue(MonthYearPickerMode.MonthAndYear.asksYear)
        assertTrue(MonthYearPickerMode.MonthAndYear.asksMonth)
    }

    @Test
    fun `with no year chosen the stepper shows the latest year in range`() {
        assertEquals(2030, MonthYearSelection().displayYear(range))
        assertEquals(2018, MonthYearSelection(year = 2018).displayYear(range))
    }

    @Test
    fun `a year outside the range is clamped for display rather than shown`() {
        assertEquals(2030, MonthYearSelection(year = 2099).displayYear(range))
        assertEquals(1990, MonthYearSelection(year = 1800).displayYear(range))
    }

    @Test
    fun `picking a month pins the year the stepper was showing when a year is asked for`() {
        val picked = MonthYearSelection().pickMonth(Month.MAY, MonthYearPickerMode.MonthAndYear, range)

        assertEquals(MonthYearSelection(year = 2030, month = Month.MAY), picked)
        assertEquals(
            MonthYearSelection(year = 2018, month = Month.MAY),
            MonthYearSelection(year = 2018).pickMonth(Month.MAY, MonthYearPickerMode.MonthAndYear, range),
        )
    }

    @Test
    fun `picking a month in month-only mode leaves the year unset`() {
        val picked = MonthYearSelection().pickMonth(Month.MAY, MonthYearPickerMode.Month, range)

        assertNull(picked.year)
        assertEquals(Month.MAY, picked.month)
    }

    @Test
    fun `stepping moves the shown year by one and stops at the ends of the range`() {
        val mid = MonthYearSelection(year = 2018, month = Month.MARCH)

        assertEquals(2019, mid.stepYear(1, range).year)
        assertEquals(2017, mid.stepYear(-1, range).year)
        assertEquals(Month.MARCH, mid.stepYear(1, range).month)
        assertEquals(MonthYearSelection(year = 2030), MonthYearSelection(year = 2030).stepYear(1, range))
        assertEquals(MonthYearSelection(year = 1990), MonthYearSelection(year = 1990).stepYear(-1, range))
        assertFalse(canStepYear(2030, 1, range))
        assertFalse(canStepYear(1990, -1, range))
        assertTrue(canStepYear(2018, 1, range))
    }

    @Test
    fun `the year grid starts one row above the chosen year and never before the first row`() {
        assertEquals(0, yearGridStartIndex(1990, range, columns = 3))
        assertEquals(0, yearGridStartIndex(1994, range, columns = 3))
        assertEquals(3, yearGridStartIndex(1996, range, columns = 3))
        assertEquals(24, yearGridStartIndex(2018, range, columns = 3))
    }

    @Test
    fun `with no year chosen the year grid starts near the end of the range`() {
        assertEquals(yearGridStartIndex(2030, range, columns = 3), yearGridStartIndex(null, range, columns = 3))
    }

    @Test
    fun `month names follow the locale`() {
        assertEquals("Mar", shortMonthName(Month.MARCH, Locale.UK))
        assertEquals("mar", shortMonthName(Month.MARCH, Locale("es")).lowercase())
        assertEquals("ene", shortMonthName(Month.JANUARY, Locale("es")).lowercase().trimEnd('.'))
    }

    @Test
    fun `the headline shows the parts the mode asks for, with a dash for one not chosen`() {
        val both = MonthYearSelection(year = 2029, month = Month.MAY)

        assertEquals("2029", pickerHeadline(MonthYearPickerMode.Year, both, Locale.UK, "-"))
        assertEquals("May", pickerHeadline(MonthYearPickerMode.Month, both, Locale.UK, "-"))
        assertEquals("May 2029", pickerHeadline(MonthYearPickerMode.MonthAndYear, both, Locale.UK, "-"))
        assertEquals("-", pickerHeadline(MonthYearPickerMode.Year, MonthYearSelection(), Locale.UK, "-"))
        assertEquals("-", pickerHeadline(MonthYearPickerMode.Month, MonthYearSelection(), Locale.UK, "-"))
        assertEquals("-", pickerHeadline(MonthYearPickerMode.MonthAndYear, MonthYearSelection(), Locale.UK, "-"))
        assertEquals("- 2029", pickerHeadline(MonthYearPickerMode.MonthAndYear, MonthYearSelection(year = 2029), Locale.UK, "-"))
        assertEquals("May -", pickerHeadline(MonthYearPickerMode.MonthAndYear, MonthYearSelection(month = Month.MAY), Locale.UK, "-"))
    }

    @Test
    fun `the headline uses the full month name in the locale`() {
        assertEquals(
            "marzo 2018",
            pickerHeadline(MonthYearPickerMode.MonthAndYear, MonthYearSelection(2018, Month.MARCH), Locale("es"), "-").lowercase(),
        )
    }
}
