package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SHELL = "shell"
private const val TARGET = "target"

/**
 * **A host that provides the user-move guard notes every direction press, and consumes none of them.** Without a host the guard
 * is dormant (`LocalTvUserMoves` is never fed); [TvUserMovesHost] is the one wrapper a shell puts at its root to switch it on.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvUserMovesHostTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var seen = 0
    private var moves: TvUserMoves? = null

    private fun hosted(child: @androidx.compose.runtime.Composable (TvUserMoves) -> Unit) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvUserMovesHost {
                    val provided = LocalTvUserMoves.current
                    moves = provided
                    child(provided)
                }
            }
        }
    }

    private fun press(key: Key) = composeTestRule.onNodeWithTag(SHELL).performKeyInput { pressKey(key) }

    @Test
    fun `each direction press is noted once, whichever direction`() {
        hosted { Box(Modifier.testTag(SHELL).size(80.dp).focusable()) }
        composeTestRule.onNodeWithTag(SHELL).requestFocus()

        listOf(Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight).forEachIndexed { index, key ->
            press(key)
            assertEquals("after $key", index + 1, moves?.count)
        }
    }

    @Test
    fun `keys that are not directions note nothing`() {
        hosted { Box(Modifier.testTag(SHELL).size(80.dp).focusable()) }
        composeTestRule.onNodeWithTag(SHELL).requestFocus()

        press(Key.DirectionCenter)
        press(Key.Enter)
        press(Key.Back)
        press(Key.A)

        assertEquals(0, moves?.count)
    }

    @Test
    fun `the host does not consume the press, so the focused node still sees it`() {
        hosted {
            Box(
                Modifier
                    .testTag(SHELL)
                    .size(80.dp)
                    .onKeyEvent {
                        seen++
                        false
                    }.focusable(),
            )
        }
        composeTestRule.onNodeWithTag(SHELL).requestFocus()

        press(Key.DirectionUp)

        assertTrue("the child saw the press", seen > 0)
        assertEquals(1, moves?.count)
    }

    @Test
    fun `under a host, an arrival whose target attaches late does not pull focus back after a user move`() {
        var enabled by mutableStateOf(false)
        hosted {
            Row {
                Box(Modifier.testTag(SHELL).size(80.dp).focusable())
                val arrival = rememberTvArrivalFocus()
                TvArrivalFocusEffect(arrival, enabled = enabled)
                Box(
                    Modifier
                        .testTag(TARGET)
                        .size(80.dp)
                        .tvArrivalTarget(arrival)
                        .focusable(),
                )
            }
        }
        composeTestRule.onNodeWithTag(SHELL).requestFocus()
        composeTestRule.waitForIdle()

        // Up has no neighbour, so focus stays on the shell: the user pressed a direction and chose to stay.
        press(Key.DirectionUp)
        enabled = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(SHELL).assertIsFocused()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
    }

    @Test
    fun `under a host, an arrival with no user move still claims focus`() {
        var enabled by mutableStateOf(false)
        hosted {
            Row {
                Box(Modifier.testTag(SHELL).size(80.dp).focusable())
                val arrival = rememberTvArrivalFocus()
                TvArrivalFocusEffect(arrival, enabled = enabled)
                Box(
                    Modifier
                        .testTag(TARGET)
                        .size(80.dp)
                        .tvArrivalTarget(arrival)
                        .focusable(),
                )
            }
        }
        composeTestRule.onNodeWithTag(SHELL).requestFocus()
        composeTestRule.waitForIdle()

        enabled = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
    }
}
