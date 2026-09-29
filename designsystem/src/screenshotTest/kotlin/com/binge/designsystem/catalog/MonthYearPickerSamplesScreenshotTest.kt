package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ComponentPreviews

class MonthYearPickerSamplesScreenshotTest {
    @PreviewTest
    @ComponentPreviews
    @Composable
    fun YearEmpty() {
        MonthYearPickerYearEmptySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Year() {
        MonthYearPickerYearSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Month() {
        MonthYearPickerMonthSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MonthAndYear() {
        MonthYearPickerMonthAndYearSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun MonthAndYearEmpty() {
        MonthYearPickerMonthAndYearEmptySample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun YearsOpen() {
        MonthYearPickerYearsOpenSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun RangeStart() {
        MonthYearPickerRangeStartSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Bounded() {
        MonthYearPickerBoundedSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun BoundedYearsOpen() {
        MonthYearPickerBoundedYearsOpenSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun Maximum() {
        MonthYearPickerMaximumSample()
    }

    @PreviewTest
    @ComponentPreviews
    @Composable
    fun StaleSelection() {
        MonthYearPickerStaleSelectionSample()
    }
}
