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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val RAIL = "rail"
private const val OLD = "old"
private const val REPLACEMENT = "replacement"

/**
 * The contract for [TvSwapFocus]/[TvSwapFocusEffect], the swap-focus latch the sign-in/out and go-home
 * hand-rolled `pending*Focus` flags fold into: [TvSwapFocus.arm] on the press, then the effect re-focuses the
 * replacement once `ready` arrives and disarms — and, crucially, a `ready` transition with *no* prior arm never
 * fires, so a background state change can't steal focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSwapFocusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `an armed swap focuses the replacement once ready and disarms`() {
        val fixture = fixture()
        fixture.focusOld()

        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()
        composeTestRule.runOnIdle { assertFalse("swap must disarm after firing", fixture.swap.armed) }
    }

    /** With no arm, a `ready` transition must leave focus alone — the background-change guard. */
    @Test
    fun `an unarmed swap does not steal focus when ready`() {
        val fixture = fixture()
        fixture.focusRail()

        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(RAIL).assertIsFocused()
        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsNotFocused()
    }

    /** The latch re-arms across swaps, so a second press re-focuses too. */
    @Test
    fun `the swap re-arms across presses`() {
        val fixture = fixture()
        fixture.focusOld()
        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()
        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()

        // Return to the old subtree, then arm and swap again.
        composeTestRule.runOnIdle { fixture.ready = false }
        composeTestRule.waitForIdle()
        fixture.focusOld()
        composeTestRule.runOnIdle { fixture.swap.arm() }
        fixture.swapInReplacement()

        composeTestRule.onNodeWithTag(REPLACEMENT).assertIsFocused()
    }

    private fun fixture(): Fixture {
        val fixture = Fixture()
        composeTestRule.setContent {
            BingeTvTheme {
                fixture.swap = rememberTvSwapFocus()
                TvSwapFocusEffect(swap = fixture.swap, ready = fixture.ready)
                Row {
                    // A focusable elsewhere, so a failed/absent request has somewhere for focus to be other than
                    // the replacement — otherwise Compose recovers onto it unaided and the guard test passes for
                    // the wrong reason.
                    Box(Modifier.testTag(RAIL).size(80.dp).focusable())
                    if (fixture.ready) {
                        Box(
                            Modifier
                                .testTag(REPLACEMENT)
                                .size(80.dp)
                                .focusRequester(fixture.swap.requester)
                                .focusable(),
                        )
                    } else {
                        Box(Modifier.testTag(OLD).size(80.dp).focusable())
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        return fixture
    }

    private inner class Fixture {
        var ready by mutableStateOf(false)
        lateinit var swap: TvSwapFocus

        /** Plant focus on the pre-swap control — the state a press starts from. */
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
            composeTestRule.runOnIdle { ready = true }
            composeTestRule.waitForIdle()
        }
    }
}
