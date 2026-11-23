package com.binge.designsystem.component

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A null title draws the choice list alone, so a caller's own header names the pick once. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeChoiceSheetNoHeaderTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val choices = BingeChoiceList.Ready(listOf(BingeChoice("a", "Alpha"), BingeChoice("b", "Beta")))

    @Test
    fun `a single-choice content with a null title draws the rows and no title`() {
        rule.setContent {
            TestTheme { BingeChoiceSheetContent(title = null, choices = choices, selected = "a", onSelect = {}, subtitle = "Subtitle") }
        }

        rule.onNodeWithText("Alpha").assertExists()
        rule.onNodeWithText("Beta").assertExists()
        rule.onAllNodesWithText("Subtitle").assertCountEquals(0)
    }

    @Test
    fun `a multi-choice content with a null title draws the rows and no Clear button`() {
        rule.setContent {
            TestTheme {
                BingeMultiChoiceSheetContent(
                    title = null,
                    choices = choices,
                    chosen = setOf("a"),
                    onCheckedChange = { _, _ -> },
                    clearLabel = "Clear",
                    onClear = {},
                )
            }
        }

        rule.onNodeWithText("Alpha").assertExists()
        rule.onAllNodesWithText("Clear").assertCountEquals(0)
    }

    @Test
    fun `a titled content still draws its title`() {
        rule.setContent {
            TestTheme { BingeChoiceSheetContent(title = "Pick one", choices = choices, selected = null, onSelect = {}) }
        }

        rule.onNodeWithText("Pick one").assertExists()
    }
}
