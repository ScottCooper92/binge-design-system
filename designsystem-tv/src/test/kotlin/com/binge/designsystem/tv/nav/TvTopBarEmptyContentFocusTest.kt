package com.binge.designsystem.tv.nav

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.TvShellArrivalNone
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A destination that composes nothing focusable, under the top bar (#3999). Under the old left rail the content pane's sink and
 * the platform's recovery search were measured alternating focus every other frame. The bar has no content-pane sink, and its
 * handoff offers focus into a group with nothing to take it, which fails without moving focus. Each test steps the clock frame
 * by frame for longer than the handoff's 8 s budget and records the focused node per frame: focus moves at most once, onto the
 * bar, and then stays wherever it is put.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTopBarEmptyContentFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    private val focusableContent = mutableStateOf(true)
    private val depth = mutableIntStateOf(1)

    @Test
    fun `a cold start into an empty destination moves focus nowhere`() {
        focusableContent.value = false
        composeTestRule.mainClock.autoAdvance = false
        setBar()

        assertEquals(listOf("none"), runs(trace(FRAMES)))
    }

    @Test
    fun `a drill-down into an empty destination parks focus on the selected tab once, and it stays`() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        focusableContent.value = false
        depth.intValue = 2

        assertEquals(listOf(CONTENT, MOVIES), runs(trace(FRAMES)))
    }

    @Test
    fun `a key press on the bar over an empty destination is not undone by the handoff`() {
        setBar()
        composeTestRule.mainClock.autoAdvance = false
        focusableContent.value = false
        depth.intValue = 2
        trace(SETTLE_FRAMES)
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(Key.DirectionRight) }

        assertEquals(listOf(SHOWS), runs(trace(FRAMES)))
    }

    /** The focused node frame by frame, collapsed to the run of nodes it passed through. */
    private fun runs(trace: List<String>): List<String> =
        trace.fold(emptyList()) { acc, node ->
            if (acc.lastOrNull() ==
                node
            ) {
                acc
            } else {
                acc + node
            }
        }

    private fun trace(frames: Int): List<String> =
        List(frames) {
            composeTestRule.mainClock.advanceTimeByFrame()
            focused()
        }

    private fun focused(): String {
        val nodes = composeTestRule.onAllNodes(isFocused(), useUnmergedTree = true).fetchSemanticsNodes()
        return nodes
            .joinToString("+") { n ->
                n.config.getOrNull(SemanticsProperties.ContentDescription)?.joinToString()
                    ?: n.config.getOrNull(SemanticsProperties.TestTag)
                    ?: "node${n.id}"
            }.ifEmpty { "none" }
    }

    private fun setBar() {
        composeTestRule.setContent {
            BingeTvTheme {
                val content = remember { FocusRequester() }
                TvTopBarScaffold(
                    slots = TvTopBarSlots(
                        header = TvNavItem("account", "Account", Icons.Filled.Person),
                        items = listOf(
                            TvNavItem("search", "Search", Icons.Filled.Search),
                            TvNavItem("movies", MOVIES, Icons.Filled.Movie),
                            TvNavItem("tv", SHOWS, Icons.Filled.Tv),
                        ),
                        footer = TvNavItem("settings", "Settings", Icons.Filled.Settings),
                    ),
                    selectedKey = "movies",
                    homeKey = "movies",
                    onSelect = {},
                    contentDepth = depth.intValue,
                    onGoHome = {},
                    overlayOpen = false,
                    contentFocusRequester = content,
                    iconOnlyKeys = setOf(KEY_SEARCH),
                ) {
                    if (!focusableContent.value) TvShellArrivalNone()
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag(CONTENT)
                            .shellArrivalTarget(enabled = focusableContent.value)
                            .then(if (focusableContent.value) Modifier.focusable() else Modifier),
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        /** Ten seconds of frames, past the handoff's 8 s budget. */
        const val FRAMES = 600
        const val SETTLE_FRAMES = 30
        const val CONTENT = "content"
        const val MOVIES = "Movies"
        const val SHOWS = "TV shows"
    }
}
