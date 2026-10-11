package com.binge.designsystem.tv.catalog

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
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
        composeTestRule.onNodeWithText("Server unreachable").assertDoesNotExist()
        composeTestRule.mainClock.advanceTimeBy(DEMO_WAIT_MILLIS)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Server unreachable").assertExists()
        composeTestRule.onNodeWithText("Retry").assertIsFocused()
    }

    @Test
    fun `the save-as-made demo saves a change with no button, and offers Retry when the save fails`() {
        composeTestRule.setContent { BingeTvTheme { TvTwoPanePageSaveAsMadeDemo() } }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Save").assertDoesNotExist()
        composeTestRule.mainClock.autoAdvance = false

        press(row("Notification 1"))
        composeTestRule.onNodeWithText("Saving…").assertExists()
        composeTestRule.mainClock.advanceTimeBy(DEMO_WAIT_MILLIS)
        composeTestRule.onNodeWithText("Saved").assertExists()

        press(row("Make saves fail"))
        press(row("Notification 2"))
        composeTestRule.mainClock.advanceTimeBy(DEMO_WAIT_MILLIS)
        composeTestRule.onNodeWithText("Couldn't save your change").assertExists()
        row("Notification 2").assertIsOn()
        row("Retry").assertExists()
    }

    private fun row(label: String) = composeTestRule.onNode(hasText(label) and isFocusable())

    private fun press(node: SemanticsNodeInteraction) {
        node.requestFocus()
        composeTestRule.mainClock.advanceTimeByFrame()
        node.performKeyInput { pressKey(Key.DirectionCenter) }
        composeTestRule.mainClock.advanceTimeBy(SETTLE_MILLIS)
    }

    private companion object {
        const val DEMO_WAIT_MILLIS = 2_000L

        /** Long enough for a press to recompose and start its save, short of the demo's save delay. */
        const val SETTLE_MILLIS = 100L
    }
}
