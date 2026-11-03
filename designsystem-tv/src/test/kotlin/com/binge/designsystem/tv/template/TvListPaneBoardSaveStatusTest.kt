package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_PLAYBACK = "playback"
private const val ROW_PLAYBACK = "Playback"
private const val FIRST_OPTION = "Direct play"
private const val LAST_OPTION = "Transcode"
private const val FAILED = "Couldn't save your change"
private const val RETRY = "Retry"
private const val REASON = "Changing this needs the administrator"

/**
 * A row's save status in [TvListPaneBoard]'s pane: it arrives after the row's options without taking focus from the option the
 * user is on, Retry is on the D-pad below the last option, and pressing it asks again and hands focus back up. The line's own
 * wording and live region are [com.binge.designsystem.tv.component.TvSaveStatus]'s.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvListPaneBoardSaveStatusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private var status by mutableStateOf<SaveStatus>(SaveStatus.Idle)
    private var retries = 0

    @Test
    fun `an idle row draws no status`() {
        setBoard()

        composeTestRule.onAllNodes(hasText(FAILED)).assertCountEquals(0)
        composeTestRule.onAllNodes(hasText(RETRY)).assertCountEquals(0)
    }

    @Test
    fun `a failure arriving leaves focus on the option the user is on`() {
        setBoard()
        focusLastOption()

        status = SaveStatus.Failed
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(FAILED).assertExists()
        option(LAST_OPTION).assertIsFocused()
    }

    @Test
    fun `down from the last option reaches Retry, and pressing it asks again and hands focus back up`() {
        setBoard()
        focusLastOption()
        status = SaveStatus.Failed
        composeTestRule.waitForIdle()

        press(Key.DirectionDown)
        composeTestRule.onNode(hasText(RETRY) and isFocusable()).assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(1, retries)
        option(LAST_OPTION).assertIsFocused()
    }

    @Test
    fun `a refusal gives its reason and no Retry`() {
        setBoard()
        status = SaveStatus.Refused(REASON)
        composeTestRule.waitForIdle()
        row().requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(REASON).assertExists()
        composeTestRule.onAllNodes(hasText(RETRY)).assertCountEquals(0)
    }

    @Test
    fun `saving and saved each show their line`() {
        setBoard()

        status = SaveStatus.Saving
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Saving…").assertExists()

        status = SaveStatus.Saved
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Saved").assertExists()
    }

    private fun setBoard() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Settings",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Playback",
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY_PLAYBACK,
                                            label = ROW_PLAYBACK,
                                            body = "How a title plays.",
                                            options = listOf(TvPaneOption(label = FIRST_OPTION), TvPaneOption(label = LAST_OPTION)),
                                            saveStatus = status,
                                            onRetry = {
                                                retries++
                                                status = SaveStatus.Saving
                                            },
                                        ),
                                    ),
                            ),
                        ),
                    focusedKey = KEY_PLAYBACK,
                    onFocusRow = {},
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun focusLastOption() {
        row().requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)
        press(Key.DirectionDown)
        option(LAST_OPTION).assertIsFocused()
    }

    private fun row() = composeTestRule.onNode(hasText(ROW_PLAYBACK) and isFocusable())

    private fun option(label: String) = composeTestRule.onNode(hasText(label) and isFocusable())

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
