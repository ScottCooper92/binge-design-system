package com.binge.designsystem.component

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** A filter that matches nothing says so, in the single- and the multi-choice list. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeChoiceSheetNoMatchTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val choices = BingeChoiceList.Ready(listOf(BingeChoice(1, "Alpha"), BingeChoice(2, "Beta")))
    private val defaultLine = RuntimeEnvironment.getApplication().getString(R.string.choice_no_match)
    private val clear = RuntimeEnvironment.getApplication().getString(R.string.cd_clear_query)

    private fun single(noMatchLabel: String? = null) {
        rule.setContent {
            TestTheme {
                SingleChoiceList(choices = choices, selected = 1, filterPlaceholder = "Filter", noMatchLabel = noMatchLabel) {}
            }
        }
    }

    private fun multi(noMatchLabel: String? = null) {
        rule.setContent {
            TestTheme {
                MultiChoiceList(
                    choices = choices,
                    chosen = setOf(1),
                    leading = setOf(1),
                    filterPlaceholder = "Filter",
                    noMatchLabel = noMatchLabel,
                    onCheckedChange = { _, _ -> },
                )
            }
        }
    }

    private fun type(text: String) {
        rule.onNode(hasSetTextAction()).performTextInput(text)
        rule.waitForIdle()
    }

    private val isPolite = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

    @Test
    fun `a single-choice filter that matches nothing says the default line, politely`() {
        single()
        type("zzz")

        rule.onNodeWithText(defaultLine).assert(isPolite)
        rule.onNodeWithText("Alpha").assertDoesNotExist()
    }

    @Test
    fun `a multi-choice filter that matches nothing says the caller's line`() {
        multi(noMatchLabel = "No languages match")
        type("zzz")

        rule.onNodeWithText("No languages match").assert(isPolite)
        rule.onNodeWithText(defaultLine).assertDoesNotExist()
    }

    @Test
    fun `clearing the filter brings the rows back and the line goes`() {
        single()
        type("zzz")
        rule.onNodeWithContentDescription(clear).performClick()
        rule.waitForIdle()

        rule.onNodeWithText(defaultLine).assertDoesNotExist()
        rule.onNodeWithText("Alpha").assertExists()
    }

    @Test
    fun `a filter that matches a row shows no line`() {
        multi()
        type("alp")

        rule.onNodeWithText(defaultLine).assertDoesNotExist()
        rule.onNodeWithText("Alpha").assertExists()
    }

    @Test
    fun `with nothing typed there is no line, even for an empty list`() {
        rule.setContent {
            TestTheme {
                SingleChoiceList(
                    choices = BingeChoiceList.Ready(emptyList<BingeChoice<Int>>()),
                    selected = null,
                    filterPlaceholder = "Filter",
                ) {}
            }
        }

        rule.onNodeWithText(defaultLine).assertDoesNotExist()
    }
}
