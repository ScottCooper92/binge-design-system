package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.focus.TV_FOCUS_SINK_TAG
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

/** How long [TvLateTarget] withholds the real target for, in the first case below. */
private const val TARGET_DELAY_FRAMES = 30

/** A window checked across, not a single frame: see the class KDoc's note on why the grant is not continuous. */
private const val EARLY_WINDOW_FRAMES = 6

/** Frames to settle after the delayed target composes. */
private const val SETTLE_FRAMES = 10

/**
 * [TvFocusSink]'s integration into [BingeTvNavRail]'s content group (#2518), driven through the real group
 * rather than in isolation — [BingeTvNavRailAdverseScheduleFocusTest] already proves the retry loop converges
 * with the sink present; this file asserts on the sink itself along that same path, plus its absence case.
 *
 * **Not a continuous, single-frame hold.** The group has no `onEnter`, so a repeated entry request re-resolves
 * the *default* candidate each time rather than holding the prior grant — measured directly (see git history on
 * this file): the sink wins the pane, is disposed the moment it is itself the thing focused (its own presence
 * gate reads the pane's own `hasFocus`), and is re-composed and re-won the next frame, with Compose's own
 * disposal-triggered recovery search landing the *in-between* frame wherever it geometrically would without the
 * sink at all. So the window below asserts the sink wins the pane repeatedly through the wait — real content is
 * never left target-less for a sustained stretch — rather than a specific frame's parity or an uninterrupted
 * hold. Whether that in-between frame should also be closed off (a shell-root interceptor, not a second sink)
 * is #2517's own open question, not this issue's.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailFocusSinkTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `the sink wins the pane repeatedly while content has nothing, then yields once the target composes`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail {
                    TvLateTarget(delayFrames = TARGET_DELAY_FRAMES, dispose = true) {
                        Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                    }
                }
            }
        }
        var sinkWonAFrame = false
        repeat(EARLY_WINDOW_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            val sinkNodes = composeTestRule.onAllNodesWithTag(TV_FOCUS_SINK_TAG).fetchSemanticsNodes()
            if (sinkNodes.any { it.config.getOrElse(SemanticsProperties.Focused) { false } }) sinkWonAFrame = true
        }
        check(sinkWonAFrame) { "the sink never won the pane in the first $EARLY_WINDOW_FRAMES frames" }

        repeat(TARGET_DELAY_FRAMES + SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        composeTestRule.onAllNodesWithTag(TV_FOCUS_SINK_TAG).assertCountEquals(0)
    }

    @Test
    fun `the sink is absent whenever the pane already holds focus`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail {
                    Box(Modifier.testTag(TARGET).size(80.dp).focusable())
                }
            }
        }
        repeat(SETTLE_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(TARGET).assertIsFocused()
        composeTestRule.onAllNodesWithTag(TV_FOCUS_SINK_TAG).assertCountEquals(0)
    }

    @Composable
    private fun rail(content: @Composable () -> Unit) {
        BingeTvNavRail(
            header = null,
            items = listOf(TvNavRailItem(key = ITEM_KEY, label = RAIL_ITEM, icon = Icons.Filled.Home)),
            footer = null,
            selectedKey = ITEM_KEY,
            onSelect = {},
            expanded = true,
            content = content,
        )
    }
}
