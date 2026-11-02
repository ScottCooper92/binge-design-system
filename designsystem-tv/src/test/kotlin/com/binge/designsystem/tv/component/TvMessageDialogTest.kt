package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
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

/** The TV message dialog puts focus on its one button, and OK on it dismisses once. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvMessageDialogTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var dismissed = 0

    @Test
    fun `focus arrives on OK and OK dismisses once`() {
        rule.setContent {
            BingeTvTheme {
                TvMessageDialogContent(title = "Title", message = "Cause and next step.", confirmLabel = "OK", onDismiss = { dismissed++ })
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("OK").assertIsFocused()
        rule.onNodeWithText("OK").performKeyInput { pressKey(Key.DirectionCenter) }
        rule.waitForIdle()

        assertEquals(1, dismissed)
    }
}
