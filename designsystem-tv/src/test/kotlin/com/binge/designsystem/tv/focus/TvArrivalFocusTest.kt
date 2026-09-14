package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val SIBLING = "sibling"
private const val CONTENT = "content"

/** Frames to run the offer loop over — a handful, since it claims on its first pass or not at all. */
private const val PUMP_FRAMES = 5

/** The measured gap between an incoming screen's mount and the outgoing screen's disposal, on a Shield. */
private const val DISPOSAL_DELAY_MS = 300L

/**
 * The contract for the arrival triple consolidated in [TvArrivalFocus]/[TvArrivalFocusEffect]/
 * [TvOverlayArrivalFocusEffect]: the offer claims focus for the target, claims it *before* the outgoing screen
 * disposes, stops the frame the target takes it, re-offers when its data gate opens, and the overlay effect
 * lands focus a frame after a mid-transition mount.
 *
 * The last two of those were once filed as device-only, on the grounds that Robolectric lays out
 * synchronously. They are not: holding the clock with `autoAdvance = false` and stepping frames orders the
 * mount, the claim and the disposal deliberately, which is what those assertions actually need. Both are
 * fault-proven — removing the `taken()` early-return fails the stop test, and replacing the per-frame offer with
 * a single mount-time request fails the disposal-race test.
 *
 * Every fixture keeps a **rail stand-in** holding focus first: without a focusable elsewhere, Compose
 * recovers focus onto the sole remaining node unaided and a no-op effect would pass for the wrong reason. The
 * rail is where focus sits until the offer moves it, so asserting the content holds focus proves the offer ran.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvArrivalFocusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The offer claims focus for the content even with other focusables present (the rail, a not-yet-disposed
     * sibling), and keeps it once that sibling disposes — the "claim early so the outgoing screen's disposal
     * finds our nodes already unfocused" behaviour. A no-op effect leaves the content unfocused: nothing else
     * requests it, and the rail stand-in stops Compose recovering focus onto it unaided.
     */
    @Test
    fun `the arrival effect claims focus for the target past a disposing sibling`() {
        var siblingPresent by mutableStateOf(true)
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    if (siblingPresent) {
                        Box(Modifier.testTag(SIBLING).size(80.dp).focusable())
                    }
                    ArrivalContent()
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        // The outgoing screen tearing down must not knock focus off the content the offer placed.
        siblingPresent = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /**
     * With the gate shut the offer never runs, so the rail keeps focus; opening it (false→true) re-offers and the
     * content claims focus. Keying the effect on `Unit` instead of `enabled` fails this — the offer would run once
     * against the shut gate and never retry.
     */
    @Test
    fun `the arrival effect re-offers when enabled flips false to true`() {
        var enabled by mutableStateOf(false)
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    val arrival = rememberTvArrivalFocus()
                    TvArrivalFocusEffect(arrival, enabled = enabled)
                    Box(
                        Modifier
                            .testTag(CONTENT)
                            .size(80.dp)
                            .tvArrivalTarget(arrival)
                            .focusable(),
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(CONTENT).assertIsNotFocused()

        enabled = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /**
     * The overlay effect lands focus on a node mounted mid-transition (an `AnimatedContent` swap) — the
     * regression net for the five overlays migrated onto it. This pins that the effect *places* focus after such
     * a mount — red when it is a no-op.
     *
     * It does not pin the frame wait, which this mount has nothing to swallow a request with. That took a
     * focus-trapped overlay and is now covered in `TvOverlayCloserTest`; the caveat that used to sit here, saying
     * the wait's necessity was a device-timing property no JVM harness could reproduce, was wrong.
     */
    @Test
    fun `the overlay effect lands focus after a mid-transition mount`() {
        var shown by mutableStateOf(false)
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    AnimatedContent(targetState = shown, label = "overlay") { visible ->
                        if (visible) {
                            val focus = remember { FocusRequester() }
                            TvOverlayArrivalFocusEffect(focus)
                            Box(
                                Modifier
                                    .testTag(CONTENT)
                                    .size(80.dp)
                                    .focusRequester(focus)
                                    .focusable(),
                            )
                        } else {
                            Box(Modifier.size(80.dp))
                        }
                    }
                }
            }
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()

        shown = true
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /**
     * The offer stops the frame its target takes focus, so it never fights the user. With the loop still
     * running, a move back to the rail is undone within a frame or two and a held D-pad press stutters —
     * the reason `offerTvArrivalFocus` early-returns on `taken()` rather than always burning its budget.
     *
     * Pumping frames after the move is the assertion: `waitForIdle` alone would pass even against a loop
     * that re-requests, because the re-request itself settles.
     */
    @Test
    fun `the offer stops once its target holds focus, so a move away is not fought`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    ArrivalContent()
                }
            }
        }
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.onNodeWithTag(CONTENT).assertIsNotFocused()
    }

    /**
     * The offer claims focus **before** the outgoing screen disposes, which is the whole fix rather than a
     * detail of it: once the surface holds focus the outgoing nodes are already unfocused when they go, so
     * their geometric reassignment never runs. The device measurement this models is a disposal landing
     * ~300 ms after the incoming screen mounts.
     *
     * `autoAdvance = false` is what makes that orderable: the assertion sits *between* the mount and the
     * disposal, so it fails for a mount-time single-shot request that lost the race rather than merely
     * reporting where focus ended up. Running the clock forward lets the disposal land where the device
     * puts it instead of at the next idle.
     */
    @Test
    fun `the offer claims focus before a sibling disposes a transition later`() {
        var siblingPresent by mutableStateOf(true)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    if (siblingPresent) {
                        Box(Modifier.testTag(SIBLING).size(80.dp).focusable())
                    }
                    ArrivalContent()
                }
            }
        }
        composeTestRule.onNodeWithTag(SIBLING).requestFocus()
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        // Mid-transition: the outgoing screen is still composed and its node still exists.
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        composeTestRule.mainClock.advanceTimeBy(DISPOSAL_DELAY_MS)
        siblingPresent = false
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /** The stand-in the offer moves focus away from — the rail every arrival surface sits beside. */
    @Composable
    private fun RailStandIn() {
        Box(Modifier.testTag(RAIL).size(80.dp).focusable())
    }

    /** A focusable content node wired with the arrival holder + effect, exactly as a screen wires it. */
    @Composable
    private fun ArrivalContent() {
        val arrival = rememberTvArrivalFocus()
        TvArrivalFocusEffect(arrival)
        Box(
            Modifier
                .testTag(CONTENT)
                .size(80.dp)
                .tvArrivalTarget(arrival)
                .focusable(),
        )
    }
}
