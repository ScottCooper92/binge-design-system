package com.binge.designsystem.tv.template

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventDispatcherOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.FilterFixture
import com.binge.designsystem.tv.catalog.LABEL_CONTENT
import com.binge.designsystem.tv.catalog.LABEL_GENRES
import com.binge.designsystem.tv.catalog.SamplePaneCopy
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MAX_CATEGORIES = 12
private const val TITLES = 12

/**
 * The page's own D-pad paths, which a baseline cannot show because focus is a parameter there: arrival reaching the pane or the cell
 * last focused, the grid and the pane each remembering where they were left, and the visit's depth reported once as the page leaves.
 * The page is driven directly over a settled results source.
 *
 * `television` qualifier: a 2D focus search on Robolectric's default screen resolves nothing like a TV panel's geometry.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvFilterGridPageFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val dispatcher = NavigationEventDispatcher()
    private var shown by mutableStateOf(true)
    private var reloading by mutableStateOf(false)
    private val visits = mutableListOf<Int?>()

    @Test
    fun `a screen that mounts after a tab switch takes focus on its first category`() {
        val arrival = TvShellArrival()
        shown = false
        setScreen(arrival = arrival)

        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()

        firstCategory().assertIsFocused()
    }

    @Test
    fun `a screen that mounts with no arrival armed leaves focus where it was`() {
        shown = false
        setScreen(arrival = TvShellArrival())

        shown = true
        composeTestRule.waitForIdle()

        firstCategory().assertIsNotFocused()
    }

    /** A pop rebuilds the shell and arms an arrival: the cell the user opened wins over the first. */
    @Test
    fun `an armed arrival reaches the grid cell last focused, not the first`() {
        val arrival = TvShellArrival()
        setScreen(arrival = arrival)
        focusPane()
        press(Key.DirectionRight)
        press(Key.DirectionDown)
        text("Title 5").assertIsFocused()

        shown = false
        composeTestRule.waitForIdle()
        arrival.arm()
        shown = true
        composeTestRule.waitForIdle()

        text("Title 5").assertIsFocused()
    }

    @Test
    fun `the grid remembers its cell across a trip to the pane`() {
        setScreen()
        focusPane()
        press(Key.DirectionRight)
        text("Title 1").assertIsFocused()
        press(Key.DirectionDown)
        text("Title 5").assertIsFocused()

        press(Key.DirectionLeft)
        composeTestRule.onAllNodes(isFocused() and hasText("Title 5")).assertCountEquals(0)

        press(Key.DirectionRight)
        text("Title 5").assertIsFocused()
    }

    @Test
    fun `leaving reports the deepest cell focus reached, once, not where focus ended`() {
        setScreen()
        focusPane()
        press(Key.DirectionRight)
        press(Key.DirectionDown)
        text("Title 5").assertIsFocused()
        press(Key.DirectionUp)
        press(Key.DirectionLeft)
        assertEquals(emptyList<Int?>(), visits)

        shown = false
        composeTestRule.waitForIdle()

        assertEquals(listOf<Int?>(4), visits)
    }

    @Test
    fun `a visit that never reaches the grid reports no depth`() {
        setScreen()
        moveToCategory(LABEL_GENRES)

        shown = false
        composeTestRule.waitForIdle()

        assertEquals(listOf<Int?>(null), visits)
    }

    /** A filter change reloads the grid under the same visit: the screen is the visit, not the query. */
    @Test
    fun `a filter change that reloads the grid is the same visit, and keeps its depth`() {
        setScreen()
        focusPane()
        press(Key.DirectionRight)
        press(Key.DirectionDown)
        text("Title 5").assertIsFocused()

        reloading = true
        composeTestRule.waitForIdle()
        reloading = false
        composeTestRule.waitForIdle()
        assertEquals(emptyList<Int?>(), visits)

        shown = false
        composeTestRule.waitForIdle()

        assertEquals(listOf<Int?>(4), visits)
    }

    @Test
    fun `the pane remembers its category across a trip to the grid`() {
        setScreen()
        moveToCategory(LABEL_GENRES)

        press(Key.DirectionRight)
        composeTestRule.onAllNodes(isFocused() and hasText(LABEL_GENRES)).assertCountEquals(0)
        press(Key.DirectionLeft)

        text(LABEL_GENRES).assertIsFocused()
    }

    /** Enters the pane, which lands on its entry (the first category) however the request was aimed. */
    private fun focusPane() {
        text(LABEL_CONTENT).requestFocus()
        composeTestRule.waitForIdle()
    }

    /** Walks Down the category list to [label], as the D-pad does, since a request into the pane is routed to its entry. */
    private fun moveToCategory(label: String) {
        focusPane()
        repeat(MAX_CATEGORIES) {
            if (composeTestRule.onAllNodes(isFocused() and hasText(label)).fetchSemanticsNodes().isNotEmpty()) return
            press(Key.DirectionDown)
        }
    }

    private fun text(label: String): SemanticsNodeInteraction = composeTestRule.onNodeWithText(label)

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun firstCategory() = composeTestRule.onNodeWithTag(TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CATEGORY))

    private fun setScreen(arrival: TvShellArrival? = null) {
        val owner = object : NavigationEventDispatcherOwner {
            override val navigationEventDispatcher: NavigationEventDispatcher = dispatcher
        }
        composeTestRule.setContent {
            CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides owner, LocalTvShellArrival provides arrival) {
                BingeTvTheme {
                    val states = rememberSaveableStateHolder()
                    if (shown) {
                        states.SaveableStateProvider("filter-page") {
                            run {
                                TvFilterGridPage(
                                    categories = FilterFixture().categories(),
                                    paneCopy = SamplePaneCopy,
                                    gridCopy = TvFilterGridCopy(empty = "Nothing here", stale = "Stale", retry = "Try again"),
                                    filterCount = 0,
                                    results = TvGridResults.Items(count = TITLES, key = { it }, at = { it }),
                                    cell = { _, id -> TvPosterCell(null, "Title ${id + 1}", onClick = {}) },
                                    listKey = null,
                                    onClearAll = {},
                                    onToggle = { _, _ -> },
                                    onClearCategory = {},
                                    onRetry = {},
                                    onVisitEnded = { visits += it },
                                    loadingShell = reloading,
                                )
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }
}
