package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
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

/**
 * The expanding hub's D-pad contract, driven with real keys on the JVM: arriving focus lands on the first row (or the hero),
 * ←/→ step a row's titles and the hero's slides, a row remembers its title when focus leaves and returns, and Back to the top bar
 * re-enters the row it left. A title is observed by what OK opens, since the slot's own text is an image under test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingRowsHubFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run (#2350, #2367). */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private val visits = mutableListOf<Int?>()
    private val shown = mutableStateOf(true)

    @Test
    fun `arriving from the bar lands on the first row's first title`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionCenter)
        assertEquals(listOf(SHORT_FIRST), opened)
    }

    @Test
    fun `arriving from the bar lands on the hero when there is one, and arrows step its slides`() {
        setHub(rows = ROWS, heroes = HEROES)
        enterFromBar()
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(HERO_FIRST + 1), opened)
    }

    @Test
    fun `leaving the hub reports the deepest row focus reached, counting the hero as 0`() {
        setHub(rows = ROWS, heroes = HEROES)
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionUp)
        shown.value = false
        composeTestRule.waitForIdle()
        assertEquals(listOf<Int?>(2), visits)
    }

    @Test
    fun `without a hero the first row is 0, and a visit that focused nothing reports null`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionDown)
        shown.value = false
        composeTestRule.waitForIdle()
        assertEquals(listOf<Int?>(1), visits)

        visits.clear()
        shown.value = true
        composeTestRule.waitForIdle()
        shown.value = false
        composeTestRule.waitForIdle()
        assertEquals(listOf<Int?>(null), visits)
    }

    @Test
    fun `the hero wraps at both ends`() {
        setHub(rows = ROWS, heroes = HEROES)
        enterFromBar()
        press(Key.DirectionLeft)
        press(Key.DirectionCenter)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(HERO_FIRST + HEROES.lastIndex, HERO_FIRST), opened)
    }

    @Test
    fun `right and left step a row's titles and OK opens the one in the slot`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionLeft)
        press(Key.DirectionCenter)
        assertEquals(listOf(SHORT_FIRST + 1), opened)
    }

    @Test
    fun `a short row stops at its last title instead of wrapping`() {
        setHub(rows = ROWS)
        enterFromBar()
        repeat(SHORT_COUNT + 2) { press(Key.DirectionRight) }
        press(Key.DirectionCenter)
        assertEquals(listOf(SHORT_FIRST + SHORT_COUNT - 1), opened)
    }

    @Test
    fun `a long row whose source has ended wraps past its last title`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionDown)
        repeat(LONG_COUNT) { press(Key.DirectionRight) }
        press(Key.DirectionCenter)
        assertEquals(listOf(LONG_FIRST), opened)
    }

    @Test
    fun `a long row that sets loops false stops at its last title instead of wrapping`() {
        setHub(rows = listOf(ROWS[1].copy(loops = false)))
        enterFromBar()
        repeat(LONG_COUNT + 1) { press(Key.DirectionRight) }
        press(Key.DirectionCenter)
        assertEquals(listOf(LONG_FIRST + LONG_COUNT - 1), opened)
    }

    @Test
    fun `a row with pages left does not wrap, and one that has ended does`() {
        val ended = mutableStateOf(false)
        setHub(rows = ROWS, pages = { key -> if (key == LONG_KEY) pagesOf(LONG_COUNT, ended.value) else null })
        enterFromBar()
        press(Key.DirectionDown)
        repeat(LONG_COUNT + 1) { press(Key.DirectionRight) }
        press(Key.DirectionCenter)
        assertEquals(listOf(LONG_FIRST + LONG_COUNT - 1), opened)

        ended.value = true
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(LONG_FIRST, opened.last())
    }

    @Test
    fun `a row remembers its title when focus leaves it and comes back`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionRight)
        press(Key.DirectionDown)
        press(Key.DirectionUp)
        press(Key.DirectionCenter)
        assertEquals(listOf(SHORT_FIRST + 1), opened)
    }

    @Test
    fun `going back to the bar and down again re-enters the row that was left`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
        press(Key.DirectionCenter)
        assertEquals(listOf(LONG_FIRST + 1), opened)
    }

    @Test
    fun `a drill-down that disposes the hub and returns re-enters the row's remembered title`() {
        setHub(rows = ROWS)
        enterFromBar()
        press(Key.DirectionRight)

        shown.value = false
        composeTestRule.waitForIdle()
        shown.value = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
        press(Key.DirectionCenter)

        assertEquals(listOf(SHORT_FIRST + 1), opened)
    }

    @Test
    fun `a tab switch arrival lands on the entry row when the hub has no hero`() {
        val arrival = TvShellArrival()
        shown.value = false
        setHub(rows = ROWS, arrival = arrival)
        arrival.arm()
        shown.value = true
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        assertEquals(listOf(SHORT_FIRST), opened)
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

    private fun setHub(
        rows: List<TvHubRow<TvHubItem>>,
        heroes: List<HeroItem> = emptyList(),
        pages: @Composable (String) -> TvRowPages<TvHubItem>? = { null },
        arrival: TvShellArrival? = null,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                    Column {
                        Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                        val holder = rememberSaveableStateHolder()
                        if (shown.value) {
                            holder.SaveableStateProvider("hub") {
                                TvExpandingRowsHub(
                                    rows = rows,
                                    onItemClick = { opened += it },
                                    heroItems = heroes,
                                    rowPages = pages,
                                    onVisitEnded = { visits += it },
                                )
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun pagesOf(count: Int, ended: Boolean) =
        TvRowPages<TvHubItem>(count = count, endReached = ended, at = { index -> if (index < count) title(LONG_FIRST + index) else null })

    private companion object {
        val BAR_SIZE = 20.dp
        const val SHORT_FIRST = 1
        const val SHORT_COUNT = 3
        const val LONG_KEY = "long"
        const val LONG_FIRST = 100
        const val LONG_COUNT = 9
        const val HERO_FIRST = 300

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")

        val ROWS = listOf(
            TvHubRow(key = "short", title = "Short row", items = List(SHORT_COUNT) { title(SHORT_FIRST + it) }),
            TvHubRow(key = LONG_KEY, title = "Long row", items = List(LONG_COUNT) { title(LONG_FIRST + it) }),
        )

        val HEROES = List(3) {
            HeroItem(id = HERO_FIRST + it, imageUrl = null, title = "Hero $it", rating = 8f, genres = listOf("Drama"), year = "2024")
        }
    }
}
