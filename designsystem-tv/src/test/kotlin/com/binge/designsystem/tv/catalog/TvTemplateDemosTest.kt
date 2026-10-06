package com.binge.designsystem.tv.catalog

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The live demos do what their KDoc says, on the D-pad, so the catalog does not show a demo that stalls. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTemplateDemosTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the step flow demo lands on the next step's first choice after Continue`() {
        composeTestRule.setContent { BingeTvTheme { TvStepFlowDemo() } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("First choice").assertIsFocused()

        composeTestRule.onNodeWithText("First choice").performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.onNodeWithText("Continue").assertIsFocused().performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Step 2 of 3").assertExists()
        composeTestRule.onNodeWithText("First choice").assertIsFocused()
    }

    @Test
    fun `the message page demo moves focus to Retry once it has loaded`() {
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.setContent { BingeTvTheme { TvMessagePageDemo() } }
        composeTestRule.mainClock.advanceTimeBy(DEMO_WAIT_MILLIS)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Retry").assertIsFocused()
    }

    private companion object {
        const val DEMO_WAIT_MILLIS = 2_000L
    }
}
