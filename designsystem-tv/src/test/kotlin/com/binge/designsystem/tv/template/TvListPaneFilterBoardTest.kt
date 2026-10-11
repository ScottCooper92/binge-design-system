package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY_GENRE = "genre"
private const val KEY_LANGUAGE = "language"
private const val ROW_GENRE = "Genre"
private const val ROW_LANGUAGE = "Language"
private const val DRAMA = "Drama"
private const val COMEDY = "Comedy"
private const val CLEAR_GENRE = "Clear genre"
private const val CLEAR_ALL = "Clear all"
private const val GENRE_BODY = "Pick the genres."

/**
 * [TvListPaneBoard] used as a filter panel: checkbox values, a detail line on the list row, a clear button in the pane
 * and one under the list, all under a real D-pad.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvListPaneFilterBoardTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    private val described = mutableListOf<String>()
    private var genres by mutableStateOf(setOf(DRAMA))
    private var clearedGenre = 0
    private var clearedAll = 0
    private var showBody = false

    private fun show() {
        rule.setContent {
            var focusedKey by remember { mutableStateOf<String?>(KEY_GENRE) }
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Filters",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Refine",
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY_GENRE,
                                            label = ROW_GENRE,
                                            body = if (showBody) GENRE_BODY else "",
                                            detail = "${genres.size} selected",
                                            multiSelect = true,
                                            options =
                                                listOf(DRAMA, COMEDY).map { genre ->
                                                    TvPaneOption(
                                                        label = genre,
                                                        selected = genre in genres,
                                                        onSelect = { genres = if (genre in genres) genres - genre else genres + genre },
                                                    )
                                                },
                                            clear = TvPaneAction(CLEAR_GENRE, onClick = { clearedGenre++ }, enabled = genres.isNotEmpty()),
                                        ),
                                        TvPaneRow(key = KEY_LANGUAGE, label = ROW_LANGUAGE),
                                    ),
                            ),
                        ),
                    focusedKey = focusedKey,
                    onFocusRow = {
                        described += it
                        focusedKey = it
                    },
                    clearAll = TvPaneAction(CLEAR_ALL, onClick = { clearedAll++ }),
                )
            }
        }
        rule.waitForIdle()
        focus(ROW_GENRE)
    }

    private fun node(label: String) = rule.onNode(hasText(label) and isFocusable())

    private fun focus(label: String) {
        node(label).requestFocus()
        rule.waitForIdle()
    }

    private fun press(key: Key) {
        rule.onNode(isFocused()).performKeyInput { pressKey(key) }
        rule.waitForIdle()
    }

    private fun role(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

    @Test
    fun `the values are checkboxes that announce their toggled state`() {
        show()

        node(
            DRAMA,
        ).assert(role(Role.Checkbox)).assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
        node(
            COMEDY,
        ).assert(role(Role.Checkbox)).assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.Off))
    }

    @Test
    fun `a multi-select pane is not a selectable group`() {
        show()

        rule
            .onNode(hasText(DRAMA) and hasAnyAncestor(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup)))
            .assertDoesNotExist()
    }

    @Test
    fun `the list row carries its detail line, and it follows a toggle`() {
        show()
        rule.onNode(hasText("1 selected")).assertExists()

        focus(COMEDY)
        press(Key.DirectionCenter)

        rule.onNode(hasText("2 selected")).assertExists()
    }

    @Test
    fun `OK on a value toggles it and keeps focus there`() {
        show()
        focus(COMEDY)

        press(Key.DirectionCenter)

        assertEquals(setOf(DRAMA, COMEDY), genres)
        node(COMEDY).assertIsFocused()
        node(COMEDY).assert(SemanticsMatcher.expectValue(SemanticsProperties.ToggleableState, ToggleableState.On))
    }

    @Test
    fun `down walks the values to the clear button, and OK clears`() {
        show()
        press(Key.DirectionRight)
        node(DRAMA).assertIsFocused()

        press(Key.DirectionDown)
        node(COMEDY).assertIsFocused()
        press(Key.DirectionDown)
        node(CLEAR_GENRE).assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(1, clearedGenre)
    }

    @Test
    fun `left from the clear button returns to the row its pane describes`() {
        show()
        focus(CLEAR_GENRE)

        press(Key.DirectionLeft)

        node(ROW_GENRE).assertIsFocused()
    }

    @Test
    fun `a clear button with nothing to clear does nothing, and one with something does`() {
        show()
        rule.onNode(hasText(CLEAR_GENRE)).performClick()
        assertEquals(1, clearedGenre)

        genres = emptySet()
        rule.waitForIdle()
        rule.onNode(hasText(CLEAR_GENRE)).performClick()

        assertEquals(1, clearedGenre)
    }

    @Test
    fun `the board-wide clear is reached below the last row, acts, and describes nothing`() {
        show()
        focus(ROW_LANGUAGE)
        val describedBefore = described.toList()

        press(Key.DirectionDown)
        node(CLEAR_ALL).assertIsFocused()
        press(Key.DirectionCenter)

        assertEquals(1, clearedAll)
        assertEquals(describedBefore, described)
    }

    @Test
    fun `an empty body draws no description, a given one does`() {
        show()
        rule.onNode(hasText(GENRE_BODY)).assertDoesNotExist()
    }

    @Test
    fun `a given body is drawn above the values`() {
        showBody = true
        show()
        rule.onNode(hasText(GENRE_BODY)).assertExists()
    }
}
