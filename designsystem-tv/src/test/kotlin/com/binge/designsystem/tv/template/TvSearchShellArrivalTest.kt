package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
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
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.focus.LocalTvShellArrival
import com.binge.designsystem.tv.focus.TvShellArrival
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The shell's arrival on the search screen, whose entry group routes to the result cell last focused, else the suggestion last
 * focused, else the field. The target sits on the screen's root, so one request lands on whichever the saved state names. A
 * saveable state holder stands in for the nav entry decorator, hiding the screen and showing it again.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvSearchShellArrivalTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    private val arrival = TvShellArrival()
    private var shown by mutableStateOf(true)
    private var uiState by mutableStateOf(SearchUiState())

    @Test
    fun `an armed arrival reaches the field when nothing has been focused`() {
        setScreen(SearchUiState(isTv = true, recentSearches = List(SUGGESTIONS) { "Search $it" }))
        hide()
        arrival.arm()
        show()

        field().assertIsFocused()
    }

    @Test
    fun `an armed arrival reaches the result cell last focused`() {
        setScreen(resultsState())
        focusField()
        focusPoster("Title 0")
        hide()
        arrival.arm()
        show()

        poster("Title 0").assertIsFocused()
    }

    @Test
    fun `an armed arrival reaches the suggestion last focused`() {
        setScreen(SearchUiState(isTv = true, recentSearches = List(SUGGESTIONS) { "Search $it" }))
        focusField()
        press(Key.DirectionDown)
        suggestion("Search 0").assertIsFocused()
        hide()
        arrival.arm()
        show()

        suggestion("Search 0").assertIsFocused()
    }

    /** A pop rebuilds the shell and arms an arrival: the result the user opened wins over the first, as on Companions. */
    @Test
    fun `an armed arrival reaches a later result cell, not the first`() {
        setScreen(resultsState())
        focusField()
        press(Key.DirectionDown)
        press(Key.DirectionRight)
        press(Key.DirectionRight)
        poster("Title 1").assertIsFocused()
        hide()
        arrival.arm()
        show()

        poster("Title 1").assertIsFocused()
    }

    @Test
    fun `an armed arrival reaches a later suggestion, not the first`() {
        setScreen(SearchUiState(isTv = true, recentSearches = List(SUGGESTIONS) { "Search $it" }))
        focusField()
        press(Key.DirectionDown)
        press(Key.DirectionDown)
        suggestion("Search 1").assertIsFocused()
        hide()
        arrival.arm()
        show()

        suggestion("Search 1").assertIsFocused()
    }

    @Test
    fun `with nothing armed the screen takes no focus on its own`() {
        setScreen(SearchUiState(isTv = true, recentSearches = List(SUGGESTIONS) { "Search $it" }))
        hide()
        show()

        composeTestRule.onAllNodes(isFocused()).assertCountEquals(0)
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
                CompositionLocalProvider(LocalTvShellArrival provides arrival) {
                    val states = rememberSaveableStateHolder()
                    if (shown) {
                        states.SaveableStateProvider(SCREEN_STATE) {
                            TvSearchContent(
                                uiState = uiState,
                                onQueryChange = {},
                                onSubmit = {},
                                onSuggestionSelected = {},
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun hide() {
        composeTestRule.runOnUiThread { shown = false }
        composeTestRule.waitForIdle()
    }

    private fun show() {
        composeTestRule.runOnUiThread { shown = true }
        composeTestRule.waitForIdle()
    }

    private fun focusField() {
        field().requestFocus()
        composeTestRule.waitForIdle()
        field().assertIsFocused()
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

    private fun field(): SemanticsNodeInteraction = composeTestRule.onNode(hasContentDescription(FIELD_LABEL) and !hasSetTextAction())

    private fun poster(title: String): SemanticsNodeInteraction = composeTestRule.onAllNodes(hasText(title) and isFocusable()).onLast()

    private fun suggestion(text: String): SemanticsNodeInteraction = composeTestRule.onNode(hasText(text) and isFocusable())
}

private const val SCREEN_STATE = "search"
private const val FIELD_LABEL = "Search"
private const val RESULTS = 24
private const val SUGGESTIONS = 6
