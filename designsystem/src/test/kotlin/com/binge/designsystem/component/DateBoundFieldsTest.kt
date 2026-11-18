package com.binge.designsystem.component

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.YearMonth

/**
 * The From/To fields: what a set field shows, that only a set one has a clear button and that it clears, and that a
 * picked month becomes the first or last day of it. The picker itself is a modal window Robolectric does not route
 * touches into, so [MonthYearPickerWiringTest] covers its controls and the mapping from a month is tested directly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
class DateBoundFieldsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val fromChanges = mutableListOf<LocalDate?>()
    private val toChanges = mutableListOf<LocalDate?>()

    private fun show(from: LocalDate?, to: LocalDate?) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeDateBoundFields(
                    from = from,
                    to = to,
                    fromLabel = "From",
                    toLabel = "To",
                    fromClearDescription = "Clear From",
                    toClearDescription = "Clear To",
                    onFromChange = { fromChanges += it },
                    onToChange = { toChanges += it },
                    fromMaximum = to,
                    toMinimum = from,
                    today = LocalDate.of(2025, 6, 15),
                )
            }
        }
    }

    @Test
    fun `empty fields show their labels and no clear button`() {
        show(from = null, to = null)

        rule.onNodeWithText("From").assertIsDisplayed()
        rule.onNodeWithText("To").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Clear From").assertCountEquals(0)
        rule.onAllNodesWithContentDescription("Clear To").assertCountEquals(0)
    }

    @Test
    fun `a set field shows its month and year over its label`() {
        show(from = LocalDate.of(2018, 3, 1), to = null)

        rule.onNodeWithText("Mar 2018").assertIsDisplayed()
        rule.onNodeWithText("From").assertIsDisplayed()
        rule.onNodeWithContentDescription("Clear From").assertIsDisplayed()
        rule.onAllNodesWithContentDescription("Clear To").assertCountEquals(0)
    }

    @Test
    fun `clearing a field reports null for that field only`() {
        show(from = LocalDate.of(2018, 3, 1), to = LocalDate.of(2024, 9, 30))

        rule.onNodeWithContentDescription("Clear To").performClick()
        rule.waitForIdle()
        assertEquals(listOf<LocalDate?>(null), toChanges)
        assertEquals(emptyList<LocalDate?>(), fromChanges)

        rule.onNodeWithContentDescription("Clear From").performClick()
        rule.waitForIdle()
        assertEquals(listOf<LocalDate?>(null), fromChanges)
    }

    @Test
    fun `each field is announced as one button`() {
        show(from = null, to = null)

        rule.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)).assertCountEquals(2)
    }

    @Test
    fun `a picked month sets From to its first day and To to its last`() {
        val february = YearMonth.of(2024, 2)

        assertEquals(LocalDate.of(2024, 2, 1), february.asFromBound())
        assertEquals(LocalDate.of(2024, 2, 29), february.asToBound())
    }
}
