package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
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

/**
 * The row follows its selection (#394): a selection that lands off-screen, as a swipe in the paired pager
 * makes it, is scrolled into view, and one already visible does not move the row.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterChipRowScrollTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var selected by mutableIntStateOf(0)

    private fun show() {
        selected = 0
        rule.setContent {
            TestTheme {
                Box(Modifier.width(ROW_WIDTH)) {
                    BingeFilterChipRow(
                        items = List(CHIPS) { FilterChipItem(label = label(it)) },
                        selectedIndex = selected,
                        onSelect = { selected = it },
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    private fun positionOf(label: String) = rule.onNodeWithText(label).fetchSemanticsNode().positionInRoot

    private fun widthOf(label: String) =
        rule
            .onNodeWithText(label)
            .fetchSemanticsNode()
            .size.width

    private fun label(index: Int) = "Filter number $index"

    @Test
    fun `a selection past the end of the row is scrolled into view`() {
        show()
        rule.onNodeWithText(label(0)).assertIsDisplayed()

        selected = CHIPS - 1
        rule.waitForIdle()

        rule.onNodeWithText(label(CHIPS - 1)).assertIsDisplayed()
    }

    @Test
    fun `a selection that is already visible leaves the row where it is`() {
        show()

        selected = 1
        rule.waitForIdle()

        rule.onNodeWithText(label(1)).assertIsDisplayed()
        // Unclipped, so a row that did scroll shows as the first chip's left edge going negative.
        assertTrue(
            rule
                .onNodeWithText(label(0))
                .fetchSemanticsNode()
                .positionInRoot.x >= 0f,
        )
    }

    @Test
    fun `an icon renders before the label and the row stays one scrolling row`() {
        var withIcon by mutableStateOf(false)
        rule.setContent {
            TestTheme {
                Box(Modifier.width(ROW_WIDTH)) {
                    BingeFilterChipRow(
                        items = List(CHIPS) {
                            FilterChipItem(label = label(it), icon = if (withIcon && it == 0) Icons.Filled.Star else null)
                        },
                        selectedIndex = 0,
                        onSelect = {},
                    )
                }
            }
        }
        rule.waitForIdle()
        val plainWidth = widthOf(label(0))
        val plainSecondY = positionOf(label(1)).y

        rule.runOnIdle { withIcon = true }
        rule.waitForIdle()

        // The icon claims width beside the label, so the chip is wider than the same chip without one.
        val iconWidth = widthOf(label(0))
        assertTrue("chip should widen for the icon: $plainWidth -> $iconWidth", iconWidth > plainWidth)
        // Still one row: the next chip sits at the same height.
        assertEquals(plainSecondY, positionOf(label(1)).y, 1f)

        // A chip past the edge is reached by scrolling the same row.
        rule.onNodeWithText(label(CHIPS - 1)).assertIsNotDisplayed()
        rule.onNode(hasScrollAction()).performScrollToIndex(CHIPS - 1)
        rule.waitForIdle()
        rule.onNodeWithText(label(CHIPS - 1)).assertIsDisplayed()
    }

    private companion object {
        val ROW_WIDTH = 240.dp
        const val CHIPS = 12
    }
}
