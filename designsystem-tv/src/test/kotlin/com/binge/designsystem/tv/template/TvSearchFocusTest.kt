package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextReplacement
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
 * The search's D-pad paths: Search handing focus to the first result, ← from the grid reaching the field, and the left
 * list scrolling as focus walks down it. A baseline takes focus as a parameter, so none of these can show in a frame.
 *
 * `television` qualifier: a 2D focus search on Robolectric's default screen resolves nothing like a TV panel's geometry.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvSearchFocusTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    private var uiState by mutableStateOf(SearchUiState())
    private var submits = 0
    private var retries = 0

    /** Search on the keyboard moves focus to the grid at once, onto the plate the first result arrives in. */
    @Test
    fun submitHandsFocusToTheGridOnTheKeyAndTheFirstResultFillsTheSameNode() {
        setScreen(tvSearchLoadingResultsUiState())
        focusField()
        press(Key.DirectionCenter)

        editor().performImeAction()
        composeTestRule.waitForIdle()

        assertEquals(1, submits)
        val plate = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id
        composeTestRule.onAllNodes(isFocused() and hasText(FIRST_RESULT)).assertCountEquals(0)

        uiState = tvResultsUiState()
        composeTestRule.waitForIdle()

        poster(FIRST_RESULT).assertIsFocused()
        assertEquals(plate, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)
    }

    /** A search that found nothing leaves focus on the grid, with ← still reaching the field to try again. */
    @Test
    fun aSearchThatFoundNothingLeavesFocusOnTheGridAndLeftReachesTheField() {
        setScreen(tvSearchLoadingResultsUiState())
        focusField()
        press(Key.DirectionCenter)
        editor().performImeAction()
        composeTestRule.waitForIdle()

        uiState = tvSearchEmptyUiState()
        composeTestRule.waitForIdle()

        field().assertIsNotFocused()
        press(Key.DirectionLeft)
        field().assertIsFocused()
    }

    /** Results already on screen are not the wait: Search lands on the grid at once, and the new results fill it in place. */
    @Test
    fun searchOnTextTheResultsOnScreenAreNotForStillMovesFocusAtOnce() {
        setScreen(tvResultsUiState())
        focusField()
        press(Key.DirectionCenter)
        editor().performTextReplacement("heat")
        editor().performImeAction()
        composeTestRule.waitForIdle()
        val cell = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id

        uiState = tvResultsUiState().resultsFor("heat")
        composeTestRule.waitForIdle()

        poster(FIRST_RESULT).assertIsFocused()
        assertEquals(cell, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)
    }

    /** A failure collapses the grid to its message, and focus on the result it removed moves to Try again: OK retries. */
    @Test
    fun aFailureMovesFocusFromTheFocusedResultToTryAgain() {
        setScreen(tvResultsUiState())
        focusField()
        focusPoster(FIRST_RESULT)

        uiState = tvResultsUiState().failed()
        composeTestRule.waitForIdle()

        assertFocusOnTryAgain()
        press(Key.DirectionCenter)
        assertEquals(1, retries)
    }

    /** Focus on a loading plate other than the first moves to Try again when the search fails. */
    @Test
    fun aFailureMovesFocusFromAnyLoadingPlateToTryAgain() {
        setScreen(tvSearchLoadingResultsUiState())
        focusField()
        press(Key.DirectionCenter)
        editor().performImeAction()
        composeTestRule.waitForIdle()
        press(Key.DirectionRight)

        uiState = tvSearchLoadingResultsUiState().failed()
        composeTestRule.waitForIdle()

        assertFocusOnTryAgain()
        press(Key.DirectionCenter)
        assertEquals(1, retries)
    }

    /**
     * Focus on a result past the third row when the search fails. The grid had scrolled, so that cell sat past the
     * twelve plates a loading grid draws: before the collapse it kept focus on a node scrolled out of view.
     */
    @Test
    fun aFailureWithFocusPastTheThirdRowMovesFocusToTryAgainOnScreen() {
        setScreen(manyResultsUiState())
        focusField()
        focusPoster(MANY_RESULTS_FIRST)
        repeat(ROWS_DOWN) { press(Key.DirectionDown) }
        poster(manyResultTitle(ROWS_DOWN * GRID_COLUMNS)).assertIsFocused()

        uiState = manyResultsUiState().failed()
        composeTestRule.waitForIdle()

        assertFocusOnTryAgain()
        press(Key.DirectionCenter)
        assertEquals(1, retries)
    }

    /** A failure while the field holds focus leaves it there: typing is not interrupted. → still reaches Try again. */
    @Test
    fun aFailureWhileTheFieldHoldsFocusLeavesItThere() {
        setScreen(tvResultsUiState())
        focusField()

        uiState = tvResultsUiState().failed()
        composeTestRule.waitForIdle()

        field().assertIsFocused()
        press(Key.DirectionRight)
        assertFocusOnTryAgain()
    }

    /** Try again takes the message away, and focus goes back into the grid it brings back, onto the first result. */
    @Test
    fun tryAgainHandsFocusBackToTheGrid() {
        setScreen(tvResultsUiState())
        focusField()
        focusPoster(FIRST_RESULT)
        uiState = tvResultsUiState().failed()
        composeTestRule.waitForIdle()
        assertFocusOnTryAgain()

        press(Key.DirectionCenter)
        uiState = tvSearchLoadingResultsUiState()
        composeTestRule.waitForIdle()

        uiState = tvResultsUiState()
        composeTestRule.waitForIdle()
        poster(FIRST_RESULT).assertIsFocused()
    }

    /**
     * The failure stays composed, unseen, until the grid has laid out, so focus goes from Try again straight to the first
     * result. Stepped a frame at a time, some node holds focus on every frame and it is never the field.
     */
    @Test
    fun focusNeverLeavesTryAgainForAnythingButTheFirstResult() {
        val base = tvResultsUiState().resultsFor("okapi")
        setScreen(base.failed())
        focusField()
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        composeTestRule.waitForIdle()

        composeTestRule.mainClock.autoAdvance = false
        uiState = base
        repeat(STEPPED_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
            field().assertIsNotFocused()
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        poster(FIRST_RESULT).assertIsFocused()
        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
    }

    /** Focus was deep in the failed grid, whose scroll is gone: the retried grid hands focus to a cell that is on screen. */
    @Test
    fun aRetryAfterAFailureDeepInTheGridLandsOnADisplayedPoster() {
        val base = manyResultsUiState()
        setScreen(base)
        focusField()
        focusPoster(MANY_RESULTS_FIRST)
        repeat(ROWS_DOWN) { press(Key.DirectionDown) }
        uiState = base.failed()
        composeTestRule.waitForIdle()
        assertFocusOnTryAgain()

        press(Key.DirectionCenter)
        uiState = base
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
        composeTestRule.onNode(isFocused()).assertIsDisplayed().assert(!hasText(TRY_AGAIN))
        poster(MANY_RESULTS_FIRST).assertIsFocused()
    }

    /**
     * A cold start whose trending feeds fail shows the same Try again with no query. Retrying it brings the recommendations
     * back, and focus goes from Try again to the first of them, never through the field.
     */
    @Test
    fun anIdleFailureRetriesIntoTheRecommendationsWithoutPassingTheField() {
        val idle = tvSearchIdleUiState()
        setScreen(idle.failed())
        focusField()
        press(Key.DirectionRight)
        assertFocusOnTryAgain()

        press(Key.DirectionCenter)
        composeTestRule.mainClock.autoAdvance = false
        uiState = idle
        repeat(STEPPED_FRAMES) {
            composeTestRule.mainClock.advanceTimeByFrame()
            composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
            field().assertIsNotFocused()
        }
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
        composeTestRule.onNode(isFocused()).assertIsDisplayed().assert(!hasText(TRY_AGAIN))
        field().assertIsNotFocused()
    }

    /**
     * A retry that fails again leaves the state exactly as it was, so the screen sees nothing. The request waits on the
     * retried query, and a new query ends it: results for it must not pull focus out of the field.
     */
    @Test
    fun aRetryThatFailsAgainDoesNotPullFocusIntoALaterSearch() {
        val failed = tvResultsUiState().resultsFor("okapi").failed()
        setScreen(failed)
        focusField()
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        uiState = failed
        composeTestRule.waitForIdle()
        focusField()

        uiState = tvResultsUiState().resultsFor("heat")
        composeTestRule.waitForIdle()

        field().assertIsFocused()
    }

    /** Clearing the field after a retry ends the request: the results of what is typed next must not take focus either. */
    @Test
    fun clearingTheFieldAfterARetryEndsTheRequest() {
        val failed = tvResultsUiState().resultsFor("okapi").failed()
        setScreen(failed)
        focusField()
        press(Key.DirectionRight)
        press(Key.DirectionCenter)
        focusField()

        uiState = failed.copy(query = "", state = TvSearchState.Idle)
        composeTestRule.waitForIdle()
        field().assertIsFocused()

        uiState = tvResultsUiState().resultsFor("okapi")
        composeTestRule.waitForIdle()

        field().assertIsFocused()
    }

    /** Focus resting on a suggestion plate while the sources load is on the row the suggestion arrives in. */
    @Test
    fun focusOnASuggestionPlateStaysOnTheRowWhenTheSuggestionsArrive() {
        setScreen(tvSearchLoadingIdleUiState().copy(recentSearches = emptyList(), trendingTitles = emptyList()))
        focusField()
        press(Key.DirectionDown)
        val plate = composeTestRule.onNode(isFocused()).fetchSemanticsNode().id
        press(Key.DirectionCenter)
        assertEquals(plate, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)

        uiState = SearchUiState(isTv = true, recentSearches = listOf("Search 0", "Search 1"))
        composeTestRule.waitForIdle()

        suggestion("Search 0").assertIsFocused()
        assertEquals(plate, composeTestRule.onNode(isFocused()).fetchSemanticsNode().id)
    }

    /** ← from the first row of the grid crosses to the field it sits level with, not to a suggestion below it. */
    @Test
    fun leftFromTheTopOfTheGridReachesTheField() {
        setScreen(tvResultsUiState())
        // Enter through the field: a request into the screen from outside is redirected by its entry group's onEnter.
        focusField()
        focusPoster(FIRST_RESULT)

        press(Key.DirectionLeft)

        field().assertIsFocused()
    }

    /** ↓ through a list longer than the column keeps the focused row on screen by scrolling the list under it. */
    @Test
    fun theSuggestionListScrollsToKeepTheFocusedRowInView() {
        setScreen(SearchUiState(isTv = true, recentSearches = List(LIST_LENGTH) { "Search $it" }))
        focusField()
        suggestion("Search 0").assertIsDisplayed()

        press(Key.DirectionDown)
        suggestion("Search 0").assertIsFocused()
        repeat(LIST_LENGTH - 1) { press(Key.DirectionDown) }

        val last = suggestion("Search ${LIST_LENGTH - 1}")
        last.assertIsFocused()
        last.assertIsDisplayed()
        suggestion("Search 0").assertIsNotDisplayed()
    }

    private fun setScreen(state: SearchUiState) {
        uiState = state
        composeTestRule.setContent {
            BingeTvTheme {
                TvSearchContent(
                    uiState = uiState,
                    onQueryChange = {},
                    onSubmit = { submits++ },
                    onSuggestionSelected = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    /** One node holds focus, on screen, and it is Try again: not a cell the failure kept, nor nothing. */
    private fun assertFocusOnTryAgain() {
        composeTestRule.onAllNodes(isFocused()).assertCountEquals(1)
        composeTestRule.onNode(isFocused()).assert(hasText(TRY_AGAIN)).assertIsDisplayed()
    }

    private fun SearchUiState.failed(): SearchUiState = copy(state = TvSearchState.Failed(ErrorKind.Network, onRetry = { retries++ }))

    private fun focusField() {
        field().requestFocus()
        composeTestRule.waitForIdle()
        field().assertIsFocused()
    }

    /**
     * Puts focus on a poster and asserts it got there before any press. Load-bearing: `performKeyInput` does not require
     * its node to be focused, so a request that quietly failed would leave the test pressing from somewhere else.
     */
    private fun focusPoster(title: String) {
        poster(title).requestFocus()
        composeTestRule.waitForIdle()
        poster(title).assertIsFocused()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    /** The field's focus frame; the input inside carries the same label, so the frame is the one that cannot take text. */
    private fun field(): SemanticsNodeInteraction = composeTestRule.onNode(hasContentDescription(FIELD_LABEL) and !hasSetTextAction())

    private fun editor(): SemanticsNodeInteraction = composeTestRule.onNode(hasSetTextAction())

    /** The grid follows the left column in composition order, so the last match is the poster, not a completion of its title. */
    private fun poster(title: String): SemanticsNodeInteraction = composeTestRule.onAllNodes(hasText(title) and isFocusable()).onLast()

    private fun suggestion(text: String): SemanticsNodeInteraction = composeTestRule.onNode(hasText(text) and isFocusable())
}

private const val FIRST_RESULT = "Inception"
private const val FIELD_LABEL = "Search"

// Far more rows than a 540dp panel's column holds, so the last one can only be reached by scrolling.
private const val LIST_LENGTH = 24

// Past the twelve plates a loading grid draws, so the focused cell is one the failure cannot keep as a plate.
private const val MANY_RESULTS = 24
private const val GRID_COLUMNS = 4
private const val ROWS_DOWN = 4
private const val TRY_AGAIN = "Try again"
private const val STEPPED_FRAMES = 8
private val MANY_RESULTS_FIRST = manyResultTitle(0)

private fun manyResultTitle(index: Int): String = "Film $index"

private fun manyResultsUiState(): SearchUiState =
    tvResultsUiState().copy(
        state = TvSearchState.Results("nolan"),
        cells = resultCells(List(MANY_RESULTS) { manyResultTitle(it) }),
    )

private fun SearchUiState.resultsFor(query: String): SearchUiState =
    copy(
        query = query,
        state = TvSearchState.Results(query),
        cells = resultCells(tvResultTitles()),
    )
