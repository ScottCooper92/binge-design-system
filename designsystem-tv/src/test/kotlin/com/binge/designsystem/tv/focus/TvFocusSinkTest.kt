package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LEFT_TARGET = "left-target"

/**
 * [TvFocusSink]'s own directional contract (#2518 decision 5): ← is the one sanctioned way out, everywhere
 * else is cancelled. [BingeTvNavRailAdverseScheduleFocusTest] and [BingeTvNavRailFocusTest] cover the sink's
 * integration into the rail's content group — presence keyed on the pane's own focus, convergence onto a
 * late-composing real target, and the parked-rail case landing on the sink instead.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvFocusSinkTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `left moves focus to the supplied entry`() {
        val leftEntry = FocusRequester()
        composeTestRule.setContent {
            BingeTvTheme {
                Box(
                    Modifier
                        .testTag(LEFT_TARGET)
                        .size(80.dp)
                        .focusRequester(leftEntry)
                        .focusable(),
                )
                TvFocusSink(leftEntry = leftEntry)
            }
        }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).requestFocus()
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionLeft) }

        composeTestRule.onNodeWithTag(LEFT_TARGET).assertIsFocused()
    }

    @Test
    fun `up down and right are inert with no leftEntry supplied`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvFocusSink()
            }
        }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).requestFocus()

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionUp) }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionLeft) }

        // No leftEntry: every direction, including left, is cancelled — the overlay host's shape, where
        // Back is the only way out.
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()
    }
}
