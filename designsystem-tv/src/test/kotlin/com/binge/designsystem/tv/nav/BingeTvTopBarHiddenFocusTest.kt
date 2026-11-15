package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Until `visible`, the bar takes no focus: not through its tabs, and not through whatever a caller puts in its trailing slot. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvTopBarHiddenFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private lateinit var focusManager: FocusManager

    private var barVisible by mutableStateOf(true)

    private fun setBar(visible: Boolean, grouped: Boolean = false) {
        barVisible = visible
        composeTestRule.setContent {
            BingeTvTheme {
                focusManager = LocalFocusManager.current
                Column {
                    BingeTvTopBar(
                        header = null,
                        items = emptyList(),
                        footer = null,
                        selectedKey = "none",
                        onSelect = {},
                        itemFocusRequesters = emptyMap(),
                        down = remember { FocusRequester() },
                        onFocusedChange = {},
                        visible = barVisible,
                        trailing = {
                            val gear = @Composable {
                                TvIconButton(
                                    icon = Icons.Filled.Settings,
                                    label = "Settings",
                                    onClick = {},
                                    expandOnFocus = false,
                                )
                            }
                            // A caller's own focus group inside the slot: the gate must reach what is inside it as well.
                            if (grouped) Box(Modifier.focusGroup()) { gear() } else gear()
                        },
                    )
                    Box(Modifier.size(48.dp).testTag("content").focusable())
                }
            }
        }
        composeTestRule.onNodeWithTag("content").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("content").assertIsFocused()
    }

    private fun moveUp() {
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Up) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `Up from the page reaches the trailing item while the bar is visible`() {
        setBar(visible = true)
        moveUp()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsFocused()
    }

    @Test
    fun `Up from the page does not reach the trailing item while the bar is hidden`() {
        setBar(visible = false)
        moveUp()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsNotFocused()
    }

    @Test
    fun `Up from the page does not reach an item the caller put in its own focus group while the bar is hidden`() {
        setBar(visible = false, grouped = true)
        moveUp()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsNotFocused()
    }

    @Test
    fun `a bar that hides after focus left a nested trailing item does not restore focus to it`() {
        setBar(visible = true, grouped = true)
        composeTestRule.onNodeWithContentDescription("Settings").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Settings").assertIsFocused()
        // Focus leaves down to the page, so the bar's restorer has the nested item saved; then the bar hides.
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Down) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("content").assertIsFocused()
        barVisible = false
        composeTestRule.waitForIdle()

        moveUp()

        composeTestRule.onNodeWithContentDescription("Settings").assertIsNotFocused()
    }
}
