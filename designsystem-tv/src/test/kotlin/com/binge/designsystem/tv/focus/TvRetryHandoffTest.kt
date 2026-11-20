package com.binge.designsystem.tv.focus

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val TRY_AGAIN = "tryAgain"
private const val HOLD = "hold"

/**
 * [TvRetryHandoff.begin] moves focus to the hold in the press itself. The hold is composed before the press, so its requester
 * has a node to take the request while the plate still holds focus; asked only after it composed, the request would land after
 * the plate had gone and the rail had taken focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvRetryHandoffTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private lateinit var handoff: TvRetryHandoff
    private var failed by mutableStateOf(true)

    @Test
    fun `begin moves focus to the hold in the press, before any frame`() {
        show()
        composeTestRule.onNodeWithTag(TRY_AGAIN).requestFocus()
        composeTestRule.onNodeWithTag(TRY_AGAIN).assertIsFocused()
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.runOnUiThread { handoff.begin() }

        composeTestRule.onNodeWithTag(HOLD).assertIsFocused()
    }

    @Test
    fun `the hold keeps focus when the plate goes and the retry shows its skeleton`() {
        show()
        composeTestRule.onNodeWithTag(TRY_AGAIN).requestFocus()

        composeTestRule.runOnUiThread {
            handoff.begin()
            failed = false
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(HOLD).assertIsFocused()
    }

    @Test
    fun `the hand-off reports holding from the press until it ends`() {
        show()
        composeTestRule.runOnIdle { assertFalse(handoff.holding) }

        composeTestRule.runOnUiThread { handoff.begin() }
        composeTestRule.runOnIdle { assertTrue(handoff.holding) }

        composeTestRule.runOnUiThread { handoff.end() }
        composeTestRule.runOnIdle { assertFalse(handoff.holding) }
    }

    private fun show() {
        composeTestRule.setContent {
            BingeTvTheme {
                handoff = rememberTvRetryHandoff(showingFailure = failed, hasResults = false, takeFocus = {})
                Row {
                    Box(Modifier.testTag(RAIL).size(48.dp).focusable())
                    if (failed) Box(Modifier.testTag(TRY_AGAIN).size(48.dp).focusable())
                    TvRetryHold(handoff, Modifier.testTag(HOLD))
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
