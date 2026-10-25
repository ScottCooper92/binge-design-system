package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
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
    fun `a control inside the sheet that consumes the start key keeps the sheet open`() {
        var dismissed = 0
        var value by mutableStateOf(5)
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheet(onDismissRequest = { dismissed++ }) { entry ->
                    TvRatingStepper(
                        value = value,
                        onValueChange = { value = it },
                        valueLabel = "$value / 10",
                        modifier = Modifier.focusRequester(entry),
                    )
                    TvSideSheetStepFocus(entry)
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionLeft) }
        composeTestRule.waitForIdle()

        assertEquals(4, value)
        assertEquals(0, dismissed)
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
    fun `the confirm step puts the safe answer first and focused, and the confirm below it`() {
        var confirmed = 0
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheet(onDismissRequest = {}) { entry ->
                    TvSideSheetConfirm(
                        title = "Cancel this request?",
                        message = "It is withdrawn.",
                        confirmLabel = "Cancel request",
                        onConfirm = { confirmed++ },
                        onCancel = {},
                        entryFocus = entry,
                        dismissLabel = "Keep",
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Keep").assertIsFocused().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Cancel request").assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        assertEquals(1, confirmed)
    }

    @Test
    fun `a disabled row is a read-out, not a control`() {
        composeTestRule.setContent {
            BingeTvTheme { TvSideSheetPanel { TvSideSheetRow(label = "Not available", onClick = {}, enabled = false) } }
        }

        composeTestRule.onNodeWithText("Not available").assertHasNoClickAction()
    }

    @Test
    fun `under a right-to-left layout the start key is right, and it dismisses the sheet`() {
        var dismissed = 0
        setRows(LayoutDirection.Rtl) { dismissed++ }

        composeTestRule.onNodeWithText(FIRST).assertIsFocused().performKeyInput { pressKey(Key.DirectionRight) }

        assertEquals(1, dismissed)
    }

    @Test
    fun `under a right-to-left layout left is the end key, and it leaves the sheet open`() {
        var dismissed = 0
        setRows(LayoutDirection.Rtl) { dismissed++ }

        composeTestRule.onNodeWithText(FIRST).assertIsFocused().performKeyInput { pressKey(Key.DirectionLeft) }

        assertEquals(0, dismissed)
    }

    @Test
    fun `up, down, the end key and OK leave the sheet open`() {
        var dismissed = 0
        setRows(LayoutDirection.Ltr) { dismissed++ }

        listOf(Key.DirectionDown, Key.DirectionUp, Key.DirectionRight, Key.DirectionCenter).forEach { key ->
            composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
            composeTestRule.waitForIdle()
        }

        assertEquals(0, dismissed)
    }

    @Test
    fun `focus cannot leave the sheet for the content behind it`() {
        setRows(LayoutDirection.Ltr, behind = true) {}
        composeTestRule.onNodeWithText(FIRST).assertIsFocused()

        listOf(FocusDirection.Left, FocusDirection.Up, FocusDirection.Right).forEach { direction ->
            composeTestRule.runOnIdle { focusManager.moveFocus(direction) }
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText(FIRST).assertIsFocused()
        }
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Down) }
        composeTestRule.runOnIdle { focusManager.moveFocus(FocusDirection.Down) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(SECOND).assertIsFocused()
        composeTestRule.onNodeWithTag(BEHIND).assertIsNotFocused()
    }

    private lateinit var focusManager: FocusManager

    /** A two-row sheet; with [behind], a focusable control under the scrim at the screen's start edge. */
    private fun setRows(
        direction: LayoutDirection,
        behind: Boolean = false,
        onDismissRequest: () -> Unit,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalLayoutDirection provides direction) {
                    focusManager = LocalFocusManager.current
                    Box {
                        if (behind) Box(Modifier.size(48.dp).testTag(BEHIND).focusable())
                        TvSideSheet(onDismissRequest = onDismissRequest) { entry ->
                            TvSideSheetRow(label = FIRST, onClick = {}, modifier = Modifier.focusRequester(entry))
                            TvSideSheetRow(label = SECOND, onClick = {})
                            TvSideSheetStepFocus(entry)
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}

private const val FIRST = "Approve"
private const val SECOND = "Decline"
private const val BEHIND = "behind"
