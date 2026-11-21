package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * What the search grid reports as `scroll_depth`: the deepest result cell focus reached, once per visit. A visit
 * is one query's results, so a new query or the results giving way to a failure ends it, and recommendations never count.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvSearchScrollDepthTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    private var uiState by mutableStateOf(tvResultsUiState())
    private var shown by mutableStateOf(true)
    private val visits = mutableListOf<Int?>()

    @Test
    fun `leaving reports the deepest result focus reached, once, not where focus ended`() {
        setScreen()
        focusPoster(FIRST)
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        poster(SIXTH).assertIsFocused()
        press(Key.DirectionUp)
        assertEquals(emptyList<Int?>(), visits)

        leave()

        assertEquals(listOf<Int?>(SIXTH_INDEX), visits)
    }

    /** No cell reached is no depth, which the log drops: typing past results without entering them reports nothing. */
    @Test
    fun `results focus never reached report no depth`() {
        setScreen()
        focusField()

        leave()

        assertEquals(listOf<Int?>(null), visits)
    }

    @Test
    fun `a new query ends the visit and starts another`() {
        setScreen()
        focusPoster(SIXTH)

        uiState = uiState.resultsFor("tenet")
        composeTestRule.waitForIdle()
        assertEquals(listOf<Int?>(SIXTH_INDEX), visits)

        focusPoster(FIRST)
        leave()

        assertEquals(listOf<Int?>(SIXTH_INDEX, 0), visits)
    }

    @Test
    fun `a failure replacing the results ends the visit`() {
        setScreen()
        focusPoster(SIXTH)

        uiState = uiState.copy(state = TvSearchState.Failed(ErrorKind.Network, onRetry = {}))
        composeTestRule.waitForIdle()

        assertEquals(listOf<Int?>(SIXTH_INDEX), visits)
    }

    /** The phone reports the results arm only, and recommendations already report focus as impressions. */
    @Test
    fun `recommendations are not a results visit`() {
        uiState = tvSearchIdleUiState()
        setScreen()
        focusPoster("Trending Show 3")

        leave()

        assertEquals(emptyList<Int?>(), visits)
    }

    private fun setScreen() {
        composeTestRule.setContent {
            BingeTvTheme {
                if (shown) {
                    TvSearchContent(
                        uiState = uiState,
                        onQueryChange = {},
                        onSubmit = {},
                        onSuggestionSelected = {},
                        modifier = Modifier.fillMaxSize(),
                        onResultsVisitEnded = { visits += it },
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun leave() {
        shown = false
        composeTestRule.waitForIdle()
    }

    private fun focusField() {
        field().requestFocus()
        composeTestRule.waitForIdle()
        field().assertIsFocused()
    }

    /** Asserts focus arrived before any press: `performKeyInput` does not require its node to be focused. */
    private fun focusPoster(title: String) {
        focusField()
        poster(title).requestFocus()
        composeTestRule.waitForIdle()
        poster(title).assertIsFocused()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun field(): SemanticsNodeInteraction = composeTestRule.onNode(hasContentDescription("Search") and !hasSetTextAction())

    /** The grid follows the left column in composition order, so the last match is the poster, not a completion. */
    private fun poster(title: String): SemanticsNodeInteraction = composeTestRule.onAllNodes(hasText(title) and isFocusable()).onLast()
}

// tvResultItems() in a four-column grid: Westworld sits under Inception, Tenet beside it.
private const val FIRST = "Inception"
private const val SIXTH = "Tenet"
private const val SIXTH_INDEX = 5

private fun SearchUiState.resultsFor(query: String): SearchUiState =
    copy(
        query = query,
        state = TvSearchState.Results(query),
        cells = resultCells(tvResultTitles()),
    )
