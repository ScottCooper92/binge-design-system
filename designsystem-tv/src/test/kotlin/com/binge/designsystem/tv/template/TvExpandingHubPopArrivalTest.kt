package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
 * A pop rebuilds the shell, and a rebuilt shell arms an arrival, which the hub's entry then claims. The hub is rebuilt under its
 * saved state, as the back stack does, and the card the user opened must still win over the row's first: the class of bug a companion screen once had,
 * here for the hub every Movies, TV shows and Account page is built on.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingHubPopArrivalTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private val shown = mutableStateOf(true)
    private val arrival = TvShellArrival()

    @Test
    fun `after a pop the armed arrival lands on the card opened in a later row, not the row's first`() {
        setHub()
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(SECOND_FIRST + 2), opened)

        shown.value = false
        composeTestRule.waitForIdle()
        arrival.arm()
        shown.value = true
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)

        assertEquals(listOf(SECOND_FIRST + 2, SECOND_FIRST + 2), opened)
    }

    @Test
    fun `after a pop the armed arrival lands on the card opened in the first row, not its first`() {
        setHub()
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(FIRST_FIRST + 2), opened)

        shown.value = false
        composeTestRule.waitForIdle()
        arrival.arm()
        shown.value = true
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)

        assertEquals(listOf(FIRST_FIRST + 2, FIRST_FIRST + 2), opened)
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun setHub() {
        composeTestRule.setContent {
            val holder = rememberSaveableStateHolder()
            BingeTvTheme {
                CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                    Column {
                        Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                        if (shown.value) {
                            holder.SaveableStateProvider("hub") {
                                TvExpandingRowsHub(rows = ROWS, onItemClick = { opened += it })
                            }
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val FIRST_FIRST = 1
        const val SECOND_FIRST = 100

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")

        val ROWS = listOf(
            TvHubRow(key = "first", title = "First row", items = List(4) { title(FIRST_FIRST + it) }),
            TvHubRow(key = "second", title = "Second row", items = List(4) { title(SECOND_FIRST + it) }),
        )
    }
}
