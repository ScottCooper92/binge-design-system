package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.tv.material3.Text
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val SEARCH = "search"
private const val SETTINGS = "settings"
private const val FADE_MILLIS = 600

private val Home = TvNavRailItem(key = HOME, label = "Home", icon = Icons.Filled.Home)
private val Search = TvNavRailItem(key = SEARCH, label = "Search", icon = Icons.Filled.Search)
private val Settings = TvNavRailItem(key = SETTINGS, label = "Settings", icon = Icons.Filled.Settings)

/**
 * How [TvShellScaffold] composes its content across a destination swap. Keyed by destination, the old
 * destination's tree goes as the new one mounts, so an animation inside the content has nothing to fade from.
 * Unkeyed, the content is one tree and both panes are present partway through. A destination that leaves the
 * rail has its saved state dropped.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvShellScaffoldContentTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private var selectedKey by mutableStateOf<Any>(HOME)
    private var items by mutableStateOf(listOf(Home, Search))
    private var nextId = 0
    private lateinit var settle: TvShellSettle

    @Test
    fun `keyed by destination, a swap cuts, so the old pane is gone partway through a fade`() {
        showCrossfade(keyContentByDestination = true)

        swapPartwayThroughFade()

        assertEquals(0, panes("pane-$HOME"))
        assertEquals(1, panes("pane-$SEARCH"))
    }

    @Test
    fun `unkeyed, a swap fades, so both panes are present partway through`() {
        showCrossfade(keyContentByDestination = false)

        swapPartwayThroughFade()

        assertEquals(1, panes("pane-$HOME"))
        assertEquals(1, panes("pane-$SEARCH"))
    }

    @Test
    fun `unkeyed, the fade finishes on the new pane alone`() {
        showCrossfade(keyContentByDestination = false)

        swapPartwayThroughFade()
        rule.mainClock.advanceTimeBy(FADE_MILLIS.toLong())
        rule.waitForIdle()

        assertEquals(0, panes("pane-$HOME"))
        assertEquals(1, panes("pane-$SEARCH"))
    }

    @Test
    fun `keyed by destination, a destination's saved state is kept while it is off screen`() {
        showIds()
        val first = idOf(HOME)

        select(SEARCH)
        select(HOME)

        assertEquals(first, idOf(HOME))
    }

    @Test
    fun `a destination that leaves the rail has its saved state dropped`() {
        showIds()
        select(SEARCH)
        val first = idOf(SEARCH)
        select(HOME)

        items = listOf(Home)
        rule.waitForIdle()
        items = listOf(Home, Search)
        select(SEARCH)

        assertEquals(first + 1, idOf(SEARCH))
    }

    /** Each destination fades between panes inside one Crossfade, as a consumer's navigation host would. */
    private fun showCrossfade(keyContentByDestination: Boolean) {
        rule.setContent {
            BingeTvTheme {
                settle = rememberTvShellSettle(HOME)
                TvShellScaffold(
                    header = null,
                    items = items,
                    footer = Settings,
                    selectedKey = selectedKey,
                    homeKey = HOME,
                    onSelect = {},
                    settle = settle,
                    keyContentByDestination = keyContentByDestination,
                ) { key ->
                    Crossfade(targetState = key, animationSpec = tween(FADE_MILLIS), label = "panes") { shown ->
                        Text("pane-$shown")
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    /** Each destination shows an id handed out when its saved state was first created. */
    private fun showIds() {
        rule.setContent {
            BingeTvTheme {
                TvShellScaffold(
                    header = null,
                    items = items,
                    footer = Settings,
                    selectedKey = selectedKey,
                    homeKey = HOME,
                    onSelect = {},
                    settleMillis = 0L,
                ) { key ->
                    val id = rememberSaveable { nextId++ }
                    Text("$key:$id")
                }
            }
        }
        rule.waitForIdle()
    }

    /** Selects and settles on the next destination at once, so the clock covers the fade alone. */
    private fun swapPartwayThroughFade() {
        rule.mainClock.autoAdvance = false
        rule.runOnUiThread {
            selectedKey = SEARCH
            settle.jumpTo(SEARCH)
        }
        // Recompose onto the new key before the clock moves, so the fade starts at the swap.
        rule.waitForIdle()
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeBy(FADE_MILLIS / 2L)
    }

    private fun select(key: String) {
        selectedKey = key
        rule.waitForIdle()
    }

    private fun panes(text: String) = rule.onAllNodesWithTextCount(text)

    private fun idOf(key: String): Int =
        rule
            .onAllNodes(hasText("$key:", substring = true))
            .fetchSemanticsNodes()
            .single()
            .config[SemanticsProperties.Text]
            .single()
            .text
            .substringAfter(':')
            .toInt()
}

private fun ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int = onAllNodes(hasText(text)).fetchSemanticsNodes().size
