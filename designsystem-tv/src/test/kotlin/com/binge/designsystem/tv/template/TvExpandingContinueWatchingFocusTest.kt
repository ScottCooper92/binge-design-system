package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BAR = "bar"
private const val CW_TITLE = "Continue watching"
private const val WATCHLIST_TITLE = "From your watchlist"

/**
 * "Continue watching" in the expanding hub, driven with real D-pad keys: a row arriving late above the user stays out until the
 * next visit, a pending one fills in place, and focus survives the row's titles leaving or re-sorting, and the row itself leaving,
 * without reaching the top bar or the hero. A title is observed by what OK opens.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingContinueWatchingFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private val shown = mutableStateOf(true)
    private val rows = mutableStateOf(listOf(watchlist(), other()))

    @Test
    fun `a late answer while focus is on the watchlist row leaves it where it is, and the row waits for the next visit`() {
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        val before = focusedBounds()

        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1), watchlist(), other())
        composeTestRule.waitForIdle()

        assertEquals(before, focusedBounds())
        press(Key.DirectionCenter)
        // Up passes no "Continue watching" on the way to the hero: the row is not in the list this visit.
        press(Key.DirectionUp)
        press(Key.DirectionCenter)
        assertEquals(listOf(WATCHLIST_FIRST, HERO_FIRST), opened)

        revisit()
        composeTestRule.onAllNodesWithText(CW_TITLE).assertCountEquals(1)
    }

    @Test
    fun `a pending row fills in place, under the hero and above the user`() {
        rows.value = listOf(pendingContinueWatching(), watchlist(), other())
        setHub()
        enterFromBar()
        val pendingHeading = headingBounds(CW_TITLE)

        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1), watchlist(), other())
        composeTestRule.waitForIdle()

        assertEquals(pendingHeading, headingBounds(CW_TITLE))
        press(Key.DirectionDown)
        press(Key.DirectionCenter)
        assertEquals(listOf(CW_FIRST), opened)
    }

    @Test
    fun `a pending row that fills while focus is on the row below it stays, and does not move it`() {
        rows.value = listOf(pendingContinueWatching(), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        val before = focusedBounds()

        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1), watchlist(), other())
        composeTestRule.waitForIdle()

        assertEquals(before, focusedBounds())
        press(Key.DirectionUp)
        press(Key.DirectionCenter)
        assertEquals(listOf(CW_FIRST), opened)
    }

    @Test
    fun `a pending row that gives up after the user moved below it keeps its slot, so the row under focus keeps its place`() {
        rows.value = listOf(pendingContinueWatching(), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        val watchlistSlot = TvLayoutAnchors.hubRow(1)
        val before = focusedBounds()
        val slotBefore = composeTestRule.onNodeWithTag(watchlistSlot).getBoundsInRoot()

        rows.value = listOf(watchlist(), other())
        composeTestRule.waitForIdle()

        assertEquals(before, focusedBounds())
        assertEquals(slotBefore, composeTestRule.onNodeWithTag(watchlistSlot).getBoundsInRoot())
        press(Key.DirectionCenter)
        press(Key.DirectionUp)
        press(Key.DirectionCenter)
        assertEquals(listOf(WATCHLIST_FIRST, HERO_FIRST), opened)
    }

    @Test
    fun `a kept slot stays empty when its answer comes late, and fills on the next visit`() {
        rows.value = listOf(pendingContinueWatching(), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        val before = focusedBounds()

        rows.value = listOf(watchlist(), other())
        composeTestRule.waitForIdle()
        rows.value = listOf(continueWatching(CW_FIRST), watchlist(), other())
        composeTestRule.waitForIdle()

        assertEquals(before, focusedBounds())
        press(Key.DirectionUp)
        press(Key.DirectionCenter)
        assertEquals(listOf(HERO_FIRST), opened)

        revisit()
        reenterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionCenter)
        assertEquals(listOf(HERO_FIRST, CW_FIRST), opened)
    }

    @Test
    fun `back from a page where the focused card was removed, focus lands on its neighbour`() {
        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1, CW_FIRST + 2), watchlist())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionRight)

        revisit { rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 2), watchlist()) }
        reenterFromBar()
        press(Key.DirectionCenter)

        assertEquals(listOf(CW_FIRST + 2), opened)
    }

    @Test
    fun `back from a page where the last card was removed, focus lands on the one before it`() {
        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1, CW_FIRST + 2), watchlist())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)

        revisit { rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1), watchlist()) }
        reenterFromBar()
        press(Key.DirectionCenter)

        assertEquals(listOf(CW_FIRST + 1), opened)
    }

    @Test
    fun `a card removed before the focused one keeps focus on the same title`() {
        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1, CW_FIRST + 2, CW_FIRST + 3), watchlist())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)

        revisit { rows.value = listOf(continueWatching(CW_FIRST + 1, CW_FIRST + 2, CW_FIRST + 3), watchlist()) }
        reenterFromBar()
        press(Key.DirectionCenter)

        assertEquals(listOf(CW_FIRST + 2), opened)
    }

    @Test
    fun `a row that re-sorts keeps the same title focused`() {
        rows.value = listOf(continueWatching(CW_FIRST, CW_FIRST + 1, CW_FIRST + 2), watchlist())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionRight)

        rows.value = listOf(continueWatching(CW_FIRST + 2, CW_FIRST, CW_FIRST + 1), watchlist())
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)

        assertEquals(listOf(CW_FIRST + 1), opened)
    }

    @Test
    fun `back to a hub whose row emptied, focus lands on the next row`() {
        rows.value = listOf(continueWatching(CW_FIRST), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)

        revisit { rows.value = listOf(watchlist(), other()) }
        reenterFromBar()
        press(Key.DirectionCenter)

        assertEquals(listOf(WATCHLIST_FIRST), opened)
    }

    @Test
    fun `back to a hub whose row above the focused one emptied, focus lands on the same row`() {
        rows.value = listOf(continueWatching(CW_FIRST), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionRight)

        revisit { rows.value = listOf(watchlist(), other()) }
        reenterFromBar()
        press(Key.DirectionCenter)

        assertEquals(listOf(WATCHLIST_FIRST + 1), opened)
    }

    @Test
    fun `a row above the focused one that empties while a page is open leaves the focused row's title as it was, in the last row`() {
        rows.value = listOf(continueWatching(CW_FIRST), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)

        revisit { rows.value = listOf(watchlist(), other()) }
        reenterFromBar()
        press(Key.DirectionCenter)

        assertEquals(listOf(OTHER_FIRST + 2), opened)
    }

    @Test
    fun `a row that empties while it has focus hands focus to the next row, not the top bar or the hero`() {
        rows.value = listOf(continueWatching(CW_FIRST), watchlist(), other())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)

        rows.value = listOf(watchlist(), other())
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(BAR).assertIsNotFocused()
        press(Key.DirectionCenter)
        assertEquals(listOf(WATCHLIST_FIRST), opened)
    }

    /** The last row has no row below it, so focus goes to the row above, which does not move: the owed request must still fire. */
    @Test
    fun `the last row emptying while it has focus hands focus to the row above it`() {
        rows.value = listOf(watchlist(), other(), third())
        setHub()
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionCenter)
        assertEquals(listOf(THIRD_FIRST), opened)

        rows.value = listOf(watchlist(), other())
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(BAR).assertIsNotFocused()
        press(Key.DirectionCenter)
        assertEquals(listOf(THIRD_FIRST, OTHER_FIRST), opened)
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun enterFromBar() {
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
    }

    /** The return from a page opened from the hub: Back lands focus on the top bar's side of the hub, and ↓ re-enters it. */
    private fun reenterFromBar() = enterFromBar()

    /** Leaves the hub and comes back, as opening a title and pressing Back does; [whileAway] changes the rows in between. */
    private fun revisit(whileAway: () -> Unit = {}) {
        shown.value = false
        composeTestRule.waitForIdle()
        whileAway()
        shown.value = true
        composeTestRule.waitForIdle()
    }

    private fun focusedBounds(): DpRect = composeTestRule.onNode(isFocused()).getBoundsInRoot()

    private fun headingBounds(text: String): DpRect = composeTestRule.onNodeWithText(text).getBoundsInRoot()

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                    val holder = rememberSaveableStateHolder()
                    if (shown.value) {
                        holder.SaveableStateProvider("hub") {
                            TvExpandingRowsHub(rows = rows.value, onItemClick = { opened += it }, heroItems = HEROES)
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val CW_FIRST = 1
        const val WATCHLIST_FIRST = 100
        const val OTHER_FIRST = 200
        const val HERO_FIRST = 300
        const val THIRD_FIRST = 400

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")

        fun continueWatching(vararg ids: Int) =
            TvHubRow(key = "continue_watching", title = CW_TITLE, items = ids.map(::title), loops = false)

        fun pendingContinueWatching() =
            TvHubRow<TvHubItem>(
                key = "continue_watching",
                title = CW_TITLE,
                items = emptyList(),
                isLoading = true,
                loops = false,
            )

        fun watchlist() = TvHubRow(key = "from_watchlist", title = WATCHLIST_TITLE, items = List(3) { title(WATCHLIST_FIRST + it) })

        fun other() = TvHubRow(key = "other", title = "Other row", items = List(3) { title(OTHER_FIRST + it) })

        fun third() = TvHubRow(key = "third", title = "Third row", items = List(3) { title(THIRD_FIRST + it) })

        val HEROES = List(2) {
            HeroItem(id = HERO_FIRST + it, imageUrl = null, title = "Hero $it", rating = 8f, genres = listOf("Drama"), year = "2024")
        }
    }
}
