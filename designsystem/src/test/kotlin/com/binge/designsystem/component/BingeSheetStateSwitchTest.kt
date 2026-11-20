package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A sheet's change of state cross-fades: both the old and the new content are on screen part-way through the swap, and under
 * reduced motion only the new one ever is. A change inside one kind of content (a list being filtered) is not a swap at all. The clock
 * is paused so a frame part-way through can be read.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSheetStateSwitchTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var state by mutableStateOf(Kind.Loading to 0)

    private fun show(reduceMotion: Boolean) {
        state = Kind.Loading to 0
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false, reduceMotion = reduceMotion) {
                Box(Modifier.testTag("host")) {
                    BingeSheetStateSwitch(state, contentKey = { it.first }) { (kind, count) -> Text("$kind $count") }
                }
            }
        }
        rule.mainClock.advanceTimeByFrame()
    }

    /** Changes the state. A paused clock does not deliver the write by itself, so it is applied by hand. */
    private fun change(to: Pair<Kind, Int>) {
        state = to
        Snapshot.sendApplyNotifications()
    }

    private fun advanceFrames(count: Int) = repeat(count) { rule.mainClock.advanceTimeByFrame() }

    @Test
    fun `a change of kind has the old and the new content on screen part-way, then only the new`() {
        show(reduceMotion = false)
        rule.onNodeWithText("Loading 0").assertIsDisplayed()

        change(Kind.Ready to 0)
        advanceFrames(MIDWAY_FRAMES)
        rule.onAllNodesWithText("Loading 0").assertCountEquals(1)
        rule.onAllNodesWithText("Ready 0").assertCountEquals(1)

        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.onAllNodesWithText("Loading 0").assertCountEquals(0)
        rule.onAllNodesWithText("Ready 0").assertCountEquals(1)
    }

    @Test
    fun `reduced motion composes only the new content, from the first frame`() {
        show(reduceMotion = true)

        change(Kind.Ready to 0)
        rule.mainClock.advanceTimeByFrame()

        rule.onAllNodesWithText("Loading 0").assertCountEquals(0)
        rule.onAllNodesWithText("Ready 0").assertCountEquals(1)
    }

    @Test
    fun `a change inside one kind is not a swap`() {
        show(reduceMotion = false)
        change(Kind.Ready to 0)
        rule.mainClock.autoAdvance = true
        rule.waitForIdle()
        rule.mainClock.autoAdvance = false

        change(Kind.Ready to 1)
        advanceFrames(MIDWAY_FRAMES)

        rule.onAllNodesWithText("Ready 0").assertCountEquals(0)
        rule.onAllNodesWithText("Ready 1").assertCountEquals(1)
    }

    private enum class Kind { Loading, Ready }

    private companion object {
        const val MIDWAY_FRAMES = 4
    }
}
