package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import com.binge.designsystem.tv.focus.TV_FOCUS_SINK_TAG
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ITEM_KEY = "item"
private const val RAIL_ITEM = "rail item"

/** Well past [FOCUS_HANDOFF_FRAMES] (120): the startup loop has long since returned by this point. */
private const val PROBE_FRAMES = 200

/**
 * #2523: the startup `LaunchedEffect`'s `if (!contentHasFocus && !railHasFocus) railEntry.requestFocus()`
 * fallback (pre-#2518, target-less-destination era) is deleted here. This is the evidence it was safe to delete
 * — driven through [BingeTvNavRail] rather than in isolation, exactly the destination the fallback existed for:
 * one that composes nothing focusable at all, ever.
 *
 * With [TvFocusSink] mounted, that destination is no longer target-less: the sink claims the content group's
 * entry on its first frame, satisfying the startup loop's `taken()` check before the deleted line could ever
 * run. What happens *after* is the oscillation #2518's own PRs measured and shipped anyway (a sink that holds
 * focus is itself "the pane has focus", so it disposes itself the next frame, and Compose's own disposal-
 * triggered recovery search — not this rail's code — lands the frame in between): sink and rail alternate every
 * other frame, indefinitely, for a destination this pathological. The fallback's guard needed *both*
 * `contentHasFocus` and `railHasFocus` false at once to fire, and that never happens — one of the two is always
 * true, every single frame, confirmed here across a window well past the startup budget. Deleting the line
 * changes nothing observable (full module suite green before and after); this test is what backs that claim
 * rather than asserting it in a PR description alone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailStartupFallbackRemovalTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `content or rail holds focus every frame for a destination with nothing focusable, ever`() {
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                rail {
                    // Nothing here, ever — the exact case the deleted fallback existed to cover.
                }
            }
        }

        repeat(PROBE_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            val railFocused = composeTestRule.onAllNodesWithText(RAIL_ITEM).fetchSemanticsNodes().any {
                it.config.getOrElse(SemanticsProperties.Focused) { false }
            }
            val sinkFocused = composeTestRule.onAllNodesWithTag(TV_FOCUS_SINK_TAG).fetchSemanticsNodes().any {
                it.config.getOrElse(SemanticsProperties.Focused) { false }
            }
            assertTrue(
                "neither the rail nor the sink held focus at frame $it — the deleted fallback would have " +
                    "been reachable here",
                railFocused || sinkFocused,
            )
        }
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
