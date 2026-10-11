package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.focus.TvArrivalFocusEffect
import com.binge.designsystem.tv.focus.rememberTvArrivalFocus
import com.binge.designsystem.tv.focus.tvArrivalTarget
import com.binge.designsystem.tv.theme.BingeTvTheme
import kotlinx.coroutines.delay
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val SETTINGS = "settings"
private const val CONTENT = "content"

/** A cold first screen: a skeleton with nothing focusable for this long, then its real content. */
private const val LOADING_MILLIS = 3_000L

/** Frames to watch: the loading window and well past it. */
private const val WATCH_FRAMES = 300

/**
 * At a cold start the shell's first screen shows a skeleton with nothing focusable, then its real content. Focus
 * must never visit the rail on the way: a rail item that takes focus draws its focus plate, so focus passing through
 * the rail reads as the rail flicking between its focused and resting look for as long as the skeleton shows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvShellStartupFocusTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val tab = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)
    private val focused = SemanticsMatcher.expectValue(SemanticsProperties.Focused, true)

    @Test
    fun `a destination that claims its own arrival focus is reached without visiting the rail`() {
        val trace = watch { ClaimingDestination() }

        assertEquals("focus visited the rail: $trace", 0, trace.count { it == Holder.Rail })
        assertEquals(Holder.Content, trace.last())
    }

    @Test
    fun `a destination that relies on the shell's entry is reached without visiting the rail`() {
        val trace = watch { PassiveDestination() }

        assertEquals("focus visited the rail: $trace", 0, trace.count { it == Holder.Rail })
        assertEquals(Holder.Content, trace.last())
    }

    private enum class Holder { Rail, Content, Elsewhere }

    /** Where focus is at each frame, collapsed to its changes. */
    private fun watch(destination: @Composable () -> Unit): List<Holder> {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            BingeTvTheme {
                TvShellScaffold(
                    header = null,
                    items = listOf(TvNavRailItem(key = HOME, label = "Home", icon = Icons.Filled.Home)),
                    footer = TvNavRailItem(key = SETTINGS, label = "Settings", icon = Icons.Filled.Settings),
                    selectedKey = HOME,
                    homeKey = HOME,
                    onSelect = {},
                ) { destination() }
            }
        }
        val trace = mutableListOf<Holder>()
        repeat(WATCH_FRAMES) {
            rule.mainClock.advanceTimeByFrame()
            val holder =
                when {
                    rule.onAllNodes(tab and focused, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() -> Holder.Rail
                    rule.onAllNodes(hasTestTag(CONTENT) and focused).fetchSemanticsNodes().isNotEmpty() -> Holder.Content
                    else -> Holder.Elsewhere
                }
            if (trace.lastOrNull() != holder) trace += holder
        }
        return trace
    }

    @Composable
    private fun ClaimingDestination() {
        val loaded = loadsAfterDelay()
        val arrival = rememberTvArrivalFocus()
        if (loaded) {
            Box(
                Modifier
                    .size(40.dp)
                    .testTag(CONTENT)
                    .tvArrivalTarget(arrival)
                    .focusable(),
            )
        } else {
            Box(Modifier.size(40.dp))
        }
        TvArrivalFocusEffect(arrival, enabled = loaded)
    }

    @Composable
    private fun PassiveDestination() {
        if (loadsAfterDelay()) Box(Modifier.size(40.dp).testTag(CONTENT).focusable()) else Box(Modifier.size(40.dp))
    }

    @Composable
    private fun loadsAfterDelay(): Boolean {
        var loaded by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            delay(LOADING_MILLIS)
            loaded = true
        }
        return loaded
    }
}
