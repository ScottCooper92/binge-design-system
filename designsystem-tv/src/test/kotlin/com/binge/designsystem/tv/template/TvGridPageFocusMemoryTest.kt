package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.testing.settle
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val COLUMNS = 5

// The remembered cell for the seeded tests: off the first row, so restoring it is distinguishable from
// the pane's at-rest fallback (the first cell).
private const val CELL = 7

// The band's stand-in control: pinned to the trailing edge so a geometric drop lands on the last column.
private const val BAND_CONTROL = "band control"

private val BAND_CONTROL_SIZE = 48.dp

/**
 * **The grid pane's focus memory survives every load-state change and resets only on a query change
 * (#1485/#1482/#1443).**
 *
 * The memory used to be cleared on any non-`Items` state — the wrong signal from both directions: a pop's
 * cached `Loading` pass-through wiped the cell the user was returning to, while an `Items` → `Items`
 * re-query kept a cell from the previous result set. Now the memory lives in `rememberSaveable` keyed on
 * the caller's `queryKey`, and the focused cell is *derived* from memory + results, so a non-`Items` state
 * simply yields nothing without forgetting anything.
 *
 * The backdrop copy is the observable: cards carry their title as a content description only, so
 * `onNodeWithText` matches the backdrop's title alone.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvGridPageFocusMemoryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /**
     * A pop replays cached paging as a brief `Loading` before the same items land. The backdrop must come
     * back describing the remembered cell — before the fix the pass-through cleared the memory and the
     * backdrop fell back to the first item of the whole result set.
     */
    @Test
    fun `a loading pass-through keeps the remembered cell`() {
        var results by mutableStateOf<TvGridResults<GridItem>>(gridItems(CELLS))
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(results = results, initialFocused = TvGridFocus(CELL, CELLS[CELL]))
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        composeTestRule.onNodeWithText(CELLS[CELL].title).assertExists("Precondition: the backdrop shows the seeded cell")

        results = TvGridResults.Loading
        composeTestRule.waitForIdle()
        results = gridItems(CELLS)
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithText(CELLS[CELL].title).assertExists(
            "The backdrop must describe the remembered cell after a Loading pass-through",
        )
        composeTestRule.onNodeWithText(CELLS[0].title).assertDoesNotExist()
    }

    /**
     * The other half of the same wrong signal: an actual query change must reset the memory (#1482's
     * back-to-the-top). The re-query passes through `Loading` and resolves to a result set holding the
     * *same items* — the case only the query key can tell apart from the pop pass-through above, since the
     * id check re-validates against identical ids. Focus is placed on a real cell first, so the reset is
     * distinguishable from the memory never having been written. (While a cell genuinely holds focus the
     * memory rightly follows it — which is why the change and the `Loading` swap arrive together here,
     * exactly as a live filter change delivers them.)
     */
    @Test
    fun `a query change resets the remembered cell`() {
        var queryKey by mutableStateOf("comedy")
        var results by mutableStateOf<TvGridResults<GridItem>>(gridItems(CELLS))
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(results = results, queryKey = queryKey)
            }
        }
        focusSecondCellByWalking()
        composeTestRule.onNodeWithText(CELLS[1].title).assertExists("Precondition: the backdrop follows real focus")

        queryKey = "drama"
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()
        results = gridItems(CELLS)
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithText(CELLS[0].title).assertExists(
            "A query change must reset the memory: the backdrop falls back to the first cell",
        )
        composeTestRule.onNodeWithText(CELLS[1].title).assertDoesNotExist()
    }

    /**
     * The scroll half of the same transition. `LazyGridState` retains `firstVisibleItemIndex` across a dataset
     * swap, so before the fix a filter applied while scrolled down composed the new set at the old offset — the
     * user's "I applied a filter and landed on row 3". `rememberSaveable(queryKey)` recreates the state on a
     * real query change, so the new set starts at the top.
     *
     * `assertIsDisplayed`, not `assertExists`, is the discriminator: a lazy grid keeps a buffer of items
     * composed past the viewport, so a scrolled-past cell can still satisfy `assertExists`. Only "displayed"
     * separates *laid out* from *on screen*. The negative precondition is load-bearing, not boilerplate: if the
     * grid never scrolled (every row fit), both assertions would pass while testing nothing — asserting the
     * first cell is *not* displayed before the flip is what makes that case fail loudly. [SCROLL_CELLS] is six
     * rows so it scrolls well past the viewport at this class's television config.
     *
     * A semantics `performScrollToIndex` rather than a D-pad walk on purpose: the claim under test is that the
     * *state object* is replaced on a query change, not how focus routes — so the case does not ride the
     * anchor-keeper coroutine that a focus-driven scroll would.
     */
    @Test
    fun `a query change resets the grid scroll`() {
        var queryKey by mutableStateOf("comedy")
        var results by mutableStateOf<TvGridResults<GridItem>>(gridItems(SCROLL_CELLS))
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(results = results, queryKey = queryKey)
            }
        }
        // Flush the anchor keeper's single mount scroll-to-anchor first. It fires once after a coalesce delay,
        // and with no cell ever focused its anchor is row 0 — so left until after the scroll below it would land
        // during the next `settle` and undo it. Its `LaunchedEffect` key is the focused index, which never
        // changes again here, so once flushed it stays quiet for the rest of the case.
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNode(hasScrollAction()).performScrollToIndex(SCROLL_CELLS.lastIndex)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsNotDisplayed()

        queryKey = "drama"
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()
        results = gridItems(SCROLL_CELLS)
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsDisplayed()
    }

    /**
     * **Entry still lands on the first cell when a pop restores a scroll offset but invalidates the focus
     * memory (#1732).** #1730 fixed the case where `rememberedIndex` is null and the grid is at the top; this
     * is the residual one where it is null *and* scrolled.
     *
     * The seed writes both halves the way a pop's `rememberSaveable` restore does — a deep remembered cell
     * gives the memory an id and `gridState` a scrolled offset. The results then come back with **different
     * ids at the same query key**: the memory survives (the key did not change) but fails its id check, so
     * the derived focus is null while the scroll persists. Without the reset, cell 0 is not composed at that
     * offset, its entry requester throws, and entry falls through to a geometric pick — which, from the
     * trailing-edge band control, lands on the last column rather than the first cell.
     *
     * `assertIsNotDisplayed` before the flip is the load-bearing precondition, as in
     * [a query change resets the grid scroll]: if the grid never scrolled, cell 0 is composed anyway and the
     * case would pass while testing nothing.
     */
    @Test
    fun `entry after a pop that keeps the scroll but drops the memory lands on the first cell`() {
        var results by mutableStateOf<TvGridResults<GridItem>>(gridItems(SCROLL_CELLS))
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(
                    results = results,
                    initialFocused = TvGridFocus(DEEP_CELL, SCROLL_CELLS[DEEP_CELL]),
                    queryKey = "comedy",
                    band = { TrailingBandControl() },
                )
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsNotDisplayed()

        // Same query key, different ids: the memory fails its id check and derives null, but gridState keeps
        // its offset — the #1732 combination. Loading between, as a pop's cached pass-through delivers it.
        results = TvGridResults.Loading
        composeTestRule.waitForIdle()
        results = gridItems(SCROLL_CELLS_B)
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(BAND_CONTROL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS_B[0].title).assertIsFocused()
        // The scroll contract's stale-pop case (#2520): retiring the memory must reset the scroll to the
        // top too, not just fall back to cell 0's focus while leaving the grid scrolled past it.
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS_B[0].title).assertIsDisplayed()
    }

    /**
     * **A paged deep restore keeps its scroll while the page is still in flight (#1732 review).**
     *
     * The case that forces the reset above to be gated on *provable* staleness rather than on null focus.
     * the grid template's focus memory yields null for two different situations and only one is a retired memory: the
     * paged callers pass `at = { items[index] }` over `LazyPagingItems`, which is null for a cell whose page has
     * **not landed yet** (`get`, not `peek`, because reading is what triggers the fetch). Resetting there would
     * yank a paged pop-restore to the top before paging could reach the remembered cell — #1485's deep-scroll
     * restore, on exactly the drill-down grids that carry an `entryFocusRequester`.
     *
     * The seed puts the memory and the grid deep with that cell loaded; the flip then drops it back to a
     * loaded prefix, which is a page in flight under a restored deep offset. A static `List` cannot express
     * this — `at()` is never null — which is why the id-mismatch case above cannot cover it.
     */
    @Test
    fun `a paged deep restore keeps its scroll while the page is in flight`() {
        var pageInFlight by mutableStateOf(false)
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(
                    results = pagedResults(deepLoaded = !pageInFlight),
                    initialFocused = TvGridFocus(DEEP_CELL, PAGED_CELLS[DEEP_CELL]),
                    queryKey = "comedy",
                )
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        composeTestRule.onNodeWithContentDescription(PAGED_CELLS[0].title).assertIsNotDisplayed()

        pageInFlight = true
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(PAGED_CELLS[0].title).assertIsNotDisplayed()
    }

    /**
     * The same guard from the other side: an `Items` swap with **no** remembered cell at all must not move the
     * scroll either. Nothing is ever focused, so `rememberAnchorKeeper`'s key (`focused?.index`) stays null and
     * it only fires its single mount scroll, flushed by the first `settle` — leaving the pane's own reset as
     * the only thing that could move the grid. Each swap hands over a **new** `Items` instance, as
     * `gridResults` builds one per recomposition for a live paged source, which is what made the
     * original `results`-keyed effect re-fire every frame.
     */
    @Test
    fun `an items swap with no remembered cell leaves the scroll alone`() {
        var results by mutableStateOf<TvGridResults<GridItem>>(pagedResults(deepLoaded = true))
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(results = results, queryKey = "comedy")
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNode(hasScrollAction()).performScrollToIndex(PAGED_CELLS.lastIndex)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(PAGED_CELLS[0].title).assertIsNotDisplayed()

        results = pagedResults(deepLoaded = true)
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(PAGED_CELLS[0].title).assertIsNotDisplayed()
    }

    /**
     * The push/pop round trip itself, through the same mechanism the nav entry uses: a `SaveableStateHolder`
     * keeps the disposed pane's saveable state, and recomposing it must bring the remembered cell back —
     * the memory is written by real focus, exactly as a user browsing before opening a detail writes it.
     */
    @Test
    fun `a disposal and recomposition restores the remembered cell`() {
        var showPane by mutableStateOf(true)
        composeTestRule.setContent {
            BingeTvTheme {
                val holder = rememberSaveableStateHolder()
                if (showPane) {
                    holder.SaveableStateProvider("pane") { Pane(results = gridItems(CELLS)) }
                } else {
                    Box(modifier = Modifier.fillMaxSize().focusable())
                }
            }
        }
        focusSecondCellByWalking()
        composeTestRule.onNodeWithText(CELLS[1].title).assertExists("Precondition: the backdrop follows real focus")

        showPane = false
        composeTestRule.waitForIdle()
        showPane = true
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithText(CELLS[1].title).assertExists(
            "Recomposing the pane must restore the remembered cell from saveable state",
        )
    }

    /**
     * A pop rebuilds the shell, and since #4320 a rebuilt shell arms an arrival that the pane claims. The cell the user opened must
     * still win over the first (the class of bug #4346 was on Companions): focus, not only the backdrop, is on the remembered cell.
     */
    @Test
    fun `a disposal and recomposition lands focus on the remembered cell even though the pop arms the shell's arrival`() {
        var showPane by mutableStateOf(true)
        val arrival = TvShellArrival()
        composeTestRule.setContent {
            BingeTvTheme {
                val holder = rememberSaveableStateHolder()
                CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                    if (showPane) {
                        holder.SaveableStateProvider("pane") { Pane(results = gridItems(CELLS)) }
                    } else {
                        Box(modifier = Modifier.fillMaxSize().focusable())
                    }
                }
            }
        }
        focusSecondCellByWalking()
        composeTestRule.onNodeWithContentDescription(CELLS[1].title).assertIsFocused()

        showPane = false
        composeTestRule.waitForIdle()
        arrival.arm()
        showPane = true
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(CELLS[1].title).assertIsFocused()
    }

    /**
     * **A first entry lands on the first cell, not on whatever the beam is pointing at (#1727).**
     *
     * The band's control sits at the row's **trailing** edge, which is what makes this discriminating: a
     * geometric ↓ from there lands on the last column, so the assertion fails on the old behaviour rather
     * than passing by coincidence the way a leading-edge control would. It is the library tab row's shape —
     * the "TV" tab is offset right of column one, and ↓ from it dropped into the middle of the grid.
     *
     * Nothing is focused in the grid beforehand, so `rememberedIndex` is null: this is precisely the case
     * where the entry requester used to be attached to no cell at all and `onEnter`'s `runCatching` fell
     * through to the directional search.
     */
    @Test
    fun `entry from the band lands on the first cell`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(results = gridItems(CELLS), band = { TrailingBandControl() })
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        composeTestRule.onNodeWithContentDescription(BAND_CONTROL).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(CELLS[0].title).assertIsFocused()
    }

    /**
     * **The scroll contract's fresh-push case (#2520): a grid with no memory rests at the top.** [SCROLL_CELLS]
     * rather than [CELLS] so the assertion is discriminating — every cell of the smaller set fits the
     * viewport, where "not scrolled" would pass trivially.
     */
    @Test
    fun `a fresh grid with no memory enters on the first cell with the grid at rest`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Pane(results = gridItems(SCROLL_CELLS), band = { TrailingBandControl() })
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        composeTestRule.onNodeWithContentDescription(BAND_CONTROL).requestFocus()
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsFocused()
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS.last().title).assertIsNotDisplayed()
    }

    /**
     * **The scroll contract's pop case (#2520): a saved-state restore keeps the remembered cell's scroll rest,
     * not the top.** `StateRestorationTester` is the closest JVM analogue to the nav teardown a real pop causes
     * (see `GalleryGridRestoreTest`'s own caveat on the same harness) — a stronger round trip than the
     * `SaveableStateHolder` toggle the other memory tests in this file use, since it exercises the actual
     * `rememberSaveable` Bundle path rather than only composition disposal.
     *
     * The remembered cell is seeded deep, so the grid starts scrolled; after the restore the grid must still be
     * scrolled to that cell's anchor, and the arrival hop from the band must land on it with **no further
     * scroll** — the hop only requests focus, and the rest was already restored.
     */
    @Test
    fun `a saved-state restore keeps the remembered cell's scroll rest, not the top`() {
        val restorer = StateRestorationTester(composeTestRule)
        restorer.setContent {
            BingeTvTheme {
                Pane(
                    results = gridItems(SCROLL_CELLS),
                    initialFocused = TvGridFocus(DEEP_CELL, SCROLL_CELLS[DEEP_CELL]),
                    queryKey = "comedy",
                    band = { TrailingBandControl() },
                )
            }
        }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsNotDisplayed()

        restorer.emulateSavedInstanceStateRestore()
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsNotDisplayed()
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[DEEP_CELL].title).assertIsDisplayed()

        composeTestRule.onNodeWithContentDescription(BAND_CONTROL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionDown) }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)

        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[DEEP_CELL].title).assertIsFocused()
        composeTestRule.onNodeWithContentDescription(SCROLL_CELLS[0].title).assertIsNotDisplayed()
    }

    /** A focusable pinned to the band's trailing edge — the tab row's offset, reduced to one node. */
    @Composable
    private fun TrailingBandControl() {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(BAND_CONTROL_SIZE)
                    .semantics { contentDescription = BAND_CONTROL }
                    .focusable(),
            )
        }
    }

    @Composable
    private fun Pane(
        results: TvGridResults<GridItem>,
        initialFocused: TvGridFocus<GridItem>? = null,
        queryKey: Any? = null,
        band: (@Composable () -> Unit)? = null,
    ) {
        TestGridPage(
            heading = "Discover",
            results = results,
            onItemClick = {},
            onRetry = {},
            emptyMessage = "Nothing matches",
            columns = COLUMNS,
            initialFocused = initialFocused,
            queryKey = queryKey,
            band = band,
        )
    }

    /**
     * Puts real focus on cell 1 by **entering the grid and stepping right**, not by requesting that cell.
     *
     * Since #1727 the entry group's `onEnter` always resolves — to the remembered cell, or the first one — so
     * a `requestFocus()` aimed at any other cell is an entry into the group and gets redirected to the entry
     * cell. Production never targets a cell directly (the only per-cell requester is the entry one), so that
     * redirect is the point rather than a defect; it just means a test writing the memory has to walk there
     * like a user does.
     */
    private fun focusSecondCellByWalking() {
        composeTestRule.onNodeWithContentDescription(CELLS[0].title).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().performKeyInput { pressKey(Key.DirectionRight) }
        composeTestRule.settle(SETTLE_PAST_FOCUS_DEBOUNCE_MILLIS)
    }
}

