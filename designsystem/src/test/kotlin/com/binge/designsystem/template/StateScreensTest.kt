package com.binge.designsystem.template

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class StateScreensTest {
    @get:Rule
    val rule = createComposeRule()

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    @Test
    fun `an error shows its kind's copy, and Try again runs the retry`() {
        var retries = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { ErrorScreen(kind = ErrorKind.Network, onRetry = { retries++ }) }
        }

        rule.onNodeWithText(string(R.string.error_kind_network_title)).assertExists()
        rule.onNodeWithText(string(R.string.error_kind_network_message)).assertExists()
        rule.onNodeWithText(string(R.string.error_kind_retry)).performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `an error without a retry offers nothing to press`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) { ErrorScreen(kind = ErrorKind.Forbidden, onRetry = null) }
        }

        rule.onAllNodes(hasClickAction()).assertCountEquals(0)
    }

    @Test
    fun `the caller's copy replaces the kind's`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                ErrorScreen(kind = ErrorKind.Server, onRetry = null, title = "Quota reached", message = "Try again tomorrow.")
            }
        }

        rule.onNodeWithText("Quota reached").assertExists()
        rule.onNodeWithText("Try again tomorrow.").assertExists()
        rule.onNodeWithText(string(R.string.error_kind_server_message)).assertDoesNotExist()
    }

    @Test
    fun `an empty screen's action is pressable`() {
        var pressed = false
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                EmptyScreen(message = "No requests yet.", action = ScreenAction("Make one", onClick = { pressed = true }))
            }
        }

        rule.onNodeWithText(string(R.string.empty_screen_title)).assertExists()
        rule.onNodeWithText("Make one").performClick()
        assertEquals(true, pressed)
    }
}
