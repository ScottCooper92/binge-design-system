package com.binge.designsystem.component

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.binge.designsystem.testing.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingePinFieldFocusTest {
    @get:Rule
    val rule = createComposeRule()

    private fun show(autoFocus: Boolean) =
        rule.setContent {
            TestTheme {
                BingePinField(value = "", onValueChange = {}, label = "PIN", autoFocus = autoFocus)
            }
        }

    @Test
    fun `the field takes focus on first composition when it asks to`() {
        show(autoFocus = true)
        rule.waitForIdle()

        rule.onNodeWithContentDescription("PIN").assertIsFocused()
    }

    @Test
    fun `the field leaves focus alone when it does not`() {
        show(autoFocus = false)
        rule.waitForIdle()

        rule.onNodeWithContentDescription("PIN").assertIsNotFocused()
    }
}
