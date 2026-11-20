package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.settle
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val COLUMNS = 5

// The cell the user leaves from: row 1, column 2. Off the first row *and* off the first column, so a
// geometric → return from the left (the pre-fix behaviour) can never coincide with it — the rail enters the
// grid at column 0, never column 2.
private const val CELL = 7

private const val TAG_RAIL = "rail"

/**
 * **A rail round trip lands back on the exact cell, on a grid with no focus-keeper (#1491).**
 *
 * `TvGridPage`/`TvMediaGrid` used to install their entry-redirect focus group only when the caller supplied an
 * `entryFocusRequester` — which the see-all, collection and list-detail grids do not. So → back from the rail
 * was a raw geometric search that landed on whatever cell aligned with the rail item, not the one the user was
 * on; the surface then re-anchored its backdrop to the wrong cell. The pane's memory was alive the whole time
 * and simply never consulted.
 *
 * The fix makes the group unconditional, which reverses a documented decision — a focus group is a boundary
 * for 2D search, and the prior comment's stated risk was that an *always-on* group could trap ← in the first
 * column. So the two directions are tested apart, because a single left-rail round trip cannot discriminate
 * both: a ← walk to the rail legitimately moves the remembered cell *into* column 0, where the geometric →
 * return also enters, so the two coincide and the return assertion stops discriminating.
 *
 * - [a left walk out of the grid reaches the rail] presses ← through the whole first column and asserts the
 *   rail gains focus — the guard for the reversed decision. This is the only place a group that trapped ←
 *   would surface; a screenshot renders focus as a parameter and would show nothing.
 * - [returning from the rail lands on the remembered cell] leaves from a column-2 cell (so the memory stays
 *   off column 0) and asserts the → return lands on it. Mutation-checked: with the group gated on
 *   `entryFocusRequester` again the → resolves geometrically to column 0 and this fails.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvGridPageRailReturnTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @Test
    fun `a left walk out of the grid reaches the rail`() {
        setContentWithRail()

        cell(CELL).requestFocus()
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        cell(CELL).assertIsFocused()

        // Real ← keypresses through the whole first column, not a seeded jump. If the unconditional focus group
        // trapped ← in column 0 — the risk the prior conditional guard named — the rail never gains focus and
        // the bounded walk exits without reaching it, failing here.
        pressLeftUntilRailFocused()
        composeTestRule.onNodeWithTag(TAG_RAIL).assertIsFocused()
    }

    @Test
    fun `returning from the rail lands on the remembered cell`() {
        setContentWithRail()

        cell(CELL).requestFocus()
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        cell(CELL).assertIsFocused()

        // Seeded rather than a ← walk on purpose: a walk would drag the remembered cell into column 0, where the
        // geometric return also lands, and the assertion below would pass with or without the fix. Leaving from
        // column 2 keeps the memory off column 0, so only the redirect can bring focus back to it.
        composeTestRule.onNodeWithTag(TAG_RAIL).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TAG_RAIL).performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        cell(CELL).assertIsFocused()
    }

    private fun setContentWithRail() {
        composeTestRule.setContent {
            BingeTvTheme {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(72.dp)
                            .testTag(TAG_RAIL)
                            .focusable(),
                    )
                    Pane(modifier = Modifier.fillMaxHeight().weight(1f))
                }
            }
        }
    }

    private fun pressLeftFromFocused() {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionLeft) }
        composeTestRule.waitForIdle()
    }

    private fun railIsFocused(): Boolean =
        composeTestRule.onAllNodes(hasTestTag(TAG_RAIL) and isFocused()).fetchSemanticsNodes().isNotEmpty()

    // Bounded so a grid that never releases ← (the trap this guards against) exits the loop and fails on the
    // rail assertion rather than looping forever.
    private fun pressLeftUntilRailFocused() {
        var guard = 0
        while (!railIsFocused() && guard < COLUMNS + 2) {
            pressLeftFromFocused()
            guard++
        }
    }

    /**
     * [CELL] is seeded as the pane's focus memory so `cell(CELL).requestFocus()` below actually lands there.
     *
     * Since #1727 the grid's entry group always resolves — to the remembered cell, or the first one — so a
     * request aimed at any *other* cell is an entry into the group and is redirected. Seeding the memory the
     * test is about makes the entry cell and the requested cell the same node, which is also the state these
     * cases model: a user who has already browsed to column 2 before stepping out to the rail.
     */
    @Composable
    private fun Pane(modifier: Modifier = Modifier) {
        TestGridPage(
            heading = "Discover",
            results = gridItems(CELLS),
            onItemClick = {},
            onRetry = {},
            emptyMessage = "Nothing matches",
            columns = COLUMNS,
            initialFocused = TvGridFocus(CELL, CELLS[CELL]),
            modifier = modifier,
        )
    }

    private fun cell(index: Int) = composeTestRule.onNodeWithContentDescription(CELLS[index].title)
}

// Distinct titles, because every finder here is by content description; three rows' worth so the seeded cell
// sits off row 0 with rows above and below it.
private val CELLS = List(COLUMNS * 3) { index ->
    GridItem(
        id = 3000 + index,
        title = "Poster $index",
    )
}
