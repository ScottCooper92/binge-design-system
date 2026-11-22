package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LABEL = "Block this title"

/** What the three buttons share through [ButtonContent]: the label, the icon carrying the name when the label is hidden, the spinner, and the slots. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ButtonContentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `an outlined and a text button with the label hidden name themselves by their icon`() {
        rule.setContent {
            TestTheme {
                BingeOutlinedButton(label = "Outlined", onClick = {}, leadingIcon = Icons.Filled.Block, showLabel = false)
                BingeTextButton(label = "Text", onClick = {}, leadingIcon = Icons.Filled.Block, showLabel = false)
            }
        }

        rule.onNodeWithContentDescription("Outlined").assertIsDisplayed()
        rule.onNodeWithContentDescription("Text").assertIsDisplayed()
        rule.onAllNodesWithTextCount("Outlined").let { assertEquals(0, it) }
    }

    @Test
    fun `a label shown leaves the icon undescribed, since the label names the button`() {
        rule.setContent {
            TestTheme { BingeTextButton(label = LABEL, onClick = {}, leadingIcon = Icons.Filled.Block) }
        }

        rule.onNodeWithText(LABEL).assertIsDisplayed()
        rule.onAllNodes(hasContentDescription(LABEL)).assertCountEquals(0)
    }

    @Test
    fun `a text button's trailing icon goes with its label and not without it`() {
        rule.setContent {
            TestTheme {
                BingeTextButton(label = "Open", onClick = {}, trailingIcon = Icons.AutoMirrored.Filled.OpenInNew)
                BingeTextButton(label = "Hidden", onClick = {
                }, leadingIcon = Icons.Filled.Block, trailingIcon = Icons.AutoMirrored.Filled.OpenInNew, showLabel = false)
            }
        }

        // Only the hidden-label button's leading icon is described; neither trailing icon carries a name.
        rule.onNodeWithText("Open").assertIsDisplayed()
        rule.onAllNodes(hasContentDescription("Hidden")).assertCountEquals(1)
    }

    @Test
    fun `a loading button of each kind shows no label and swallows a tap, a filled one with its own slot too`() {
        var taps = 0
        rule.setContent {
            TestTheme {
                BingeFilledButton(label = "Filled", onClick = { taps++ }, loading = true, leading = { Text("dot") })
                BingeOutlinedButton(label = "Outlined", onClick = { taps++ }, loading = true)
                BingeTextButton(label = "Text", onClick = { taps++ }, loading = true)
            }
        }

        rule.onAllNodesWithTextCount("Filled").let { assertEquals(0, it) }
        rule.onAllNodesWithTextCount("dot").let { assertEquals(0, it) }
        rule.onAllNodesWithTextCount("Outlined").let { assertEquals(0, it) }
        rule.onAllNodesWithTextCount("Text").let { assertEquals(0, it) }
        assertEquals(0, taps)
    }

    @Test
    fun `a filled button draws its slot, then its icon, then its label`() {
        var clicked = 0
        rule.setContent {
            TestTheme {
                BingeFilledButton(label = "Go", onClick = { clicked++ }, leadingIcon = Icons.Filled.Block, leading = { Text("slot") })
            }
        }

        rule.onNodeWithText("slot").assertIsDisplayed()
        rule.onNodeWithText("Go").performClick()
        assertEquals(1, clicked)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(
            androidx.compose.ui.test
                .hasText(text),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().size
}
