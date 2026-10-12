package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.TV_FOCUS_SINK_TAG
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HEADER = "Account"
private const val FOOTER = "Settings"
private const val ITEM_COUNT = 4

/** Several of the sink's hand-off polls, so a poll that pulled focus back from the rail would have done so. */
private const val AFTER_POLLS_MILLIS = SINK_HANDOFF_POLL_MILLIS * 10

private fun destination(index: Int) = "Destination $index"

/**
 * The rail over a content pane with nothing focusable in it, as while a cold first screen shows its skeleton: focus
 * rests on the pane's sink, ← opens the rail on the selected item, and the D-pad reaches every item from there. The
 * sink's hand-off polls only while the sink itself holds focus, so it never pulls focus back out of the rail.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailEmptyPaneTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `over a pane with nothing focusable, left opens the rail on the selection and the d-pad walks every item`() {
        val committed = mutableListOf<Any>()
        composeTestRule.setContent {
            var selected by remember { mutableStateOf<Any>(destination(0)) }
            BingeTvTheme {
                BingeTvNavRail(
                    header = TvNavRailItem(key = HEADER, label = HEADER, icon = Icons.Filled.Home),
                    items = List(ITEM_COUNT) { TvNavRailItem(key = destination(it), label = destination(it), icon = Icons.Filled.Home) },
                    footer = TvNavRailItem(key = FOOTER, label = FOOTER, icon = Icons.Filled.Home),
                    selectedKey = selected,
                    onSelect = { key ->
                        committed += key
                        selected = key
                    },
                    expanded = true,
                ) { Box(Modifier.fillMaxSize()) }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()

        press(Key.DirectionLeft)
        composeTestRule.onNodeWithText(destination(0)).assertIsFocused()
        press(Key.DirectionUp)
        composeTestRule.onNodeWithText(HEADER).assertIsFocused()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(destination(0)).assertIsFocused()
        for (index in 1 until ITEM_COUNT) {
            press(Key.DirectionDown)
            composeTestRule.onNodeWithText(destination(index)).assertIsFocused()
        }
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(FOOTER).assertIsFocused()

        composeTestRule.mainClock.advanceTimeBy(AFTER_POLLS_MILLIS)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(FOOTER).assertIsFocused()
        assertEquals(listOf(HEADER, destination(0)) + List(ITEM_COUNT - 1) { destination(it + 1) } + FOOTER, committed)
    }

    /**
     * What a consumer's rail test can rely on now that focus rests on the sink rather than in the rail: an explicit
     * request into the rail from outside it is an entry, so it lands on the selected item and commits nothing, and a
     * request made once inside moves focus there and commits that item. A test that wants focus on a given item plants
     * it in two steps, the selection first.
     */
    @Test
    fun `from the sink, a request into the rail lands on the selection, and one inside it moves`() {
        val committed = mutableListOf<Any>()
        composeTestRule.setContent {
            BingeTvTheme {
                BingeTvNavRail(
                    header = null,
                    items = List(ITEM_COUNT) { TvNavRailItem(key = destination(it), label = destination(it), icon = Icons.Filled.Home) },
                    footer = null,
                    selectedKey = destination(0),
                    onSelect = { committed += it },
                    expanded = true,
                ) { Box(Modifier.fillMaxSize()) }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(destination(1)).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(destination(0)).assertIsFocused()
        assertTrue("an entry onto the selection commits nothing, but committed $committed", committed.isEmpty())

        composeTestRule.onNodeWithText(destination(1)).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(destination(1)).assertIsFocused()
        assertEquals(listOf<Any>(destination(1)), committed)
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
