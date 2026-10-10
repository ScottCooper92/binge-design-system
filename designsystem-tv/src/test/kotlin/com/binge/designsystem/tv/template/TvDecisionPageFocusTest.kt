package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ACCEPT = "Accept"

/** Enough points that the card runs well past a 540dp panel. */
private const val POINT_COUNT = 10

/**
 * A decision whose points are taller than the panel reads end to end with a D-pad: ↑ from the answers reaches
 * the first point, ↓ walks every point in order and lands back on the answers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvDecisionPageFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `every point of a page taller than the panel is reached in order`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvDecisionPage(
                    copy = DecisionCopy(title = "Title", subtitle = "Subtitle"),
                    points = (1..POINT_COUNT).map { DecisionPoint(Icons.Filled.BarChart, "Point $it", "Detail of point $it.") },
                    acceptLabel = ACCEPT,
                    declineLabel = "Decline",
                    onAccept = {},
                    onDecline = {},
                    hosting = TvPageHosting.PreShell,
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(ACCEPT).assertIsFocused()

        repeat(POINT_COUNT + 1) { press(Key.DirectionUp) }
        composeTestRule.onNodeWithText("Point 1").assertIsFocused()

        (2..POINT_COUNT).forEach {
            press(Key.DirectionDown)
            composeTestRule.onNodeWithText("Point $it").assertIsFocused()
        }
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(ACCEPT).assertIsFocused()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
