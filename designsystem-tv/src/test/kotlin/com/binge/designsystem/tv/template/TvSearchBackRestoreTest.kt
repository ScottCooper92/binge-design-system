package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Back from a title to the search lands on what was opened, with the grid where it was. The shell is disposed under the
 * page a result opens, and what comes back is the saved state of its entry: a saveable state holder, the one the app's nav
 * entry decorator wraps each entry in, stands in for that here, hiding the screen for the page and showing it again. The
 * shell's focus hand-off is the request on the screen's own group.
 *
 * `television` qualifier: a 2D focus search on Robolectric's default screen resolves nothing like a TV panel's geometry.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvSearchBackRestoreTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    private val handOff = FocusRequester()
    private var shown by mutableStateOf(true)
    private var uiState by mutableStateOf(SearchUiState())

    @Test
    fun `a result in the fourth row is focused and in view when the screen comes back`() {
        setScreen(resultsState())
        focusField()
        focusPoster("Title 0")
        repeat(ROWS_DOWN) { press(Key.DirectionDown) }
        press(Key.DirectionRight)
        poster("Title 13").assertIsFocused()

        openPageAndBack()

        poster("Title 13").assertIsFocused()
        poster("Title 13").assertIsDisplayed()
    }

    @Test
    fun `a suggestion is focused when the screen comes back`() {
        setScreen(SearchUiState(isTv = true, recentSearches = List(SUGGESTIONS) { "Search $it" }))
        focusField()
        repeat(3) { press(Key.DirectionDown) }
        suggestion("Search 2").assertIsFocused()

        openPageAndBack()

        suggestion("Search 2").assertIsFocused()
    }

    @Test
    fun `a result that filled the plate focus was on is where the screen comes back to`() {
        setScreen(SearchUiState(query = "title", isTv = true, state = TvSearchState.LoadingResults))
        focusField()
        press(Key.DirectionCenter)
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        uiState = resultsState()
        composeTestRule.waitForIdle()
        poster("Title 0").assertIsFocused()
        openPageAndBack()

        poster("Title 0").assertIsFocused()
    }

    private fun resultsState(): SearchUiState =
        SearchUiState(
            query = "title",
            isTv = true,
            state = TvSearchState.Results("title"),
            cells = resultCells(List(RESULTS) { "Title $it" }),
        )

    private fun setScreen(state: SearchUiState) {
        uiState = state
        composeTestRule.setContent {
            BingeTvTheme {
                val states = rememberSaveableStateHolder()
                if (shown) {
                    states.SaveableStateProvider(SCREEN_STATE) {
                        TvSearchContent(
                            uiState = uiState,
                            onQueryChange = {},
                            onSubmit = {},
                            onSuggestionSelected = {},
                            modifier = Modifier.fillMaxSize().focusRequester(handOff),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun openPageAndBack() {
        composeTestRule.runOnUiThread { shown = false }
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread { shown = true }
        composeTestRule.waitForIdle()
        composeTestRule.runOnUiThread { runCatching { handOff.requestFocus() } }
        composeTestRule.waitForIdle()
    }

    /** Enter through the field: a request into the screen from outside is redirected by its entry group's onEnter. */
    private fun focusField() {
        val field = composeTestRule.onNode(hasContentDescription(FIELD_LABEL) and !hasSetTextAction())
        field.requestFocus()
        composeTestRule.waitForIdle()
        field.assertIsFocused()
    }

    private fun focusPoster(title: String) {
        poster(title).requestFocus()
        composeTestRule.waitForIdle()
        poster(title).assertIsFocused()
    }

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    private fun poster(title: String): SemanticsNodeInteraction = composeTestRule.onAllNodes(hasText(title) and isFocusable()).onLast()

    private fun suggestion(text: String): SemanticsNodeInteraction = composeTestRule.onNode(hasText(text) and isFocusable())
}

private const val SCREEN_STATE = "search"
private const val FIELD_LABEL = "Search"
private const val RESULTS = 24
private const val SUGGESTIONS = 6
private const val ROWS_DOWN = 3
