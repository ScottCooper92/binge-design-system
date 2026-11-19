package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [TvStepFlow] draws its "Step N of M" read-out only when there is more than one step, as the phone flow does, in the
 * normal layout and with the chrome over the content. Both sides of the rule are checked.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvStepFlowSingleStepTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    private fun show(stepCount: Int, chromeOverContent: Boolean) {
        rule.setContent {
            BingeTvTheme {
                TvStepFlow(stepCount = stepCount, currentStep = 0, chromeOverContent = chromeOverContent) { Text("Step content") }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `one step draws no read-out`() {
        show(stepCount = 1, chromeOverContent = false)

        rule.onNodeWithText("Step content").assertIsDisplayed()
        rule.onNodeWithText("Step 1 of 1").assertDoesNotExist()
    }

    @Test
    fun `one step draws no read-out with the chrome over the content`() {
        show(stepCount = 1, chromeOverContent = true)

        rule.onNodeWithText("Step content").assertIsDisplayed()
        rule.onNodeWithText("Step 1 of 1").assertDoesNotExist()
    }

    @Test
    fun `two steps draw the read-out`() {
        show(stepCount = 2, chromeOverContent = false)

        rule.onNodeWithText("Step 1 of 2").assertIsDisplayed()
    }

    @Test
    fun `two steps draw the read-out with the chrome over the content`() {
        show(stepCount = 2, chromeOverContent = true)

        rule.onNodeWithText("Step 1 of 2").assertIsDisplayed()
    }
}
