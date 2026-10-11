package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** The overview's toggle, which frames show only seeded (#394): it flips, and reports the expand alone. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ExpandableOverviewTest {
    @get:Rule
    val rule = createComposeRule()

    private val more = RuntimeEnvironment.getApplication().getString(R.string.detail_show_more)
    private val less = RuntimeEnvironment.getApplication().getString(R.string.detail_show_less)

    @Test
    fun `the toggle flips between more and less, and reports only the expand`() {
        var expands = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ExpandableOverview(text = "An overview.", initiallyOverflowing = true, onExpand = { expands++ })
            }
        }

        rule.onNodeWithText(more).performClick()
        rule.onNodeWithText(less).performClick()

        rule.onNodeWithText(more).assertExists()
        assertEquals(1, expands)
    }

    @Test
    fun `text that fits offers no toggle`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { ExpandableOverview(text = "Short.") }
        }

        rule.onNodeWithText(more).assertDoesNotExist()
    }

    /** The line count is not measurable here (Robolectric lays text out on one line), so the cap is read off the layout's input. */
    @Test
    fun `the toggle lifts the line cap and puts it back`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ExpandableOverview(text = TEXT, initiallyOverflowing = true)
            }
        }
        assertEquals(COLLAPSED_LINES, maxLines())

        rule.onNodeWithText(more).performClick()
        assertEquals(Int.MAX_VALUE, maxLines())

        rule.onNodeWithText(less).performClick()
        assertEquals(COLLAPSED_LINES, maxLines())
    }

    @Test
    fun `new text starts collapsed again`() {
        var text by mutableStateOf(TEXT)
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ExpandableOverview(text = text, initiallyOverflowing = true)
            }
        }
        rule.onNodeWithText(more).performClick()
        rule.onNodeWithText(less).assertExists()

        text = "$TEXT And another sentence."
        rule.waitForIdle()

        rule.onNodeWithText(more).assertExists()
        assertEquals(COLLAPSED_LINES, maxLines())
    }

    private fun maxLines(): Int {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNode(hasText(TEXT, substring = true))
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        return results.single().layoutInput.maxLines
    }

    private companion object {
        const val TEXT = "An overview that is long enough to need the toggle."
        const val COLLAPSED_LINES = 3
    }
}
