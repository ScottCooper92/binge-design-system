package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
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
private const val MESSAGE = "Check your connection and try again."
private const val RETRY = "Try again"

/**
 * A row whose titles could not be read keeps its slot in the expanding hub and says why, with its retry as the row's one focus
 * stop: entry lands on it when it is first, ↓ and ↑ stop on it like any row, OK retries, and when the retry brings the titles in
 * focus moves onto the row that replaced it rather than being lost.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingRowsHubFailedRowTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private var retries = 0

    @Test
    fun `a failed first row takes entry on its retry, and OK retries`() {
        setHub(listOf(failed("first"), loaded("below", BELOW_FIRST)))
        composeTestRule.onNodeWithText(MESSAGE).assertExists()

        enterFromBar()
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(1, retries)
    }

    @Test
    fun `down and up stop on a failed row's retry between loaded rows`() {
        setHub(listOf(loaded("above", ABOVE_FIRST), failed("middle"), loaded("below", BELOW_FIRST)))

        enterFromBar()
        press(Key.DirectionDown)
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        press(Key.DirectionDown)
        press(Key.DirectionCenter)
        assertEquals(listOf(BELOW_FIRST), opened)

        press(Key.DirectionUp)
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
    }

    @Test
    fun `a failure on a row that has titles is ignored`() {
        setHub(listOf(failed("loaded").copy(items = listOf(title(ABOVE_FIRST)))))

        composeTestRule.onNodeWithText(MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `when the retry brings the titles in, focus moves onto the row that replaced it`() {
        var rows by mutableStateOf(listOf(failed("first"), loaded("below", BELOW_FIRST)))
        setHub(rows = { rows })

        enterFromBar()
        composeTestRule.onNodeWithText(RETRY).assertIsFocused()
        composeTestRule.runOnIdle { rows = listOf(loaded("first", ABOVE_FIRST), loaded("below", BELOW_FIRST)) }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(RETRY).assertDoesNotExist()
        press(Key.DirectionCenter)
        assertEquals(listOf(ABOVE_FIRST), opened)
    }

    private fun failed(key: String) =
        TvHubRow<TvHubItem>(key = key, title = key, items = emptyList(), failure = TvHubRowFailure(MESSAGE, RETRY) { retries++ })

    private fun loaded(key: String, first: Int) = TvHubRow(key = key, title = key, items = List(ROW_COUNT) { title(first + it) })

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun enterFromBar() {
        composeTestRule.onNodeWithTag(BAR).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionDown)
    }

    private fun setHub(rows: List<TvHubRow<TvHubItem>>) = setHub(rows = { rows })

    private fun setHub(rows: () -> List<TvHubRow<TvHubItem>>) {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                    TvExpandingRowsHub(rows = rows(), onItemClick = { opened += it })
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val ROW_COUNT = 3
        const val ABOVE_FIRST = 1
        const val BELOW_FIRST = 100

        fun title(id: Int) = TvHubItem(key = "movie_$id", posterUrl = null, title = "Title $id", rating = 7.5f, year = "2024")
    }
}
