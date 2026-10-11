package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.TV_FOCUS_SINK_TAG
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertFalse
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
 * The startup `LaunchedEffect`'s `if (!contentHasFocus && !railHasFocus) railEntry.requestFocus()`
 * fallback (from before [TvFocusSink], when a destination could be target-less) is deleted here. This is the evidence it
 * was safe to delete, driven through [BingeTvNavRail] with exactly the destination the fallback existed for: one that
 * composes nothing focusable at all, ever.
 *
 * With [TvFocusSink] mounted, that destination is no longer target-less: the sink claims the content group's entry on
 * its first frame and keeps it, since the rail no longer disposes a sink that holds focus. So the content side holds
 * focus every frame, and the rail never does. The rail once held it every other frame here, which on a device read as
 * the rail flicking for as long as a cold first screen loaded.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailStartupFallbackRemovalTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the sink holds focus every frame for a destination with nothing focusable, ever, and the rail never does`() {
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
            assertTrue("the sink did not hold focus at frame $it", sinkFocused)
            assertFalse("the rail held focus at frame $it", railFocused)
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
