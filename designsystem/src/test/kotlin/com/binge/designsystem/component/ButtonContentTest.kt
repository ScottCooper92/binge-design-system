package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LABEL = "Block this title"
private val ICON_SIZE = 20.dp

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
    fun `a loading button of each kind shows no label and takes no tap`() {
        var taps = 0
        rule.setContent {
            TestTheme {
                Column {
                    BingeFilledButton(label = "Filled", onClick = { taps++ }, loading = true, leading = { Text("dot") })
                    BingeOutlinedButton(label = "Outlined", onClick = { taps++ }, loading = true)
                    BingeTextButton(label = "Text", onClick = { taps++ }, loading = true)
                }
            }
        }

        rule.onAllNodesWithTextCount("Filled").let { assertEquals(0, it) }
        rule.onAllNodesWithTextCount("dot").let { assertEquals(0, it) }
        rule.onAllNodesWithTextCount("Outlined").let { assertEquals(0, it) }
        rule.onAllNodesWithTextCount("Text").let { assertEquals(0, it) }
        val buttons = rule.onAllNodes(hasClickAction())
        buttons.assertCountEquals(3)
        (0 until 3).forEach { buttons[it].performClick() }
        assertEquals("a loading button must not forward a tap", 0, taps)
    }

    @Test
    fun `a filled button's icon sits between its slot and its label, and a tap on it fires once`() {
        var clicked = 0
        rule.setContent {
            TestTheme {
                Column {
                    BingeFilledButton(label = "Plain", onClick = { clicked++ }, leading = { Text("slotA") })
                    BingeFilledButton(label = "Iconed", onClick = {}, leadingIcon = Icons.Filled.Block, leading = { Text("slotB") })
                }
            }
        }

        fun gap(slot: String, label: String) =
            rule.onNodeWithText(label, useUnmergedTree = true).getBoundsInRoot().left -
                rule.onNodeWithText(slot, useUnmergedTree = true).getBoundsInRoot().right
        // Both read slot, then label; the second has an icon between them, which is wider than the bare spacing by the icon's own width.
        assertTrue("the label follows the slot", gap("slotA", "Plain") > 0.dp)
        assertTrue("the icon takes room between the slot and the label", gap("slotB", "Iconed") - gap("slotA", "Plain") >= ICON_SIZE)
        rule.onNodeWithText("Plain").performClick()
        assertEquals(1, clicked)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(
            androidx.compose.ui.test
                .hasText(text),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().size
}
