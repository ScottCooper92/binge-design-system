package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the sheet's first row takes focus, and the start key dismisses it`() {
        var dismissed = 0
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheet(onDismissRequest = { dismissed++ }) { entry ->
                    TvSideSheetRow(label = "Approve", onClick = {}, modifier = Modifier.focusRequester(entry))
                    TvSideSheetRow(label = "Decline", onClick = {})
                    TvSideSheetStepFocus(entry)
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Approve").assertIsFocused().performKeyInput { pressKey(Key.DirectionLeft) }

        assertEquals(1, dismissed)
    }

    @Test
    fun `the confirm step lands on Cancel, and Cancel brings the rows back focused`() {
        composeTestRule.setContent {
            BingeTvTheme {
                var confirming by remember { mutableStateOf(true) }
                TvSideSheet(onDismissRequest = {}) { entry ->
                    if (confirming) {
                        TvSideSheetConfirm(
                            title = "Delete this request?",
                            message = "It is removed for everyone.",
                            confirmLabel = "Delete",
                            onConfirm = {},
                            onCancel = { confirming = false },
                            entryFocus = entry,
                        )
                    } else {
                        TvSideSheetRow(label = "Delete request", onClick = {}, modifier = Modifier.focusRequester(entry))
                        TvSideSheetStepFocus(entry)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Cancel").assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Delete request").assertIsFocused()
    }

    @Test
    fun `a disabled row is a read-out, not a control`() {
        composeTestRule.setContent {
            BingeTvTheme { TvSideSheetPanel { TvSideSheetRow(label = "Not available", onClick = {}, enabled = false) } }
        }

        composeTestRule.onNodeWithText("Not available").assertHasNoClickAction()
    }
}
