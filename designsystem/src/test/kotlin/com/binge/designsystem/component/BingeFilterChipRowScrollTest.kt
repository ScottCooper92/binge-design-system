package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
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
    val rule = createComposeRule()

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

    private companion object {
        val ROW_WIDTH = 240.dp
        const val CHIPS = 12
    }
}
