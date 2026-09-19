package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
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

private const val ITEM_KEY = "item"
private const val RAIL_ITEM = "rail item"
private const val TARGET = "target"

/** Frames to settle the offer loop after its target should already be composed and focusable. */
private const val SETTLE_FRAMES = 10

/**
 * The adverse-schedule cases #2521 asks for on [BingeTvNavRail]'s two content-side handoffs: the startup offer
 * (cold start) and [contentHandoffInFlight] (drill-down), each proven to converge for a target that only
 * composes N frames after the loop starts — not merely one that attaches late, which [TvLateTarget] rules out
 * by construction (see its KDoc). N = 60 is the deliberate ceiling (#2521's decision 3): a full second at 60fps,
 * comfortably inside both loops' frame budgets ([FOCUS_HANDOFF_FRAMES] 120, [CONTENT_HANDOFF_FRAMES] 600).
 *
 * Convergence is asserted per #2521's decision 4 — the target holds focus and the rail does not — not the path
 * taken to get there.
 *
 * [overlayCloseHandoffInFlight] gets the same N-frames-late cases (#65): it shares [contentHandoffInFlight]'s
 * exact retry shape (`withTimeoutOrNull([CONTENT_HANDOFF_TIMEOUT_MS])` around `offerFocusToContent(frames =
 * [CONTENT_HANDOFF_FRAMES])`), but unlike a drill-down it never waits on a focus loss — its own KDoc — so its
 * fixture parks focus on the rail item exactly as [BingeTvNavRailFocusTest]'s own case for it models (see that
 * class's KDoc), rather than disposing/remounting the rail itself: #65 found that a remount races this handoff's
 * retry against the startup offer's own against the same content [androidx.compose.ui.focus.FocusRequester] with
 * no way to tell which one delivered focus, which is exactly what composing the rail once and bumping
 * `overlayEpoch` as a plain state change avoids.
 *
 * [contentHandoffInFlight] gets one more case (#72): unlike the startup offer, it wraps its retry in
 * [kotlinx.coroutines.withTimeoutOrNull]([CONTENT_HANDOFF_TIMEOUT_MS]) — a real elapsed-time cap distinct from
 * [CONTENT_HANDOFF_FRAMES]'s retry count. It reads as a wall-clock trap, but under this harness it isn't one:
 * [createComposeRule]'s composition — every `LaunchedEffect`, including the one `withTimeoutOrNull` schedules
 * its deadline on — runs on the `TestDispatcher` backing `composeTestRule.mainClock`'s own
 * `TestCoroutineScheduler` (`AndroidComposeUiTestEnvironment` builds `TestMonotonicFrameClock` and the
 * `Recomposer`'s effect context from that same dispatcher; verified by decompiling `ui-test-android:1.12.0`).
 * `mainClock.advanceTimeByFrame()`/`advanceTimeBy()` call that scheduler's own `advanceTimeBy` + `runCurrent`,
 * so stepping the frame clock *is* advancing the timeout's clock — no [android.os.SystemClock] or
 * `ShadowSystemClock` involved, unlike [TvArrivalFocusAdverseScheduleTest]'s wall-clock case for
 * `offerTvArrivalFocus`'s direct `SystemClock.uptimeMillis()` read. A frame is 16ms of that virtual time
 * (`TestMonotonicFrameClock`'s default), so 250 frames (4000ms) crosses [CONTENT_HANDOFF_TIMEOUT_MS] (3000ms)
 * while [CONTENT_HANDOFF_FRAMES]'s own 600-frame budget (9600ms) is nowhere close to exhausted — only the
 * timeout explains giving up. Confirmed empirically (not just derived): once the loop gives up, Compose's own
 * disposal-triggered recovery search (the same mechanism [BingeTvNavRailFocusSinkTest] documents) settles on
 * the rail item and stays there — a real, stable focusable, not [TvFocusSink]'s churn — so the replacement
 * composing later never gets a look-in even once it exists.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailAdverseScheduleFocusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the startup offer converges on a target composed 0 frames late`() = assertColdStartConverges(0)

    @Test
    fun `the startup offer converges on a target composed 1 frame late`() = assertColdStartConverges(1)

    @Test
    fun `the startup offer converges on a target composed 10 frames late`() = assertColdStartConverges(10)

    @Test
    fun `the startup offer converges on a target composed 60 frames late`() = assertColdStartConverges(60)

    @Test
    fun `the drill-down handoff converges on a replacement composed 0 frames late`() = assertDrillDownConverges(0)

    @Test
    fun `the drill-down handoff converges on a replacement composed 1 frame late`() = assertDrillDownConverges(1)

    @Test
    fun `the drill-down handoff converges on a replacement composed 10 frames late`() = assertDrillDownConverges(10)

    @Test
    fun `the drill-down handoff converges on a replacement composed 60 frames late`() = assertDrillDownConverges(60)

    @Test
    fun `the overlay-close handoff converges on a target composed 0 frames late`() = assertOverlayCloseConverges(0)

    @Test
    fun `the overlay-close handoff converges on a target composed 1 frame late`() = assertOverlayCloseConverges(1)

    @Test
    fun `the overlay-close handoff converges on a target composed 10 frames late`() = assertOverlayCloseConverges(10)

    @Test
    fun `the overlay-close handoff converges on a target composed 60 frames late`() = assertOverlayCloseConverges(60)

    /**
     * The wall-clock deadline's own success case: 170 frames (2720ms of the shared virtual clock — see the
     * class KDoc) is close enough to [CONTENT_HANDOFF_TIMEOUT_MS] (3000ms) to matter, while still landing with
     * a comfortable margin before it — proving the deadline does not fire early on a replacement that is merely
     * slow, only on one that is later than the budget allows (below).
     */
    @Test
    fun `the drill-down handoff still converges on a replacement composed 170 frames late, just under the deadline`() =
        assertDrillDownConverges(170)

    /**
     * The wall-clock deadline's own give-up case: 250 frames (4000ms) is past [CONTENT_HANDOFF_TIMEOUT_MS]
     * (3000ms) but nowhere near exhausting [CONTENT_HANDOFF_FRAMES]'s 600-frame (9600ms) budget on its own — so
     * only the timeout can explain the loop giving up. Pumping the remaining frames afterward and finding the
     * replacement still unfocused once it finally composes is the proof: nothing was left retrying to claim it.
     */
    @Test
    fun `the drill-down handoff gives up once its wall-clock deadline passes, never claiming a later replacement`() =
        assertDrillDownTimesOut(250)

    /** No sibling: the cold-start offer competes against nothing composed at all, the adversary this loop names. */
    private fun assertColdStartConverges(delayFrames: Int) {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail(contentDepth = 1) {
                    TvLateTarget(delayFrames = delayFrames, dispose = true) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        repeat(delayFrames + SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsNotFocused()
    }

    /**
     * The sibling stands in for the outgoing screen the drill-down disposes; flipping `dispose` alongside the
     * depth change models the real handoff, where the disposal is what the depth change is *about* — never a
     * same-recomposition swap onto an already-composed replacement, which [TvLateTarget] cannot produce for
     * `delayFrames > 0` (its whole point).
     */
    private fun assertDrillDownConverges(delayFrames: Int) {
        var contentDepth by mutableIntStateOf(1)
        var dispose by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail(contentDepth = contentDepth) {
                    TvLateTarget(delayFrames = delayFrames, dispose = dispose) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(TV_LATE_TARGET_SIBLING_TAG).assertIsFocused()

        contentDepth = 2
        dispose = true
        composeTestRule.waitForIdle()
        repeat(delayFrames + SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsNotFocused()
    }

    /**
     * [overlayCloseHandoffInFlight]'s own case (#65): the rail is parked with focus — a bare `requestFocus()`
     * on the rail item, no key event, [BingeTvNavRailFocusTest]'s "Compose recovery park" modelling (its class
     * KDoc) for exactly this handoff — then `overlayEpoch` bumps on the same composed rail while the content
     * group's target composes [delayFrames] frames late, standing in for the fresh pane an overlay pop returns
     * to. Unlike [assertDrillDownConverges] there is no prior "old" focus to sequence on and lose — the sibling
     * is never focused before `dispose` flips — matching the handoff's own contract that it never waits on a
     * focus loss (its KDoc). Fault-proven: this handoff's `yieldToRail` reads `userMovedToRail`, not a raw
     * `railHasFocus`; reverting it to the raw read — #54's own regression — fails this, since the parked rail
     * reads `railHasFocus == true` too and would keep the handoff from ever claiming the late target.
     */
    private fun assertOverlayCloseConverges(delayFrames: Int) {
        var overlayEpoch by mutableIntStateOf(0)
        var dispose by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail(contentDepth = 1, overlayEpoch = overlayEpoch) {
                    TvLateTarget(delayFrames = delayFrames, dispose = dispose) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithText(RAIL_ITEM).requestFocus()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()

        overlayEpoch = 1
        dispose = true
        composeTestRule.waitForIdle()
        repeat(delayFrames + SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsNotFocused()
    }

    /**
     * Same disposing-sibling setup as [assertDrillDownConverges], but [delayFrames] is chosen past
     * [CONTENT_HANDOFF_TIMEOUT_MS] rather than under it, so [contentHandoffInFlight]'s `withTimeoutOrNull`
     * cancels the retry before the replacement ever composes. The replacement still gets composed and pumped
     * to settle — the target [TvLateTarget] rules out by construction is a target that never shows up at all,
     * not one the loop was right to have stopped waiting for.
     */
    private fun assertDrillDownTimesOut(delayFrames: Int) {
        var contentDepth by mutableIntStateOf(1)
        var dispose by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail(contentDepth = contentDepth) {
                    TvLateTarget(delayFrames = delayFrames, dispose = dispose) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(TV_LATE_TARGET_SIBLING_TAG).assertIsFocused()

        contentDepth = 2
        dispose = true
        composeTestRule.waitForIdle()
        repeat(delayFrames + SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertExists()
        composeTestRule.onNodeWithTag(TARGET).assertIsNotFocused()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()
    }

    @Composable
    private fun rail(
        contentDepth: Int,
        overlayEpoch: Int = 0,
        content: @Composable () -> Unit,
    ) {
        BingeTvNavRail(
            header = null,
            items = listOf(TvNavRailItem(key = ITEM_KEY, label = RAIL_ITEM, icon = Icons.Filled.Home)),
            footer = null,
            selectedKey = ITEM_KEY,
            onSelect = {},
            expanded = true,
            contentDepth = contentDepth,
            overlayEpoch = overlayEpoch,
            content = content,
        )
    }
}
