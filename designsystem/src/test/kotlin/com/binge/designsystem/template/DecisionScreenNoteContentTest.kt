package com.binge.designsystem.template

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.testing.TestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val NOTE = "You can change this at any time in Settings."
private const val SLOT_FIRST = "Nothing is sent until you say yes."
private const val SLOT_SECOND = "Nothing is kept for longer than ninety days."

/**
 * [DecisionScreen] passes its note slot to both of its layouts (#675): the portrait one through [DecisionBody], the
 * landscape one through the start column's [DecisionHeading]. Either way the slot replaces `copy.note`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DecisionScreenNoteContentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun `portrait draws the slot in place of the note`() {
        setScreen()
        assertSlotReplacesNote()
    }

    @Test
    @Config(qualifiers = "w891dp-h411dp")
    fun `landscape draws the slot in place of the note`() {
        setScreen()
        assertSlotReplacesNote()
    }

    private fun setScreen() {
        rule.setContent {
            TestTheme {
                DecisionScreen(
                    copy = DecisionCopy(title = "Help make the app better", subtitle = "Share anonymous usage data.", note = NOTE),
                    points = listOf(DecisionPoint(Icons.Filled.Check, "What is shared", "Which screens you open.")),
                    accept = ScreenAction("Share usage data", onClick = {}),
                    decline = ScreenAction("Not now", onClick = {}),
                    noteContent = {
                        Text(SLOT_FIRST)
                        Text(SLOT_SECOND)
                    },
                )
            }
        }
        rule.waitForIdle()
    }

    private fun assertSlotReplacesNote() {
        rule.onNodeWithText(SLOT_FIRST).assertExists()
        rule.onNodeWithText(SLOT_SECOND).assertExists()
        rule.onNodeWithText(NOTE).assertDoesNotExist()
    }
}
