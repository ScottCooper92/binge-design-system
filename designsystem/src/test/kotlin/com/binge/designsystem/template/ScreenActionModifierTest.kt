package com.binge.designsystem.template

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ScreenActionModifierTest {
    @get:Rule
    val rule = createComposeRule()

    private val copy = DecisionCopy(kicker = null, title = "Share?", subtitle = "Anonymous counts.")

    @Test
    fun `a decision screen's answers carry the modifier their action brings`() {
        var accepted = 0
        var declined = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                DecisionScreen(
                    copy = copy,
                    points = emptyList<DecisionPoint>(),
                    accept = ScreenAction("Share", onClick = { accepted++ }, modifier = Modifier.testTag("accept")),
                    decline = ScreenAction("Not now", onClick = { declined++ }, modifier = Modifier.testTag("decline")),
                )
            }
        }

        rule.onNodeWithTag("decline").performClick()
        rule.onNodeWithTag("accept").performClick()

        assertEquals(1, declined)
        assertEquals(1, accepted)
    }

    @Test
    fun `a message screen's actions carry the modifier their action brings`() {
        var retried = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                MessageScreen(
                    body = "Something went wrong.",
                    primary = ScreenAction("Try again", onClick = { retried++ }, modifier = Modifier.testTag("retry")),
                )
            }
        }

        rule.onNodeWithTag("retry").performClick()

        assertEquals(1, retried)
    }

    @Test
    fun `the decision body draws the copy without the screen's answers`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                DecisionBody(copy = copy, points = emptyList())
            }
        }

        rule.onNodeWithText("Share?").assertIsDisplayed()
        assertEquals(0, rule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size)
    }
}
