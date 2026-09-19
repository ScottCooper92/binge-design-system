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
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.testing.TV_LATE_TARGET_SIBLING_TAG
import com.binge.designsystem.tv.testing.TvLateTarget
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

private const val RAIL = "rail"
private const val TARGET = "target"

/** Frames to settle the offer loop after its target should already hold focus, or should have given up. */
private const val SETTLE_FRAMES = 10

/** Past [FOCUS_OFFER_TIMEOUT_MS] (3s), well short of exhausting [FOCUS_OFFER_FRAMES] (600) — the deadline's own case. */
private val PAST_ARRIVAL_TIMEOUT = Duration.ofMillis(3_100)

/**
 * The overlay-arrival adverse-schedule cases #2521 asks for: [offerTvArrivalFocus] (via [TvArrivalFocusEffect])
 * converges on a target composed N frames past the disposing sibling, for N in {0, 1, 10, 60} — same ceiling
 * rationale as `BingeTvNavRailAdverseScheduleFocusTest`.
 *
 * [TvArrivalFocusEffect] is hoisted above [TvLateTarget] in every fixture here, exactly as a real screen wires
 * it: the offer starts the moment the screen composes, and only the requester's *target node* — not the offer
 * itself — is what [TvLateTarget] delays. Nesting the effect inside the delayed content would start the retry
 * loop late too, so it would always find its own target already attached and never exercise a lateness the
 * loop has to survive.
 *
 * The wall-clock case below is decision 2's second named trap: [offerTvArrivalFocus] reads
 * `SystemClock.uptimeMillis()`, which Robolectric does not advance on its own — a test that only steps
 * `mainClock` would never see the deadline fire, and the loop would look bounded only because its frame budget
 * also happens to be finite. Advancing [ShadowSystemClock] is what makes the deadline itself the thing under
 * test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvArrivalFocusAdverseScheduleTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the arrival offer converges on a target composed 0 frames late`() = assertArrivalConverges(0)

    @Test
    fun `the arrival offer converges on a target composed 1 frame late`() = assertArrivalConverges(1)

    @Test
    fun `the arrival offer converges on a target composed 10 frames late`() = assertArrivalConverges(10)

    @Test
    fun `the arrival offer converges on a target composed 60 frames late`() = assertArrivalConverges(60)

    /**
     * The wall-clock deadline's own case: [ShadowSystemClock] moves past [FOCUS_OFFER_TIMEOUT_MS] while the
     * target is still 200 frames from composing — far short of [FOCUS_OFFER_FRAMES] exhausting on its own, so
     * only the deadline can explain the loop exiting. Pumping the remaining 200 frames afterward and finding
     * the target still unfocused is the proof: nothing was left retrying to claim it once it finally composed.
     */
    @Test
    fun `the offer gives up once its wall-clock deadline passes, never claiming a later target`() {
        val delayFrames = 200
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                    val arrival = rememberTvArrivalFocus()
                    TvArrivalFocusEffect(arrival)
                    TvLateTarget(delayFrames = delayFrames, dispose = true) {
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
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()

        ShadowSystemClock.advanceBy(PAST_ARRIVAL_TIMEOUT)
        repeat(delayFrames) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertExists()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
    }

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
