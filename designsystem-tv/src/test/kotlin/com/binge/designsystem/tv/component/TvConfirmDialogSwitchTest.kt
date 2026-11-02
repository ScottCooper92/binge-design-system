package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.catalog.TvConfirmDialogSwitchSample
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The switch dialog on a remote: focus starts on Cancel, the switch is one Up away, and OK on it toggles without confirming. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvConfirmDialogSwitchTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private fun press(key: Key) = rule.onRoot().performKeyInput { pressKey(key) }

    @Test
    fun `focus starts on Cancel, Up reaches the switch, OK toggles the confirm label, Right reaches the confirm`() {
        rule.setContent { BingeTvTheme { TvConfirmDialogSwitchSample() } }
        rule.waitForIdle()
        rule.onNodeWithText("Cancel").assertIsFocused()

        press(Key.DirectionUp)
        rule.waitForIdle()
        rule.onNodeWithText("Also delete the files").assertIsFocused()
        rule.onNodeWithText("Remove request").assertExists()

        press(Key.DirectionCenter)
        rule.waitForIdle()
        rule.onNodeWithText("Remove and delete files").assertExists()
        rule.onNodeWithText("Also delete the files").assertIsFocused()

        press(Key.DirectionDown)
        rule.waitForIdle()
        rule.onNodeWithText("Cancel").assertIsFocused()

        press(Key.DirectionRight)
        rule.waitForIdle()
        rule.onNodeWithText("Remove and delete files").assertIsFocused()
    }
}
