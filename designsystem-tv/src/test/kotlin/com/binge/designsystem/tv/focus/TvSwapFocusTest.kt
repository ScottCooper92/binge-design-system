package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val OLD = "old"
private const val REPLACEMENT = "replacement"
private const val LATE_ATTACH_FRAMES = 10
private const val PAST_THE_WINDOW_MS = 3_500L

/**
 * The contract for [TvSwapFocus]/[TvSwapFocusEffect]: [TvSwapFocus.arm] on the press, then one request when a
 * [tvSwapTarget] attaches after the arm, then disarm. A replacement with no prior arm never gets focus, so a background
 * state change can't steal it; a target already on screen at the arm is the control being swapped out, not the
 * replacement; and an arm that no replacement answers expires instead of waiting for a later, unrelated change.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSwapFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `an armed swap focuses the replacement when it attaches and disarms`() {
        val fixture = fixture()
        fixture.focusOld()

        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()
        composeTestRule.runOnIdle { assertFalse("swap must disarm after firing", fixture.swap.armed) }
    }

    /** With no arm, a replacement attaching must leave focus alone: the background-change guard. */
    @Test
    fun `an unarmed swap does not steal focus when the replacement attaches`() {
        val fixture = fixture()
        fixture.focusRail()

        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsNotFocused()
    }

    @Test
    fun `the swap re-arms across presses`() {
        val fixture = fixture()
        fixture.focusOld()
        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()
        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()

        composeTestRule.runOnIdle { fixture.replaced = false }
        composeTestRule.waitForIdle()
        fixture.focusOld()
        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()
    }

    /**
     * The replacement may compose a while after the press (a reload racing the arrival). The arm waits for its target to
     * attach, so the one request still lands. Making the effect request at the arm instead fails this test: it fires at
     * the old control and the replacement is never focused.
     */
    @Test
    fun `a replacement that composes late still gets the one request`() {
        val fixture = fixture()
        fixture.lateReplacement = true
        fixture.focusOld()

        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()
        composeTestRule.runOnIdle { assertFalse("swap must disarm once it lands", fixture.swap.armed) }
    }

    /** The old control is a target too (a "new code" button that is swapped for a QR confirm): arming beside it requests nothing. */
    @Test
    fun `a target already on screen when the swap is armed is not taken for the replacement`() {
        val fixture = fixture()
        fixture.oldIsTarget = true
        fixture.focusRail()

        composeTestRule.runOnIdle { fixture.swap.arm() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.runOnIdle { assertTrue("still waiting for the replacement", fixture.swap.armed) }

        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()
    }

    @Test
    fun `an arm no replacement answers expires and a later change does not steal focus`() {
        val fixture = fixture()
        fixture.focusRail()

        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.runOnUiThread { fixture.swap.arm() }
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.mainClock.advanceTimeByFrame()
        composeTestRule.runOnIdle { assertTrue("armed until the window passes", fixture.swap.armed) }
        composeTestRule.mainClock.advanceTimeBy(PAST_THE_WINDOW_MS)
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle { assertFalse("the arm must expire", fixture.swap.armed) }

        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsNotFocused()
    }

    private fun fixture(): Fixture {
        val fixture = Fixture()
        composeTestRule.setContent {
            BingeTvTheme {
                fixture.swap = rememberTvSwapFocus()
                TvSwapFocusEffect(swap = fixture.swap)
                Row {
                    // A focusable elsewhere, so an absent request has somewhere for focus to be other than the
                    // replacement; otherwise Compose recovers onto it unaided and the guard tests pass for the wrong reason.
                    Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                    if (fixture.replaced) {
                        var mounted by remember { mutableStateOf(!fixture.lateReplacement) }
                        if (!mounted) {
                            LaunchedEffect(Unit) {
                                repeat(LATE_ATTACH_FRAMES) { withFrameNanos { } }
                                mounted = true
                            }
                        }
                        if (mounted) {
                            Box(
                                Modifier
                                    .testTag(REPLACEMENT)
                                    .size(80.dp)
                                    .tvSwapTarget(fixture.swap)
                                    .focusable(),
                            )
                        }
                    } else {
                        Box(
                            Modifier
                                .testTag(OLD)
                                .size(80.dp)
                                .then(if (fixture.oldIsTarget) Modifier.tvSwapTarget(fixture.swap) else Modifier)
                                .focusable(),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        return fixture
    }

    private inner class Fixture {
        var replaced by mutableStateOf(false)
        var lateReplacement by mutableStateOf(false)
        var oldIsTarget by mutableStateOf(false)
        lateinit var swap: TvSwapFocus

        /** Plant focus on the pre-swap control: the state a press starts from. */
        fun focusOld() {
            composeTestRule.onNodeWithTag(OLD).requestFocus()
            composeTestRule.onNodeWithTag(OLD).assertIsFocused()
        }

        fun focusRail() {
            composeTestRule.onNodeWithTag(RAIL).requestFocus()
            composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        }

        /** Swap the replacement subtree in, as the press's state change does. */
        fun swapInReplacement() {
            composeTestRule.runOnIdle { replaced = true }
            composeTestRule.waitForIdle()
        }
    }
}
