package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val SIBLING = "sibling"
private const val CONTENT = "content"

/** Frames to run after a change — a handful, since the effect makes its one request on the first pass or not at all. */
private const val PUMP_FRAMES = 5

/** The measured gap between an incoming screen's mount and the outgoing screen's disposal, on a Shield. */
private const val DISPOSAL_DELAY_MS = 300L

/**
 * The contract for the arrival triple consolidated in [TvArrivalFocus]/[TvArrivalFocusEffect]/
 * [TvOverlayArrivalFocusEffect]. Each effect makes one request and never retries: the arrival effect claims focus
 * for its target *before* the outgoing screen disposes, waits for its `enabled` gate, makes that request once per
 * arrival, and stands down for a user move or a target that already holds focus; the overlay effect makes its
 * request when it composes, including on a node mounted mid-transition.
 *
 * The ordering assertions look device-only, since Robolectric lays out synchronously. They are not: holding the
 * clock with `autoAdvance = false` and stepping frames orders the mount, the claim and the disposal deliberately.
 *
 * Every fixture keeps a **rail stand-in** holding focus first: without a focusable elsewhere, Compose
 * recovers focus onto the sole remaining node unaided and a no-op effect would pass for the wrong reason. The
 * rail is where focus sits until the request moves it, so asserting the content holds focus proves the request ran.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvArrivalFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    /**
     * The arrival claims focus for the content even with other focusables present (the rail, a not-yet-disposed
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

        // The outgoing screen tearing down must not knock focus off the content the request placed.
        siblingPresent = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /**
     * With the gate shut no request is made, so the rail keeps focus; opening it (false→true) makes it and the
     * content claims focus. Keying the effect on `Unit` instead of `enabled` fails this — the request would run once
     * against the shut gate and never retry.
     */
    @Test
    fun `the arrival effect requests when enabled flips false to true`() {
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
     * regression net for the five overlays migrated onto it. Its one request, made as the overlay composes, must
     * place focus after such a mount — red when the effect is a no-op.
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
     * The arrival makes no further request once its target has held focus, so it never fights the user. A request
     * made again would undo a move back to the rail within a frame or two, and a held D-pad press would stutter.
     *
     * Pumping frames after the move is the assertion: `waitForIdle` alone would pass even against a repeating
     * request, because the repeat itself settles.
     */
    @Test
    fun `the arrival stops once its target holds focus, so a move away is not fought`() {
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
     * The arrival claims focus **before** the outgoing screen disposes, which is the whole fix rather than a
     * detail of it: once the surface holds focus the outgoing nodes are already unfocused when they go, so
     * their geometric reassignment never runs. The outgoing screen holds focus when the incoming one mounts,
     * and its disposal lands a transition later.
     *
     * `autoAdvance = false` is what makes that orderable: the assertion sits *between* the mount and the
     * disposal, so it fails for a request that waits for the disposal rather than merely reporting where focus
     * ended up.
     */
    @Test
    fun `the arrival claims focus while the outgoing screen still holds it, and keeps it when that screen disposes`() {
        var siblingPresent by mutableStateOf(true)
        var incomingPresent by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    if (siblingPresent) {
                        Box(Modifier.testTag(SIBLING).size(80.dp).focusable())
                    }
                    if (incomingPresent) ArrivalContent()
                }
            }
        }
        composeTestRule.onNodeWithTag(SIBLING).requestFocus()
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(SIBLING).assertIsFocused()

        composeTestRule.runOnUiThread { incomingPresent = true }
        repeat(PUMP_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.waitForIdle()
        }

        // Mid-transition: the outgoing screen is still composed and its node still exists.
        composeTestRule.onNodeWithTag(SIBLING).assertExists()
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        composeTestRule.mainClock.advanceTimeBy(DISPOSAL_DELAY_MS)
        composeTestRule.runOnIdle { siblingPresent = false }
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /**
     * The arrival is one request. Once it has claimed focus and the user has moved on, the target attaching again
     * (the same holder, the node composed out and back) does not claim it back.
     */
    @Test
    fun `the arrival fires once, so a target that attaches again does not take focus back`() {
        var targetPresent by mutableStateOf(true)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    val arrival = rememberTvArrivalFocus()
                    TvArrivalFocusEffect(arrival)
                    if (targetPresent) {
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
        }
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        targetPresent = false
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        targetPresent = true
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.onNodeWithTag(CONTENT).assertIsNotFocused()
    }

    /**
     * A direction press by the user since the arrival composed cancels the request, so an arrival whose target
     * attaches late never pulls focus back from where the user went (the bar, above all).
     */
    @Test
    fun `a user move since the arrival cancels it`() {
        val moves = TvUserMoves()
        var enabled by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            CompositionLocalProvider(LocalTvUserMoves provides moves) {
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
        }
        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        moves.note()
        enabled = true
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.onNodeWithTag(CONTENT).assertIsNotFocused()
    }

    /**
     * A surface that arrives again without leaving the composition (controls coming back after a sheet closes) passes a
     * new [arrivalKey]; a changed key is a fresh request, and recomposing with the same key is not. Only the last key
     * is remembered, so returning to an earlier key is a fresh request too.
     */
    @Test
    fun `a changed arrival key is a fresh request, and an unchanged key is not`() {
        var key by mutableStateOf(1)
        var unrelated by mutableStateOf(0)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                Row {
                    RailStandIn()
                    val arrival = rememberTvArrivalFocus()
                    TvArrivalFocusEffect(arrival, arrivalKey = key)
                    Box(
                        Modifier
                            .testTag(CONTENT)
                            .size((80 + unrelated).dp)
                            .tvArrivalTarget(arrival)
                            .focusable(),
                    )
                }
            }
        }
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()

        composeTestRule.onNodeWithTag(RAIL).requestFocus()
        repeat(PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()

        composeTestRule.runOnUiThread { unrelated = 1 }
        repeat(PUMP_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()

        composeTestRule.runOnUiThread { key = 2 }
        repeat(PUMP_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.waitForIdle()
        }
        composeTestRule.onNodeWithTag(CONTENT).assertIsFocused()
    }

    /** The stand-in the request moves focus away from — the rail every arrival surface sits beside. */
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
