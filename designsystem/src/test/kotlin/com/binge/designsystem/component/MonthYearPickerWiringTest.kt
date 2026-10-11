package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Month
import java.util.Locale

/**
 * The picker's controls reach the selection it confirms: the year stepper, the year grid and a month cell (#394). The
 * body is hosted as [MonthYearPickerDialog] hosts it, without the modal window, which Robolectric does not route
 * touches into. The screen is tall enough that the actions under the grid are on it, where a click can land.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w600dp-h1200dp")
class MonthYearPickerWiringTest {
    @get:Rule
    val rule = createComposeRule()

    private val confirmed = mutableListOf<MonthYearSelection>()
    private var dismissals = 0

    @Test
    fun `stepping the year and picking a month confirms both`() {
        show()

        rule.onNodeWithContentDescription("Next year").performClick()
        rule.onNodeWithContentDescription("Next year").performClick()
        rule.onNodeWithText("Mar").performClick()
        rule.onNodeWithText("OK").performClick()
        rule.waitForIdle()

        assertEquals(listOf(MonthYearSelection(2022, Month.MARCH)), confirmed)
    }

    @Test
    fun `a year picked from the grid is the year confirmed`() {
        show()

        rule.onNodeWithContentDescription("Choose year").performClick()
        rule.onNodeWithText("2021").performClick()
        rule.onNodeWithText("Jun").performClick()
        rule.onNodeWithText("OK").performClick()
        rule.waitForIdle()

        assertEquals(listOf(MonthYearSelection(2021, Month.JUNE)), confirmed)
    }

    @Test
    fun `cancel dismisses without confirming`() {
        show()

        rule.onNodeWithText("Mar").performClick()
        rule.onNodeWithText("Cancel").performClick()
        rule.waitForIdle()

        assertEquals(1, dismissals)
        assertEquals(emptyList<MonthYearSelection>(), confirmed)
    }

    private fun show() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                var selection by remember { mutableStateOf(MonthYearSelection()) }
                var yearsOpen by remember { mutableStateOf(false) }
                MonthYearPickerContent(
                    title = "From",
                    mode = MonthYearPickerMode.MonthAndYear,
                    selection = selection,
                    yearRange = 2010..2030,
                    defaultYear = 2020,
                    yearsOpen = yearsOpen,
                    onYearsOpenChange = { yearsOpen = it },
                    onSelectionChange = { selection = it },
                    onConfirm = { confirmed += selection },
                    onDismiss = { dismissals++ },
                    locale = Locale.US,
                )
            }
        }
        rule.waitForIdle()
    }
}
