package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.HeroItem
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
 * The hub's hero plate is the node focus rests on while the hub loads, and the hero that replaces it is that same node, so a
 * user who is already on it keeps focus through the data arriving with nothing re-requesting it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingHubPlateFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    private val opened = mutableListOf<Int>()
    private val loading = mutableStateOf(true)

    @Test
    fun `focus placed on the plate is on the same node once the hero arrives`() {
        setHub()
        enterFromBar()
        val plate = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id

        loading.value = false
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocused()).assertIsFocused()
        assertEquals(plate, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(HERO_FIRST + 1), opened)
    }

    @Test
    fun `OK and the other directions do nothing on the plate, and up reaches the bar`() {
        setHub()
        enterFromBar()
        val plate = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id

        for (key in listOf(Key.DirectionCenter, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight)) {
            press(key)
            assertEquals(plate, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)
        }
        assertEquals(emptyList<Int>(), opened)

        press(Key.DirectionUp)
        composeTestRule.onNodeWithTag(BAR).assertIsFocused()
    }

    @Test
    fun `without a hero the first row is the plate and the same node once its titles arrive`() {
        setHeroless()
        enterFromBar()
        val plate = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id
        press(Key.DirectionCenter)
        assertEquals(emptyList<Int>(), opened)

        loading.value = false
        composeTestRule.waitForIdle()

        assertEquals(plate, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        assertEquals(listOf(1), opened)
    }

    @Test
    fun `without a hero up from the plate reaches the bar`() {
        setHeroless()
        enterFromBar()

        press(Key.DirectionUp)

        composeTestRule.onNodeWithTag(BAR).assertIsFocused()
    }

    private fun setHeroless() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Row(Modifier.testTag(BAR).size(BAR_SIZE).focusable()) {}
                    val rows = if (loading.value) PENDING_ROWS else ROWS
                    TvExpandingRowsHub(rows = rows, onItemClick = { opened += it })
                }
            }
        }
        composeTestRule.waitForIdle()
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
                    val isLoading = loading.value
                    TvExpandingRowsHub(
                        rows = if (isLoading) emptyList() else ROWS,
                        onItemClick = { opened += it },
                        heroItems = if (isLoading) emptyList() else HEROES,
                        loading = isLoading,
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private companion object {
        val BAR_SIZE = 20.dp
        const val HERO_FIRST = 300

        val ROWS = listOf(
            TvHubRow(
                key = "short",
                title = "Short row",
                items = List(3) {
                    TvHubItem(key = "movie_$it", posterUrl = null, title = "Title $it", rating = 7.5f, year = "2024")
                },
            ),
        )

        val PENDING_ROWS = listOf(TvHubRow<TvHubItem>(key = "short", title = "Short row", items = emptyList(), isLoading = true))

        val HEROES = List(3) {
            HeroItem(id = HERO_FIRST + it, imageUrl = null, title = "Hero $it", rating = 8f, genres = listOf("Drama"), year = "2024")
        }
    }
}
