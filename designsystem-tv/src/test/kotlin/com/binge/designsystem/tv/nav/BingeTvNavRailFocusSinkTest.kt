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
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
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

/** Frames the sink is given to win the pane before it must hold it continuously. */
private const val EARLY_WINDOW_FRAMES = 2

/** Frames to settle after the delayed target composes. */
private const val SETTLE_FRAMES = 10

/**
 * [TvFocusSink]'s integration into [BingeTvNavRail]'s content group, driven through the real group
 * rather than in isolation — [BingeTvNavRailAdverseScheduleFocusTest] already proves the retry loop converges
 * with the sink present; this file asserts on the sink itself along that same path, plus its absence case.
 *
 * **A continuous hold.** The sink wins the pane within the first frames and keeps it, frame after frame, until the
 * destination grows a target. The rail no longer disposes a sink that holds focus, so nothing hands Compose's
 * recovery search a frame to send focus to the rail: it once landed every other frame there, which on a device read
 * as the rail flicking while a cold first screen loaded. The sink then hands focus to the target and goes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailFocusSinkTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the sink holds the pane every frame while content has nothing, then yields once the target composes`() {
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
        repeat(EARLY_WINDOW_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        repeat(TARGET_DELAY_FRAMES - EARLY_WINDOW_FRAMES * 2) { frame ->
            val sinkNodes = composeTestRule.onAllNodesWithTag(TV_FOCUS_SINK_TAG).fetchSemanticsNodes()
            check(sinkNodes.any { it.config.getOrElse(SemanticsProperties.Focused) { false } }) {
                "the sink did not hold the pane ${frame + EARLY_WINDOW_FRAMES} frames in"
            }
            composeTestRule.mainClock.advanceTimeByFrame()
        }

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
