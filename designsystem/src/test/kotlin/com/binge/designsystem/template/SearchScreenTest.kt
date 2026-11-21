package com.binge.designsystem.template

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Density
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.component.LocalPagerScrimProbe
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SHORT_DRAG_START = 300f

/** How long a header gets to follow the page it settled on, far longer than the frame it takes. */
private const val SETTLE_MILLIS = 3_000L
private const val SHORT_DRAG_END = 700f

private fun results(count: Int) =
    List(count) { MediaResult("k$it", MediaResultKind.Poster, null, "Result $it", "Movie · 2020", rating = 7f, meta = "Drama") }

private val types =
    SearchTypes(
        listOf(SearchTypeChip("all", "All"), SearchTypeChip("movies", "Movies"), SearchTypeChip("people", "People")),
        selectedKey = "all",
    )

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun callbacks(
        onLoadMore: () -> Unit = {},
        onPickRecent: (String) -> Unit = {},
        onOpenResult: (String) -> Unit = {},
        onType: (String) -> Unit = {},
    ) = SearchCallbacks(
        onQueryChange = {},
        onClear = {},
        onOpenResult = onOpenResult,
        onLoadMore = onLoadMore,
        onRetry = {},
        onType = onType,
        onPickRecent = onPickRecent,
    )

    private fun show(
        state: SearchState,
        callbacks: SearchCallbacks = callbacks(),
        fontScale: Float = 1f,
        failedContent: (@Composable (Modifier) -> Unit)? = null,
        idleDefaults: Boolean = false,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    if (idleDefaults) {
                        SearchScreen(title = "Search", text = "", state = state, callbacks = callbacks, placeholder = "Search here")
                    } else {
                        SearchScreen(
                            title = "Search",
                            text = "past",
                            state = state,
                            callbacks = callbacks,
                            placeholder = "Search here",
                            idleTitle = "Idle title",
                            idleMessage = "Idle message",
                            failedContent = failedContent,
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `the near-end rule fires within three of the end`() {
        assertTrue(isNearEnd(lastVisibleIndex = 7, totalCount = 10, ahead = SEARCH_LOAD_MORE_AHEAD))
        assertFalse(isNearEnd(lastVisibleIndex = 6, totalCount = 10, ahead = SEARCH_LOAD_MORE_AHEAD))
    }

    @Test
    fun `type chips show in Ready and NoResults only`() {
        show(SearchState.Ready(results(2), loaded = 2, hasMore = false, types = types))
        rule.onNodeWithText("Movies").assertIsDisplayed()
    }

    @Test
    fun `idle shows no type chips and does show a recent that can be picked`() {
        var picked: String? = null
        show(SearchState.Idle(listOf("severance")), callbacks(onPickRecent = { picked = it }))
        rule.onNodeWithText("Movies").assertDoesNotExist()
        rule.onNodeWithText("severance").performClick()
        assertEquals("severance", picked)
    }

    @Test
    fun `idle with nothing to offer shows the plate, and extras keep it away`() {
        show(SearchState.Idle(emptyList()))
        rule.onNodeWithText("Idle title").assertIsDisplayed()
    }

    @Test
    fun `a settled empty type shows its plate`() {
        show(SearchState.Ready(emptyList(), loaded = 0, hasMore = false, emptyTitle = "No people", emptyMessage = "None here"))
        rule.onNodeWithText("No people").assertIsDisplayed()
    }

    @Test
    fun `an empty type that has more still asks for the next page`() {
        var asked = 0
        show(SearchState.Ready(emptyList(), loaded = 0, hasMore = true, emptyTitle = "No people"), callbacks(onLoadMore = { asked++ }))
        rule.onNodeWithText("No people").assertDoesNotExist()
        assertTrue("expected a load-more request, got $asked", asked >= 1)
    }

    @Test
    fun `tapping a result hands back its key and a chip its key`() {
        var opened: String? = null
        var type: String? = null
        show(
            SearchState.Ready(results(3), loaded = 3, hasMore = false, types = types),
            callbacks(onOpenResult = { opened = it }, onType = { type = it }),
        )
        rule.onNodeWithText("Result 1").performClick()
        rule.onNodeWithText("People").performClick()
        assertEquals("k1", opened)
        assertEquals("people", type)
    }

    @Test
    fun `the default idle plate says what to do`() {
        show(SearchState.Idle(emptyList()), idleDefaults = true)
        rule.onNodeWithText("Type a title or a name.").assertIsDisplayed()
    }

    @Test
    fun `loading draws the first plate with its anchor and no results`() {
        show(SearchState.Loading)
        rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))).assertIsDisplayed()
    }

    @Test
    fun `a failure that cannot be retried draws no Try again`() {
        show(SearchState.Failed(ErrorKind.Network, retryable = false))
        rule.onNodeWithText("Try again").assertDoesNotExist()
    }

    private fun clipping(fontScale: Float) {
        show(SearchState.Ready(results(3), loaded = 3, hasMore = false, types = types), fontScale = fontScale)
        val checked = rule.assertNoTextClipped()
        assertTrue("only $checked text nodes were checked", checked >= 4)
    }

    @Test fun `field, chips and rows are not clipped at 1_3`() = clipping(1.3f)

    @Test fun `field, chips and rows are not clipped at 2_0`() = clipping(2.0f)

    @Test
    fun `a failed page draws its secondary action and a tap reports it`() {
        var allowed = 0
        show(SearchState.Failed(ErrorKind.Network, secondary = ScreenAction("Allow", { allowed++ })))
        rule.onNodeWithText("Try again").assertExists()
        rule.onNodeWithText("Allow").performClick()
        assertEquals(1, allowed)
    }

    @Test
    fun `a page that added nothing asks again while the screen still has more`() {
        val asked = mutableListOf<Int>()
        var state by mutableStateOf<SearchState>(SearchState.Ready(results(3), loaded = 3, hasMore = true, types = types))
        rule.setContent {
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "past",
                    state = state,
                    callbacks = callbacks(onLoadMore = { asked += asked.size }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        assertEquals(1, asked.size)
        state = SearchState.Ready(results(3), loaded = 3, hasMore = true, appending = true, types = types)
        rule.waitForIdle()
        assertEquals(1, asked.size)
        state = SearchState.Ready(results(3), loaded = 3, hasMore = true, types = types)
        rule.waitForIdle()
        assertEquals(2, asked.size)
    }

    @Test
    fun `a result keyed like the footer still renders beside it`() {
        val odd = MediaResult("search-footer", MediaResultKind.Poster, null, "Odd key", "Movie", rating = 7f)
        show(SearchState.Ready(listOf(odd) + results(2), loaded = 3, hasMore = true, appending = true, types = types))
        rule.onNodeWithText("Odd key").assertIsDisplayed()
    }

    private fun named(prefix: String, count: Int = 3) =
        List(count) { MediaResult("$prefix$it", MediaResultKind.Poster, null, "$prefix title $it", "Movie · 2020", rating = 7f) }

    private val pagedResults = mapOf("all" to named("a"), "movies" to named("m"), "people" to named("p"))

    /** The scrim fraction the screen's scaffold last drew behind its header. */
    private var scrim = 0f

    private fun showPaged(
        selectedKey: String,
        asked: MutableList<String>,
        pages: Map<String, List<MediaResult>> = pagedResults,
        bar: ScreenBar = ScreenBar.Large,
        pinned: Boolean = false,
    ) {
        rule.setContent {
            var selected by remember { mutableStateOf(selectedKey) }
            CompositionLocalProvider(LocalPagerScrimProbe provides { scrim = it }) {
                TestTheme {
                    SearchScreen(
                        title = "Search",
                        text = "past",
                        state = SearchState.Ready(
                            named("a"),
                            loaded = 3,
                            hasMore = false,
                            types = SearchTypes(types.chips, selected),
                            pages = pages,
                        ),
                        callbacks = callbacks(onType = {
                            asked += it
                            selected = it
                        }),
                        placeholder = "Search here",
                        bar = bar,
                        pinned = pinned,
                    )
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a settled swipe reports the next type's key once, and that page shows`() {
        val asked = mutableListOf<String>()
        showPaged("all", asked)
        rule.onNodeWithText("a title 0").assertIsDisplayed()

        rule.onNodeWithText("a title 0").performTouchInput { swipeLeft() }
        rule.waitForIdle()

        assertEquals(listOf("movies"), asked)
        rule.onNodeWithText("m title 0").assertIsDisplayed()
    }

    @Test
    fun `a chip tap moves to that type's page and reports its key`() {
        val asked = mutableListOf<String>()
        showPaged("all", asked)

        rule.onNodeWithText("People").performClick()
        rule.waitForIdle()

        assertEquals(listOf("people"), asked)
        rule.onNodeWithText("p title 0").assertIsDisplayed()
    }

    @Test
    fun `a type with no entry shows loading rows, not another type's results`() {
        val asked = mutableListOf<String>()
        showPaged("movies", asked, pages = pagedResults - "movies")

        rule.onNodeWithText("m title 0").assertDoesNotExist()
        rule.onNodeWithText("a title 0").assertDoesNotExist()
        rule.onNode(hasTestTag(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))).assertIsDisplayed()
    }

    @Test
    fun `one type does not page, so a swipe reports nothing`() {
        val asked = mutableListOf<String>()
        rule.setContent {
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "past",
                    state = SearchState.Ready(
                        named("a"),
                        loaded = 3,
                        hasMore = false,
                        types = SearchTypes(listOf(SearchTypeChip("all", "All")), "all"),
                        pages = mapOf("all" to named("a")),
                    ),
                    callbacks = callbacks(onType = { asked += it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("a title 0").performTouchInput { swipeLeft() }
        rule.waitForIdle()

        assertTrue(asked.isEmpty())
    }

    private fun showLongPaged(
        asked: MutableList<String>,
        bar: ScreenBar = ScreenBar.Large,
        pinned: Boolean = false,
    ) {
        val pages = mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40))
        showPaged("all", asked, pages = pages, bar = bar, pinned = pinned)
    }

    private fun assertScrolledAway(label: String) {
        rule.onNodeWithText(label).assertDoesNotExist()
    }

    @Test
    fun `a page keeps its scroll when a chip tap takes the pager two pages away and back`() {
        val asked = mutableListOf<String>()
        showLongPaged(asked)
        repeat(3) {
            rule.onRoot().performTouchInput { swipeUp() }
        }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        rule.onNodeWithText("People").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("p title 0").assertIsDisplayed()
        rule.onNodeWithText("All").performClick()
        rule.waitForIdle()

        assertScrolledAway("a title 0")
    }

    @Test
    fun `a page keeps its scroll through a moment with no entry, as while its type reloads`() {
        val asked = mutableListOf<String>()
        var pages by mutableStateOf(mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40)))
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "past",
                    state = SearchState.Ready(
                        named("a"),
                        loaded = 3,
                        hasMore = false,
                        types = SearchTypes(types.chips, selected),
                        pages = pages,
                    ),
                    callbacks = callbacks(onType = {
                        asked += it
                        selected = it
                    }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        val all = pages.getValue("all")
        pages = pages - "all"
        rule.waitForIdle()
        pages = pages + ("all" to all)
        rule.waitForIdle()

        assertScrolledAway("a title 0")
    }

    /**
     * Hosts the screen the way an app does: every `onType` makes the holder emit a new [SearchState.Ready], with new `types` for the new selection
     * and a new `pages` map of new lists with equal content, so nothing the screen keeps can hang on the identity of the state, the types or the map.
     */
    private fun showAsAnApp(emptyKeys: Set<String> = emptySet()) {
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            val pages =
                mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40)).mapValues { (key, list) ->
                    if (key in emptyKeys) emptyList() else list
                }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "past",
                    state = SearchState.Ready(
                        named("a"),
                        loaded = 3,
                        hasMore = false,
                        types = SearchTypes(types.chips, selected),
                        pages = pages,
                    ),
                    callbacks = callbacks(onType = { selected = it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
    }

    private fun scrollAllThenTapAwayAndBack(away: String) {
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        rule.onNodeWithText(away).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("All").performClick()
        rule.waitForIdle()

        assertScrolledAway("a title 0")
    }

    @Test
    fun `an app's new state on each type keeps a page's scroll through a chip tap away and back`() {
        showAsAnApp()
        scrollAllThenTapAwayAndBack("Movies")
    }

    @Test
    fun `an app's new state on each type keeps a page's scroll through a tap to an empty page and back`() {
        showAsAnApp(emptyKeys = setOf("people"))
        scrollAllThenTapAwayAndBack("People")
    }

    @Test
    fun `a page keeps its scroll when the screen shows another state between two type taps`() {
        var interlude by mutableStateOf(false)
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            val pages = mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40))
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "past",
                    state = if (interlude) {
                        SearchState.Loading
                    } else {
                        SearchState.Ready(
                            named("a"),
                            loaded = 3,
                            hasMore = false,
                            types = SearchTypes(types.chips, selected),
                            pages = pages,
                        )
                    },
                    callbacks = callbacks(onType = { selected = it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        interlude = true
        rule.waitForIdle()
        interlude = false
        rule.waitForIdle()

        assertScrolledAway("a title 0")
    }

    @Test
    fun `a page keeps its scroll through a saved state restore`() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "past",
                    state = SearchState.Ready(
                        named("a"),
                        loaded = 3,
                        hasMore = false,
                        types = SearchTypes(types.chips, "all"),
                        pages = mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40)),
                    ),
                    callbacks = callbacks(),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertScrolledAway("a title 0")
    }

    @Test
    fun `a new query opens each page at the top, not where the old query's list was scrolled`() {
        var query by mutableStateOf("past")
        var prefix by mutableStateOf("a")
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = query,
                    state = SearchState.Ready(
                        named(prefix),
                        loaded = 3,
                        hasMore = false,
                        types = SearchTypes(types.chips, selected),
                        pages = mapOf("all" to named(prefix, 40), "movies" to named("m$prefix", 40), "people" to named("p$prefix", 40)),
                    ),
                    callbacks = callbacks(onType = { selected = it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        query = "heat"
        prefix = "h"
        rule.waitForIdle()

        rule.onNodeWithText("h title 0").assertIsDisplayed()
    }

    private fun films(count: Int, keyPrefix: String = "film") =
        List(count) { MediaResult("$keyPrefix$it", MediaResultKind.Poster, null, "Film $it", "Movie · 2020", rating = 7f) }

    /**
     * The consumer's own emission, one [SearchState.Ready] per tap and nothing else: `results` is the selected type's page, so it changes on every tap,
     * the films on All and Movies share their keys, and the types after them have empty pages.
     */
    private fun showFromAConsumer(onSelect: (String) -> Unit = {}) {
        val chips =
            listOf(
                SearchTypeChip("all", "All"),
                SearchTypeChip("movies", "Movies"),
                SearchTypeChip("tv", "TV"),
                SearchTypeChip("people", "People"),
            )
        val pages = mapOf("all" to films(40), "movies" to films(40, "mfilm"), "tv" to emptyList(), "people" to emptyList())
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = "film",
                    state = SearchState.Ready(
                        results = pages.getValue(selected),
                        loaded = 40,
                        hasMore = false,
                        types = SearchTypes(chips, selected),
                        pages = pages,
                    ),
                    callbacks = callbacks(onType = {
                        selected = it
                        onSelect(it)
                    }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
    }

    /** What the reader sees: the films on All and Movies share their titles, and the page beside the selected one may still be composed off-screen. */
    private fun assertNoFilmOnScreen() {
        rule
            .onAllNodesWithText(
                "Film 1",
            ).fetchSemanticsNodes()
            .indices
            .forEach { rule.onAllNodesWithText("Film 1")[it].assertIsNotDisplayed() }
    }

    @Test
    fun `a page keeps its scroll through a chip tap away and back when the consumer's results follow the selection`() {
        showFromAConsumer()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertNoFilmOnScreen()

        rule.onNodeWithText("Movies").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("All").performClick()
        rule.waitForIdle()

        assertNoFilmOnScreen()
    }

    @Test
    fun `a page keeps its scroll through a tap to an empty type and back when the consumer's results follow the selection`() {
        showFromAConsumer()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertNoFilmOnScreen()

        rule.onNodeWithText("People").performClick()
        rule.waitForIdle()
        rule.onNodeWithText("All").performClick()
        rule.waitForIdle()

        assertNoFilmOnScreen()
    }

    @Test
    fun `typing keeps the old results where they were until the results for the new query arrive`() {
        var text by mutableStateOf("past")
        var answered by mutableStateOf("past")
        var prefix by mutableStateOf("a")
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = text,
                    state = SearchState.Ready(
                        named(prefix),
                        loaded = 3,
                        hasMore = false,
                        types = SearchTypes(types.chips, selected),
                        pages = mapOf("all" to named(prefix, 40), "movies" to named("m$prefix", 40), "people" to named("p$prefix", 40)),
                        query = answered,
                    ),
                    callbacks = callbacks(onType = { selected = it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        text = "heat"
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        answered = "heat"
        prefix = "h"
        rule.waitForIdle()
        rule.onNodeWithText("h title 0").assertIsDisplayed()
    }

    @Test
    fun `a state with no results between two states for the same query keeps each page's scroll`() {
        var showing by mutableStateOf(true)
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    // The field holds what was typed; the results answer the normalised query, as an app that trims it would.
                    text = "heat ",
                    state = if (showing) {
                        SearchState.Ready(
                            named("a"),
                            loaded = 3,
                            hasMore = false,
                            types = SearchTypes(types.chips, selected),
                            pages = mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40)),
                            query = "heat",
                        )
                    } else {
                        SearchState.Loading
                    },
                    callbacks = callbacks(onType = { selected = it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        showing = false
        rule.waitForIdle()
        showing = true
        rule.waitForIdle()

        assertScrolledAway("a title 0")
    }

    @Test
    fun `clearing the field and searching the same query again opens each page at the top`() {
        var idle by mutableStateOf(false)
        var text by mutableStateOf("heat")
        rule.setContent {
            var selected by remember { mutableStateOf("all") }
            TestTheme {
                SearchScreen(
                    title = "Search",
                    text = text,
                    state = if (idle) {
                        SearchState.Idle(recents = emptyList())
                    } else {
                        SearchState.Ready(
                            named("a"),
                            loaded = 3,
                            hasMore = false,
                            types = SearchTypes(types.chips, selected),
                            pages = mapOf("all" to named("a", 40), "movies" to named("m", 40), "people" to named("p", 40)),
                            query = "heat",
                        )
                    },
                    callbacks = callbacks(onType = { selected = it }),
                    placeholder = "Search here",
                )
            }
        }
        rule.waitForIdle()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertScrolledAway("a title 0")

        idle = true
        text = ""
        rule.waitForIdle()
        idle = false
        text = "heat"
        rule.waitForIdle()

        rule.onNodeWithText("a title 0").assertIsDisplayed()
    }

    private fun chipsBounds() = rule.onNodeWithText("Movies").getBoundsInRoot()

    /**
     * The large bar collapses as a page scrolls, and a page that arrives already scrolled (or at the top) moves nothing, so the header has to be
     * set from the page it settles on. The chip row sits under the title, so where it is tells which state the header is in.
     */
    private fun assertHeaderFollowsPages(goTo: (String, String) -> Unit) {
        val asked = mutableListOf<String>()
        showLongPaged(asked)
        val expanded = chipsBounds()
        rule.onNodeWithText("Movies").performClick()
        rule.waitForIdle()
        assertEquals(expanded, chipsBounds())

        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        val collapsed = chipsBounds()
        assertTrue("scrolling the page should collapse the header", collapsed.top < expanded.top)

        goTo("All", "right")
        rule.waitForIdle()
        assertEquals("page 1 is at the top, so the header is expanded", expanded, chipsBounds())

        goTo("Movies", "left")
        rule.waitForIdle()
        assertEquals("page 2 is scrolled, so the header is collapsed and no row sits behind an expanded one", collapsed, chipsBounds())
    }

    @Test
    fun `a page that arrives scrolled or at the top sets the header, on a swipe`() =
        assertHeaderFollowsPages { _, direction ->
            rule.onRoot().performTouchInput { if (direction == "left") swipeLeft() else swipeRight() }
        }

    @Test
    fun `a page that arrives scrolled or at the top sets the header, on a chip tap`() =
        assertHeaderFollowsPages { chip, _ -> rule.onNodeWithText(chip).performClick() }

    /**
     * The small bar's header draws a scrim while the page under it is scrolled, driven by how far the content has passed under it, and a page that
     * arrives already scrolled passes nothing, so it would show the scrim of the page before it. The scrim is the page's own colour at a high alpha,
     * so a scrolled header looks much like a bare one and what tells them apart is a row showing through; each page's header is compared with how it
     * looked when the page was left, by the region from the top edge to the chips.
     */
    private fun assertSmallHeaderFollowsPages(pinned: Boolean = false, goTo: (String, String) -> Unit) {
        showLongPaged(mutableListOf(), bar = ScreenBar.Small, pinned = pinned)
        rule.onNodeWithText("Movies").performClick()
        rule.waitForIdle()

        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        // A short drag back brings the bar back, which slides away as the page scrolls, with the page still well down.
        rule.onRoot().performTouchInput { swipeDown(startY = SHORT_DRAG_START, endY = SHORT_DRAG_END) }
        rule.waitForIdle()
        val scrolled = scrim
        assertTrue("page 2 is scrolled, so its scrim is up", scrolled > 0f)

        goTo("All", "right")
        rule.waitForIdle()
        assertEquals("page 1 is at its top, so there is no scrim", 0f, scrim, 0f)

        goTo("Movies", "left")
        // The header is set once the page has settled, a frame after the pager reports it. The clock is driven by hand, past every animation the
        // move starts, so what is read is where the scrim ends and not where it was on a frame that load made late. A header that never follows
        // the page leaves the scrim at 0 and fails here.
        rule.mainClock.autoAdvance = false
        rule.mainClock.advanceTimeBy(SETTLE_MILLIS)
        assertEquals("page 2 is scrolled, so its header draws the scrim it was left with", scrolled, scrim, 0f)
    }

    @Test
    fun `the small bar's header follows the page it settles on, on a swipe`() =
        assertSmallHeaderFollowsPages { _, direction ->
            rule.onRoot().performTouchInput { if (direction == "left") swipeLeft() else swipeRight() }
        }

    @Test
    fun `the small bar's header follows the page it settles on, on a chip tap`() =
        assertSmallHeaderFollowsPages { chip, _ -> rule.onNodeWithText(chip).performClick() }

    @Test
    fun `the pinned small bar's header follows the page it settles on, on a swipe`() =
        assertSmallHeaderFollowsPages(pinned = true) { _, direction ->
            rule.onRoot().performTouchInput { if (direction == "left") swipeLeft() else swipeRight() }
        }

    @Test
    fun `the pinned small bar's header follows the page it settles on, on a chip tap`() =
        assertSmallHeaderFollowsPages(pinned = true) { chip, _ -> rule.onNodeWithText(chip).performClick() }

    @Test
    fun `a pinned small bar keeps its title, field and chips in place while a page scrolls`() {
        showLongPaged(mutableListOf(), bar = ScreenBar.Small, pinned = true)

        fun tops() = listOf("Search", "past", "Movies").map { rule.onNodeWithText(it).getUnclippedBoundsInRoot().top }
        val before = tops()
        repeat(3) { rule.onRoot().performTouchInput { swipeUp() } }
        rule.waitForIdle()
        assertEquals(before, tops())
    }
}
