package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.tv.material3.Text
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val Copy = DecisionCopy(title = "Title", subtitle = "Subtitle")

/** The TV templates take their content in the phone twins' shapes: one action type, one step read-out (#705). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTemplateTwinShapesTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `a decision's answers are the phone's actions, modifier and callback included`() {
        val pressed = mutableListOf<String>()
        rule.setContent {
            BingeTvTheme {
                TvDecisionPage(
                    copy = Copy,
                    points = emptyList(),
                    accept = ScreenAction("Accept", { pressed += "accept" }, modifier = Modifier.testTag("accept")),
                    decline = ScreenAction("Decline", { pressed += "decline" }, modifier = Modifier.testTag("decline")),
                    hosting = TvPageHosting.PreShell,
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("accept").assertIsFocused().performClick()
        rule.onNodeWithTag("decline").performClick()
        assertEquals(listOf("accept", "decline"), pressed)
    }

    @Suppress("DEPRECATION")
    @Test
    fun `the loose-label decision forwards to the shared shape`() {
        val pressed = mutableListOf<String>()
        rule.setContent {
            BingeTvTheme {
                TvDecisionPage(
                    copy = Copy,
                    points = emptyList(),
                    acceptLabel = "Accept",
                    declineLabel = "Decline",
                    onAccept = { pressed += "accept" },
                    onDecline = { pressed += "decline" },
                    hosting = TvPageHosting.PreShell,
                    declineModifier = Modifier.testTag("decline"),
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("Accept").assertIsFocused().performClick()
        rule.onNodeWithTag("decline").performClick()
        assertEquals(listOf("accept", "decline"), pressed)
    }

    @Test
    fun `a message page's action keeps the modifier it was given`() {
        var retries = 0
        rule.setContent {
            BingeTvTheme {
                TvMessagePage(
                    body = "Body",
                    hosting = TvPageHosting.PreShell,
                    primary = ScreenAction("Retry", { retries++ }, modifier = Modifier.testTag("retry")),
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag("retry").assertIsFocused().performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `a step flow reads out its position in the phone's words unless told otherwise`() {
        rule.setContent {
            BingeTvTheme { TvStepFlow(stepCount = 3, currentStep = 1) {} }
        }

        rule.onNodeWithText("Step 2 of 3").assertExists()
    }

    @Test
    fun `a loading step shows its label in place of the step`() {
        rule.setContent {
            BingeTvTheme {
                TvStepFlow(stepCount = 3, currentStep = 0, loadingLabel = "Loading your choices…") {
                    Text("Step content")
                }
            }
        }

        rule.onNodeWithText("Loading your choices…").assertExists()
        rule.onNodeWithText("Step content").assertDoesNotExist()
    }
}
