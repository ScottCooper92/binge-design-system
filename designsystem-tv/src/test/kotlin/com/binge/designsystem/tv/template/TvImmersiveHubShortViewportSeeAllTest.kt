package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.TvImmersiveHubFocusedSample
import com.binge.designsystem.tv.catalog.TvImmersiveHubSample
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * On a viewport shorter than the pinned row's cards (as on the Streamer under the catalog app, where the cards run past the bottom
 * edge), focusing See-all drew a bring-into-view scroll from the column that nothing undid: a title's focus changes the anchor
 * keeper's focused-item key and so re-pins the row, See-all's did not. The row must stay where it is.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h400dp-television-xhdpi")
class TvImmersiveHubShortViewportSeeAllTest {
    @get:Rule(order = 0)
    val leanback = LeanbackRule()

    @get:Rule(order = 1)
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    @Test
    fun `the focused hub sample keeps its row where it is when focus reaches See-all`() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                TvImmersiveHubFocusedSample()
            }
        }
        composeTestRule.waitForIdle()
        // Real focus enters the seeded row: the first move lands on a card, as the user's does.
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Right) }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        val onTitle = composeTestRule.onNodeWithText("Approved (24)").getUnclippedBoundsInRoot().top

        repeat(40) {
            composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Right) }
            composeTestRule.waitForIdle()
        }
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        val onSeeAll = composeTestRule.onNodeWithText("Approved (24)").getUnclippedBoundsInRoot().top

        composeTestRule.onNodeWithContentDescription("See all").assertIsFocused()
        assertEquals("The row moved between a title and See-all", onTitle.value, onSeeAll.value, 0.5f)
    }

    @Test
    fun `the unseeded hub sample keeps its row where it is when focus travels to See-all`() {
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                TvImmersiveHubSample()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Down) }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Down) }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        val onTitle = composeTestRule.onNodeWithText("Approved (24)").getUnclippedBoundsInRoot().top

        repeat(40) {
            composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Right) }
            composeTestRule.waitForIdle()
        }
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        val onSeeAll = composeTestRule.onNodeWithText("Approved (24)").getUnclippedBoundsInRoot().top

        composeTestRule.onNodeWithContentDescription("See all").assertIsFocused()
        assertEquals("The row moved between a title and See-all", onTitle.value, onSeeAll.value, 0.5f)
    }
}
