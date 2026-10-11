package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_SERVER = "server"
private const val KEY_DISCONNECT = "disconnect"
private const val ROW_SERVER = "Server"
private const val ROW_DISCONNECT = "Disconnect"
private const val OPTION_DISCONNECT = "Disconnect now"

/**
 * [TvListPaneBoard] under a real D-pad: entry lands on the described row whatever was asked for, ↓ walks the list and
 * moves the description with it, → reaches the pane's option, ← returns to the row the option belongs to, and OK on
 * an option commits it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvListPaneBoardFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val described = mutableListOf<String>()
    private var commits = 0

    @Test
    fun `entry is routed to the described row`() {
        setBoard(describedKey = KEY_DISCONNECT)

        row(ROW_SERVER).requestFocus()
        composeTestRule.waitForIdle()

        row(ROW_DISCONNECT).assertIsFocused()
    }

    @Test
    fun `down walks the list and describes the row it reaches`() {
        setBoard(describedKey = KEY_SERVER)
        row(ROW_SERVER).requestFocus()
        composeTestRule.waitForIdle()
        row(ROW_SERVER).assertIsFocused()

        press(Key.DirectionDown)

        row(ROW_DISCONNECT).assertIsFocused()
        assertEquals(KEY_DISCONNECT, described.last())
    }

    @Test
    fun `right reaches the option and left returns to its row`() {
        setBoard(describedKey = KEY_DISCONNECT)
        row(ROW_DISCONNECT).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionRight)
        option().assertIsFocused()
        press(Key.DirectionLeft)

        row(ROW_DISCONNECT).assertIsFocused()
        assertEquals(listOf(KEY_DISCONNECT), described.distinct())
    }

    @Test
    fun `OK on an option commits it`() {
        setBoard(describedKey = KEY_DISCONNECT)
        row(ROW_DISCONNECT).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)
        option().assertIsFocused()

        press(Key.DirectionCenter)

        assertEquals(1, commits)
    }

    private fun setBoard(describedKey: String) {
        composeTestRule.setContent {
            var focusedKey by remember { mutableStateOf<String?>(describedKey) }
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Settings",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Connection",
                                rows =
                                    listOf(
                                        TvPaneRow(key = KEY_SERVER, label = ROW_SERVER, body = "http://media.example.com"),
                                        TvPaneRow(
                                            key = KEY_DISCONNECT,
                                            label = ROW_DISCONNECT,
                                            body = "Removes the saved sign-in.",
                                            options = listOf(TvPaneOption(label = OPTION_DISCONNECT, onSelect = { commits++ })),
                                        ),
                                    ),
                            ),
                        ),
                    focusedKey = focusedKey,
                    onFocusRow = {
                        described += it
                        focusedKey = it
                    },
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun row(label: String) = composeTestRule.onNode(hasText(label) and isFocusable())

    private fun option() = composeTestRule.onNode(hasText(OPTION_DISCONNECT) and isFocusable())

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
