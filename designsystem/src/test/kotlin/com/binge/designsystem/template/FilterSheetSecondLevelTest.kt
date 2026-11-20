package com.binge.designsystem.template

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.component.BingeSheetNav
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROW = "Release year"
private const val DETAIL = "The years page"

/** A filter row that opens a second level inside the sheet: it slides in as a page, and Back or the nav returns to the sheet. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h900dp")
class FilterSheetSecondLevelTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val rowClicks = mutableListOf<String>()
    private val left = mutableListOf<String>()
    private var nav: BingeSheetNav? = null
    private var back: OnBackPressedDispatcherOwner? = null

    private fun show(withDetail: Boolean) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                FilterSheet(
                    title = "Filters",
                    clearAllLabel = "Clear all",
                    showResultsLabel = "Show results",
                    sections = emptyList(),
                    rows = listOf(FilterRowUi("years", ROW, "Any", Icons.Filled.CalendarMonth)),
                    onDismiss = {},
                    onApply = {},
                    onClearAll = {},
                    onRowClick = { rowClicks += it },
                    detail =
                        if (withDetail) {
                            { rowId, handle ->
                                nav = handle
                                back = LocalOnBackPressedDispatcherOwner.current
                                Text("$DETAIL: $rowId")
                            }
                        } else {
                            null
                        },
                    onDetailBack = { left += it },
                )
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a row opens its page in the sheet, and the sheet is not shown under it`() {
        show(withDetail = true)

        rule.onNodeWithText(ROW).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("$DETAIL: years").assertExists()
        rule.onNodeWithText("Show results").assertDoesNotExist()
        assertEquals("the caller is still told which row was tapped", listOf("years"), rowClicks)
    }

    @Test
    fun `nav pop returns to the sheet and reports the row's page`() {
        show(withDetail = true)
        rule.onNodeWithText(ROW).performClick()
        rule.waitForIdle()

        rule.runOnUiThread { nav!!.pop() }
        rule.waitForIdle()

        rule.onNodeWithText("Show results").assertExists()
        rule.onNodeWithText("$DETAIL: years").assertDoesNotExist()
        assertEquals(listOf("years"), left)
    }

    @Test
    fun `system Back returns to the sheet`() {
        show(withDetail = true)
        rule.onNodeWithText(ROW).performClick()
        rule.waitForIdle()

        rule.runOnIdle { checkNotNull(back).onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()

        rule.onNodeWithText("Show results").assertExists()
        assertEquals(listOf("years"), left)
    }

    @Test
    fun `without a detail the row only reports the tap, as it always did`() {
        show(withDetail = false)

        rule.onNodeWithText(ROW).performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Show results").assertExists()
        assertEquals(listOf("years"), rowClicks)
        assertEquals(emptyList<String>(), left)
    }
}