// Distinct titles, because every finder here is by name; two rows' worth so the seeded cell is off row 0.
private val CELLS = List(COLUMNS * 3) { index ->
    GridItem(
        id = 2000 + index,
        title = "Poster $index",
    )
}

// Six rows, so the grid scrolls well past the viewport at the class's television config — its length is
// load-bearing for `a query change resets the grid scroll`, so it is named for that rather than lengthening
// the shared CELLS.
private val SCROLL_CELLS = List(COLUMNS * 6) { index ->
    GridItem(
        id = 5000 + index,
        title = "Scroll poster $index",
    )
}

// Off the last row, so seeding it as the remembered cell scrolls the grid well past the first cell.
private const val DEEP_CELL = COLUMNS * 5

// How many of PAGED_CELLS have "landed" — the rest read as a page still in flight.
private const val PAGED_LOADED_PREFIX = COLUMNS * 2

private val PAGED_CELLS = List(COLUMNS * 6) { index ->
    GridItem(
        id = 7000 + index,
        title = "Paged poster $index",
    )
}

/**
 * A paged source's shape: `at` yields null past [PAGED_LOADED_PREFIX], as `LazyPagingItems[index]` does for a
 * cell whose page has not arrived; [deepLoaded] holds the whole set, standing for that page having landed. A
 * fresh instance per call, matching `gridResults`, which rebuilds `Items` (new lambdas) on every
 * recomposition.
 */
private fun pagedResults(deepLoaded: Boolean): TvGridResults<GridItem> =
    TvGridResults.Items(
        count = PAGED_CELLS.size,
        key = { index -> PAGED_CELLS[index].id },
        at = { index -> PAGED_CELLS.getOrNull(index).takeIf { deepLoaded || index < PAGED_LOADED_PREFIX } },
    )

// A same-size set with disjoint ids and titles, for the pop that returns different results at the same query
// key — so the surviving memory fails its id check and every finder tells the two sets apart.
private val SCROLL_CELLS_B = List(COLUMNS * 6) { index ->
    GridItem(
        id = 6000 + index,
        title = "Swapped poster $index",
    )
}
