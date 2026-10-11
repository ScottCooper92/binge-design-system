package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_MOVIES = "movies"
private const val KEY_TV = "tv"
private const val BELOW = "below"
private const val EMPTY = "empty"
private const val SEASON_COUNT = 12

/**
 * [TvTabRow] selects on focus, so entry into it cannot be left to a directional search: the tab nearest the beam
 * would be selected just by arriving. These pin the routing, the guard against re-selecting the current tab, and the
 * scroll that keeps a walked-to tab on screen.
 *
 * The fixture's geometry is the test. A full-width focusable below the row makes ↑ resolve by geometry to the
 * second tab, so the first is the one selected: the one the search would not pick.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTabRowTraversalTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val committed = mutableListOf<String>()

    @Test
    fun `entering the row lands on the selected tab rather than the nearest`() {
        setTabRowOverBlock()
        enterFromBelow()

        composeTestRule.onNodeWithText("Movies").assertIsFocused()
    }

    @Test
    fun `entering the row does not re-commit the tab already selected`() {
        setTabRowOverBlock()
        enterFromBelow()

        assertEquals("Arriving on the selected tab must commit nothing, but committed $committed", emptyList<String>(), committed)
    }

    @Test
    fun `moving to another tab selects it without a press`() {
        setTabRowOverBlock()
        composeTestRule.onNodeWithText("Movies").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Movies").assertIsFocused()

        press(Key.DirectionRight)

        composeTestRule.onNodeWithText("TV shows").assertIsFocused()
        assertEquals(listOf(KEY_TV), committed)
    }

    @Test
    fun `a selected tab beyond the fold is scrolled into view on arrival`() {
        setSeasons(selectedKey = "s$SEASON_COUNT")

        composeTestRule.onNodeWithText("Season $SEASON_COUNT").assertIsDisplayed()
    }

    @Test
    fun `walking right reaches an off-screen tab and scrolls it into view`() {
        setSeasons(selectedKey = "s1")
        composeTestRule.onNodeWithText("Season 1").requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Season 1").assertIsFocused()

        repeat(SEASON_COUNT - 1) { press(Key.DirectionRight) }

        composeTestRule.onNodeWithText("Season $SEASON_COUNT").assertIsFocused()
        composeTestRule.onNodeWithText("Season $SEASON_COUNT").assertIsDisplayed()
    }

    @Test
    fun `a row with no tabs draws nothing`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Box(Modifier.testTag(EMPTY)) { TvTabRow(tabs = emptyList(), selectedKey = null, onSelect = {}) }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(EMPTY).assertWidthIsEqualTo(0.dp).assertHeightIsEqualTo(0.dp)
    }

    /** Proves focus started below the row, so the ↑ cannot fire from some unrelated origin and assert nothing. */
    private fun enterFromBelow() {
        composeTestRule.onNodeWithContentDescription(BELOW).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription(BELOW).assertIsFocused()
        press(Key.DirectionUp)
    }

    /** The padding gives the search a direction to resolve by, rather than overlap. */
    private fun setTabRowOverBlock() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    TvTabRow(
                        tabs = listOf(TvTabUi(KEY_MOVIES, "Movies"), TvTabUi(KEY_TV, "TV shows")),
                        selectedKey = KEY_MOVIES,
                        onSelect = { committed += it },
                    )
                    Box(
                        modifier =
                            Modifier
                                .padding(top = 48.dp)
                                .fillMaxWidth()
                                .height(96.dp)
                                .semantics { contentDescription = BELOW }
                                .focusable(),
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    /** A dozen tabs in a band too narrow for them, so the later ones are laid out past its edge. */
    private fun setSeasons(selectedKey: String) {
        val tabs = (1..SEASON_COUNT).map { TvTabUi("s$it", "Season $it") }
        composeTestRule.setContent {
            BingeTvTheme {
                Box(modifier = Modifier.width(400.dp)) {
                    TvTabRow(tabs = tabs, selectedKey = selectedKey, onSelect = {})
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
