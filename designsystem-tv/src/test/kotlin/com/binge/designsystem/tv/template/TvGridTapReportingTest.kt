package com.binge.designsystem.tv.template

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val ITEMS = List(3) { GridItem(id = 100 + it, title = "Title $it") }

/**
 * A grid cell reports its **position** when pressed, alongside the routing click that takes the item.
 *
 * The two callbacks exist because the same press answers two questions, and only one of them may carry an id:
 * `onItemClick` routes by `GridItem.kind`, `onItemTapped` reports where in the results the cell was. A grid
 * that fired them out of step — or reported the focused cell rather than the pressed one — would be invisible
 * to a screenshot and wrong only in the dashboard, which is the same shape of failure an earlier fix found for clicks
 * themselves.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvGridTapReportingTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val tapped = mutableListOf<Int>()
    private val clicked = mutableListOf<GridItem>()

    private fun press(cell: Int) {
        composeTestRule.setContent {
            BingeTvTheme {
                TestGridPage(
                    heading = "Discover",
                    results = gridItems(ITEMS),
                    onItemClick = clicked::add,
                    onItemTapped = tapped::add,
                    onRetry = {},
                    emptyMessage = "Nothing here",
                )
            }
        }
        // Matched by click action and taken in composition order — the cell's own text sits on an
        // unmerged child, so a text finder resolves to a node with no focus or click action on it.
        val cells = composeTestRule.onAllNodes(hasClickAction())
        // Entry, then → to the target. An explicit requestFocus() on a later cell is redirected by the
        // pane's tvEntryFocusGroup to wherever the group routes entry, so it would press cell 0
        // however far along the fixture aimed — walking there is both correct and the real interaction.
        val entry = cells[0]
        entry.requestFocus()
        composeTestRule.waitForIdle()
        entry.assertIsFocused()
        repeat(cell) {
            entry.performKeyInput { pressKey(Key.DirectionRight) }
            composeTestRule.waitForIdle()
        }
        cells[cell].assertIsFocused()
        cells[cell].performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()
    }

    /** The first cell, where an off-by-one would still look plausible in a dashboard. */
    @Test
    fun `pressing the first cell reports position zero`() {
        press(cell = 0)

        assertEquals(listOf(0), tapped)
    }

    /** A cell further in, so the report is the pressed position rather than a constant or the entry index. */
    @Test
    fun `pressing a later cell reports its own position`() {
        press(cell = 2)

        assertEquals(listOf(2), tapped)
    }

    /**
     * Both fire for one press, and describe the same cell. A report that arrived without the navigation (or
     * the other way round) would still pass either assertion above on its own.
     */
    @Test
    fun `the report and the navigation describe the same press`() {
        press(cell = 1)

        assertEquals(listOf(1), tapped)
        assertEquals(listOf(ITEMS[1]), clicked)
    }
}
