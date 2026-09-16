package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val TRAILING = "trailing"
private const val CELL_WIDTH_DP = 40
private const val CELL_COUNT = 3

/**
 * [TvCardRow]'s trailing (see-all) tile must be remembered as the row's entry cell exactly like any other
 * cell — the gap #2551 fixed for `TvMediaRow`, mirrored here. Without the fix, [rememberTvRowEntry] sizes
 * itself to `items.size` alone, the trailing tile never carries an [TvRowEntry.entryModifier] and never calls
 * [TvRowEntry.rememberFocused], so Back from a drill-down opened off the tile lands on the last card instead
 * of the tile — `TvDetailCastRow`'s "see all" into the full cast grid is the concrete surface.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvCardRowFocusMemoryTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * Walk real D-pad ↦ moves from the first cell to the trailing tile (a directly targeted `requestFocus()`
     * on the tile would itself be an *entry* into the row's [tvEntryFocusGroup] and get redirected to whatever
     * is currently remembered — the same reason production focus moves this way, cell by cell), leave the row
     * entirely (standing in for the tile's own drill-down and its Back), then re-enter through the row's
     * [FocusRequester] — the same path a returning screen re-enters by. Entry must resolve to the tile, not
     * the last card.
     */
    @Test
    fun `focusing the trailing tile is remembered as the entry cell`() {
        val entryRequester = FocusRequester()
        lateinit var focusManager: FocusManager
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Row {
                    RailStandIn()
                    CardRowWithTrailing(entryRequester)
                }
            }
        }

        // Enter the row for the first time — lands on its first cell.
        composeTestRule.runOnUiThread { runCatching { entryRequester.requestFocus() } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("cell0").assertIsFocused()

        // Walk right, cell by cell, onto the trailing tile.
        repeat(CELL_COUNT) {
            composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Right) }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TRAILING).assertIsFocused()

        // Leave the row — the tile's own drill-down would do this by navigating away entirely.
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()

        // Re-enter the row the way Back does: through its entry requester, not a fresh focus search.
        composeTestRule.runOnUiThread { runCatching { entryRequester.requestFocus() } }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TRAILING).assertIsFocused()
    }

    /** A row that has never had a cell focused still opens on its first card, trailing tile present or not. */
    @Test
    fun `a fresh row with a trailing tile still opens on its first cell`() {
        val entryRequester = FocusRequester()
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    CardRowWithTrailing(entryRequester)
                }
            }
        }

        composeTestRule.runOnUiThread { runCatching { entryRequester.requestFocus() } }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cell0").assertIsFocused()
    }

    /** The stand-in the entry requester moves focus away from — the rail every row-hosting screen sits beside. */
    @Composable
    private fun RailStandIn() {
        Box(Modifier.testTag(RAIL).size(CELL_WIDTH_DP.dp).focusable())
    }

    /** Three cards plus a trailing see-all tile, wired exactly as a real caller (e.g. `TvDetailCastRow`) would. */
    @Composable
    private fun CardRowWithTrailing(entryRequester: FocusRequester) {
        TvCardRow(
            items = (0 until CELL_COUNT).toList(),
            key = { it },
            cellWidth = CELL_WIDTH_DP.dp,
            entryFocusRequester = entryRequester,
            trailing = { _, onFocusChanged, cellModifier ->
                Box(
                    Modifier
                        .testTag(TRAILING)
                        .size(CELL_WIDTH_DP.dp)
                        .then(cellModifier)
                        .onFocusChanged { onFocusChanged(it.isFocused) }
                        .focusable(),
                )
            },
        ) { item, _, onFocusChanged, cellModifier ->
            Box(
                Modifier
                    .testTag("cell$item")
                    .then(cellModifier)
                    .onFocusChanged { onFocusChanged(it.isFocused) }
                    .focusable(),
            )
        }
    }
}
