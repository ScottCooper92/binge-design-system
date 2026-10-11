package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LINE = "line"
private const val RETRY = "Retry"
private const val FIELD = "Field"

/**
 * The save status line draws nothing until there is a save to report, and on a failure its Retry calls back on OK
 * without taking focus from the control the user is on.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSaveStatusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `idle draws nothing and takes no space`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Box(modifier = Modifier.testTag(LINE)) { TvSaveStatus(status = SaveStatus.Idle) }
            }
        }

        composeTestRule.onNodeWithTag(LINE).assertWidthIsEqualTo(0.dp).assertHeightIsEqualTo(0.dp)
        composeTestRule.onNodeWithText("Saving…").assertDoesNotExist()
        composeTestRule.onNodeWithText("Saved").assertDoesNotExist()
        composeTestRule.onNodeWithText(RETRY).assertDoesNotExist()
    }

    @Test
    fun `saving and saved show no Retry`() {
        var status by mutableStateOf<SaveStatus>(SaveStatus.Saving)
        composeTestRule.setContent { BingeTvTheme { TvSaveStatus(status = status) } }

        composeTestRule.onNodeWithText("Saving…").assertExists()
        composeTestRule.onNodeWithText(RETRY).assertDoesNotExist()

        status = SaveStatus.Saved
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Saved").assertExists()
        composeTestRule.onNodeWithText(RETRY).assertDoesNotExist()
    }

    @Test
    fun `OK on Retry after a failure calls back`() {
        var retries = 0
        composeTestRule.setContent { BingeTvTheme { TvSaveStatus(status = SaveStatus.Failed(onRetry = { retries++ })) } }
        retry().requestFocus()
        composeTestRule.waitForIdle()
        retry().assertIsFocused()

        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        assertEquals(1, retries)
    }

    @Test
    fun `a failure arriving leaves focus on the control the user is on`() {
        var status by mutableStateOf<SaveStatus>(SaveStatus.Saving)
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    TvButton(label = FIELD, onClick = {})
                    TvSaveStatus(status = status)
                }
            }
        }
        field().requestFocus()
        composeTestRule.waitForIdle()

        status = SaveStatus.Failed(onRetry = {})
        composeTestRule.waitForIdle()

        retry().assertExists()
        field().assertIsFocused()
    }

    @Test
    fun `Retry moving to Saving does not send focus to the first item`() = retryLeavesTheLine(SaveStatus.Saving)

    @Test
    fun `Retry moving to Saved does not send focus to the first item`() = retryLeavesTheLine(SaveStatus.Saved)

    private fun retryLeavesTheLine(next: SaveStatus) {
        var status by mutableStateOf<SaveStatus>(SaveStatus.Idle)
        status = SaveStatus.Failed(onRetry = { status = next })
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    TvButton(label = "First", onClick = {})
                    TvButton(label = FIELD, onClick = {})
                    TvSaveStatus(status = status)
                }
            }
        }
        retry().requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        retry().assertDoesNotExist()
        field().assertIsFocused()
    }

    private fun retry() = composeTestRule.onNode(hasText(RETRY) and isFocusable())

    private fun field() = composeTestRule.onNode(hasText(FIELD) and isFocusable())
}
