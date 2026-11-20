package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
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
 * Entry follows the head of the hub: a row that arrives above the one startup focus took (a "Continue watching" answering late) takes
 * focus, unless the user has pressed a key since. A title is observed by what OK opens, as the slot's own text is an image under test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingHubHeadFollowTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private val rows = mutableStateOf(listOf(row("below", BELOW_FIRST), row("last", LAST_FIRST)))

    @Test
    fun `a row arriving above the one startup focus took takes focus when no key has been pressed`() {
        setHub()
        enterFromBar()
        rows.value = listOf(row("above", ABOVE_FIRST)) + rows.value
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        assertEquals(listOf(ABOVE_FIRST), opened)
    }

    @Test
    fun `a key pressed first keeps focus where the user went`() {
        setHub()
        enterFromBar()
        press(Key.DirectionRight)
        rows.value = listOf(row("above", ABOVE_FIRST)) + rows.value
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        assertEquals(listOf(BELOW_FIRST + 1), opened)
    }

    @Test
    fun `a hub with its head already in place does not move focus when a row arrives below`() {
        setHub()
        enterFromBar()
        rows.value = rows.value + row("after", AFTER_FIRST)
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        assertEquals(listOf(BELOW_FIRST), opened)
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

    private fun setHub() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                    TvExpandingRowsHub(rows = rows.value, onItemClick = { opened += it })
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val ABOVE_FIRST = 1
        const val BELOW_FIRST = 100
        const val LAST_FIRST = 200
        const val AFTER_FIRST = 300
        const val TITLES_PER_ROW = 3

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")

        fun row(key: String, first: Int) = TvHubRow(key = key, title = key, items = List(TITLES_PER_ROW) { title(first + it) })
    }
}
