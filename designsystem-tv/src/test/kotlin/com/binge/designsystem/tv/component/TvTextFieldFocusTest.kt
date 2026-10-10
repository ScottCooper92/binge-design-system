package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
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

private const val NAME = "Display name"
private const val ADDRESS = "Server address"
private const val NOTES = "Notes"
private const val SEARCH = "Search"
private const val EXAMPLE = "http://192.168.1.10:5055"

/**
 * The fields under a real D-pad (#580). A TV raises the keyboard whenever a text input holds focus, so "the keyboard
 * stays down" is "the input is not focused": the frame takes focus, and only select hands it to the input.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvTextFieldFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private var actions = 0

    @Test
    fun `focus lands on the frame and leaves the input, so the keyboard stays down`() {
        setFields()
        frame(ADDRESS).requestFocus()
        composeTestRule.waitForIdle()

        frame(ADDRESS).assertIsFocused()
        input(ADDRESS).assertIsNotFocused()
    }

    @Test
    fun `select on the frame focuses the input`() {
        setFields()
        frame(ADDRESS).requestFocus()
        composeTestRule.waitForIdle()

        press(Key.DirectionCenter)

        input(ADDRESS).assertIsFocused()
    }

    /** One line has no up or down to move the cursor to, so the remote leaves the field rather than being stuck in it. */
    @Test
    fun `down while editing leaves the field for the one below`() {
        setFields()
        frame(ADDRESS).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        input(ADDRESS).assertIsFocused()

        press(Key.DirectionDown)

        input(ADDRESS).assertIsNotFocused()
        frame(NOTES).assertIsFocused()
    }

    /**
     * A field sits below another, so "the page's first stop" and "its own frame" are different nodes, and dropping the
     * hand-back to the frame would turn this red.
     */
    @Test
    fun `the keyboard action hands focus back to the frame, then runs the action`() {
        setFields()
        frame(ADDRESS).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)

        input(ADDRESS).performImeAction()
        composeTestRule.waitForIdle()

        frame(ADDRESS).assertIsFocused()
        assertEquals(1, actions)
    }

    @Test
    fun `the search field edits on select and searches from the keyboard`() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Field(NAME)
                    var query by remember { mutableStateOf("") }
                    TvSearchField(
                        value = query,
                        onValueChange = { query = it },
                        label = SEARCH,
                        placeholder = "Films",
                        onSearch = { actions++ },
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        frame(SEARCH).requestFocus()
        composeTestRule.waitForIdle()
        input(SEARCH).assertIsNotFocused()

        press(Key.DirectionCenter)
        input(SEARCH).assertIsFocused()
        input(SEARCH).performImeAction()
        composeTestRule.waitForIdle()

        frame(SEARCH).assertIsFocused()
        assertEquals(1, actions)
    }

    /** The caret starts at the end when select opens the input, so typing into a field that holds text appends. */
    @Test
    fun `typing into a field that already holds text appends to it`() {
        var name by mutableStateOf("dun")
        var query by mutableStateOf("dun")
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    TvTextField(value = name, onValueChange = { name = it }, label = NAME)
                    TvSearchField(value = query, onValueChange = { query = it }, label = SEARCH, placeholder = "Films", onSearch = {})
                }
            }
        }
        composeTestRule.waitForIdle()

        frame(NAME).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        input(NAME).performTextInput("e")
        composeTestRule.waitForIdle()

        frame(SEARCH).requestFocus()
        composeTestRule.waitForIdle()
        press(Key.DirectionCenter)
        input(SEARCH).performTextInput("e")
        composeTestRule.waitForIdle()

        assertEquals("dune", name)
        assertEquals("dune", query)
    }

    @Test
    fun `the placeholder shows only while the field is empty`() {
        var value by mutableStateOf("")
        composeTestRule.setContent {
            BingeTvTheme { TvTextField(value = value, onValueChange = { value = it }, label = ADDRESS, placeholder = EXAMPLE) }
        }
        composeTestRule.onNodeWithText(EXAMPLE).assertExists()

        value = "http://seerr.lan"
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(EXAMPLE).assertDoesNotExist()
    }

    private fun setFields() {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    Field(NAME)
                    Field(ADDRESS, onImeAction = { actions++ })
                    Field(NOTES)
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Composable
    private fun Field(label: String, onImeAction: (() -> Unit)? = null) {
        var value by remember { mutableStateOf("") }
        TvTextField(value = value, onValueChange = { value = it }, label = label, onImeAction = onImeAction)
    }

    /** The frame names the field and cannot take text; the input inside it can. */
    private fun frame(label: String) = composeTestRule.onNode(hasContentDescription(label) and isFocusable() and !hasSetTextAction())

    private fun input(label: String) = composeTestRule.onNode(hasContentDescription(label) and hasSetTextAction())

    private fun press(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
