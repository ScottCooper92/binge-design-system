package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ENTRY_TARGET = "entry-target"

/**
 * [TvFocusSink]'s own directional contract: the key toward the start edge is the one sanctioned way out, and
 * every other direction is cancelled. [BingeTvNavRailAdverseScheduleFocusTest] and [BingeTvNavRailFocusTest]
 * cover the sink's integration into the rail's content group — presence keyed on the pane's own focus, convergence onto a
 * late-composing real target, and the parked-rail case landing on the sink instead.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvFocusSinkTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private fun setSinkWithEntry(layoutDirection: LayoutDirection) {
        val startEntry = FocusRequester()
        composeTestRule.setContent {
            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                BingeTvTheme {
                    Box(
                        Modifier
                            .testTag(ENTRY_TARGET)
                            .size(80.dp)
                            .focusRequester(startEntry)
                            .focusable(),
                    )
                    TvFocusSink(startEntry = startEntry)
                }
            }
        }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).requestFocus()
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()
    }

    @Test
    fun `left moves focus to the supplied entry`() {
        setSinkWithEntry(LayoutDirection.Ltr)

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionLeft) }

        composeTestRule.onNodeWithTag(ENTRY_TARGET).assertIsFocused()
    }

    /** The rail sits at the start edge, which is the right under RTL, so the exit follows it (#429). */
    @Test
    fun `under RTL right moves focus to the supplied entry and left is inert`() {
        setSinkWithEntry(LayoutDirection.Rtl)

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionLeft) }
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()

        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.onNodeWithTag(ENTRY_TARGET).assertIsFocused()
    }

    @Test
    fun `every direction is inert with no startEntry supplied`() {
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

        // No startEntry: every direction, including left, is cancelled — the overlay host's shape, where
        // Back is the only way out.
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()
    }
}
