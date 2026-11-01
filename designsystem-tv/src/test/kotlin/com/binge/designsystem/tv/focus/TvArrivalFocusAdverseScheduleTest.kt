package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.testing.TV_LATE_TARGET_SIBLING_TAG
import com.binge.designsystem.tv.testing.TvLateTarget
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val TARGET = "target"

/** Frames to run after the target is placed, enough for the effect to have made its one request or ruled it out. */
private const val SETTLE_FRAMES = 10

/**
 * The overlay-arrival adverse-schedule cases: [TvArrivalFocusEffect] claims a target composed N
 * frames past the disposing sibling, once the target is placed, for N in {0, 1, 10, 60} — a frame
 * budget well above what a real layout takes.
 *
 * [TvArrivalFocusEffect] is hoisted above [TvLateTarget] in every fixture here, exactly as a real screen wires
 * it: the effect is there from the first frame, and only the requester's *target node* is what [TvLateTarget]
 * delays, so the claim has to wait for the node to be placed.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvArrivalFocusAdverseScheduleTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the arrival claims focus for a target composed 0 frames late`() = assertArrivalConverges(0)

    @Test
    fun `the arrival claims focus for a target composed 1 frame late`() = assertArrivalConverges(1)

    @Test
    fun `the arrival claims focus for a target composed 10 frames late`() = assertArrivalConverges(10)

    @Test
    fun `the arrival claims focus for a target composed 60 frames late`() = assertArrivalConverges(60)

    private fun assertArrivalConverges(delayFrames: Int) {
        var dispose by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                    val arrival = rememberTvArrivalFocus()
                    TvArrivalFocusEffect(arrival)
                    TvLateTarget(delayFrames = delayFrames, dispose = dispose) {
                        Box(
                            Modifier
                                .testTag(TARGET)
                                .size(80.dp)
                                .tvArrivalTarget(arrival)
                                .focusable(),
                        )
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(TV_LATE_TARGET_SIBLING_TAG).requestFocus()
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(TV_LATE_TARGET_SIBLING_TAG).assertIsFocused()

        dispose = true
        composeTestRule.waitForIdle()
        repeat(delayFrames + SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        composeTestRule.onNodeWithTag(RAIL).assertIsNotFocused()
    }
}
