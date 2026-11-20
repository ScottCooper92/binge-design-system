package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.template.TvHubRowFailure
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
 * The shell's arrival reaches whichever node the hub aims its entry at: the hero, but also a restored row, the first row of a hub
 * with no hero, and a failed first row's retry. Each is observed by what OK does on the focused node, with no key pressed first,
 * since the arrival (not a key) is what must have moved focus there.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingHubShellArrivalTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run (#2350, #2367). */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private var retries = 0
    private val shown = mutableStateOf(false)
    private val arrival = TvShellArrival()

    @Test
    fun `an armed arrival reaches the hero when the hero is the entry`() {
        setHub(rows = ROWS, heroes = HEROES)
        arrival.arm()
        mount()
        press(Key.DirectionCenter)
        assertEquals(listOf(HERO_FIRST), opened)
    }

    @Test
    fun `an armed arrival reaches the restored row when the hub comes back on one`() {
        setHub(rows = ROWS, heroes = HEROES, restoredRow = SECOND_KEY)
        arrival.arm()
        mount()
        press(Key.DirectionCenter)
        assertEquals(listOf(SECOND_FIRST), opened)
    }

    @Test
    fun `an armed arrival reaches the first row of a hub with no hero`() {
        setHub(rows = ROWS)
        arrival.arm()
        mount()
        press(Key.DirectionCenter)
        assertEquals(listOf(FIRST_FIRST), opened)
    }

    @Test
    fun `an armed arrival reaches a failed first row's retry`() {
        setHub(rows = listOf(failedRow(FIRST_KEY)))
        arrival.arm()
        mount()
        press(Key.DirectionCenter)
        assertEquals(1, retries)
    }

    private fun mount() {
        shown.value = true
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun failedRow(key: String) =
        TvHubRow<TvHubItem>(
            key = key,
            title = key,
            items = emptyList(),
            failure = TvHubRowFailure("Couldn't load", "Retry") { retries++ },
        )

    private fun setHub(
        rows: List<TvHubRow<TvHubItem>>,
        heroes: List<HeroItem> = emptyList(),
        restoredRow: String? = null,
    ) {
        composeTestRule.setContent {
            BingeTvTheme {
                CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                    Column {
                        Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                        if (shown.value) {
                            TvExpandingRowsHub(
                                rows = rows,
                                onItemClick = { opened += it },
                                heroItems = heroes,
                                initialFocusedKey = restoredRow,
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val FIRST_KEY = "first"
        const val SECOND_KEY = "second"
        const val FIRST_FIRST = 1
        const val SECOND_FIRST = 100
        const val HERO_FIRST = 300

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")

        val ROWS = listOf(
            TvHubRow(key = FIRST_KEY, title = "First row", items = List(3) { title(FIRST_FIRST + it) }),
            TvHubRow(key = SECOND_KEY, title = "Second row", items = List(3) { title(SECOND_FIRST + it) }),
        )

        val HEROES = List(3) {
            HeroItem(id = HERO_FIRST + it, imageUrl = null, title = "Hero $it", rating = 8f, genres = listOf("Drama"), year = "2024")
        }
    }
}
