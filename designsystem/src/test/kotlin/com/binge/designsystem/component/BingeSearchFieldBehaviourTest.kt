package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import com.binge.designsystem.R
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val PLACEHOLDER = "Search films"

/** Inside the pill's start padding, clear of the icon and the text field. */
private const val PILL_EDGE_PX = 4f

/** The search field's behaviour a frame cannot show: the clear button, the keyboard's submit, a caller's focus. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSearchFieldBehaviourTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val clear = RuntimeEnvironment.getApplication().getString(R.string.cd_clear_query)

    @Test
    fun `the clear button shows only with a query, and clears it`() {
        var query by mutableStateOf("")
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = query, onQueryChange = { query = it }, onClear = { query = "" }, placeholder = PLACEHOLDER)
            }
        }
        rule.onNodeWithContentDescription(clear).assertDoesNotExist()

        query = "dune"
        rule.waitForIdle()
        rule.onNodeWithContentDescription(clear).performClick()
        rule.waitForIdle()

        assertEquals("", query)
        rule.onNodeWithContentDescription(clear).assertDoesNotExist()
    }

    @Test
    fun `the keyboard's done action submits too, for a field that asks for it`() {
        var submits = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(
                    query = "dune",
                    onQueryChange = {},
                    onClear = {},
                    placeholder = PLACEHOLDER,
                    imeAction = ImeAction.Done,
                    onSubmit = { submits++ },
                )
            }
        }

        rule.onNode(hasSetTextAction()).performImeAction()

        assertEquals(1, submits)
    }

    @Test
    fun `the keyboard's search action submits`() {
        var submits = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = "dune", onQueryChange = {}, onClear = {}, placeholder = PLACEHOLDER, onSubmit = { submits++ })
            }
        }

        rule.onNode(hasSetTextAction()).performImeAction()

        assertEquals(1, submits)
    }

    /** The KDoc warns two requesters cannot both bind, so a caller's replaces the field's own rather than joining it. */
    @Test
    fun `a caller's focus requester focuses the field`() {
        val requester = FocusRequester()
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = "", onQueryChange = {}, onClear = {}, placeholder = PLACEHOLDER, focusRequester = requester)
            }
        }

        rule.runOnIdle { requester.requestFocus() }

        rule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun `a tap on the pill outside the text focuses the field`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = "", onQueryChange = {}, onClear = {}, placeholder = PLACEHOLDER)
            }
        }
        rule.onNode(hasSetTextAction()).assertIsNotFocused()

        rule.onRoot().performTouchInput { click(Offset(PILL_EDGE_PX, height / 2f)) }
        rule.waitForIdle()

        rule.onNode(hasSetTextAction()).assertIsFocused()
    }

    private fun caret(): Int =
        rule
            .onNode(hasSetTextAction())
            .fetchSemanticsNode()
            .config[SemanticsProperties.TextSelectionRange]
            .start

    @Test
    fun `a field that opens with a query has its caret at the end of it`() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = "hardware", onQueryChange = {}, onClear = {}, placeholder = PLACEHOLDER)
            }
        }
        rule.waitForIdle()
        assertEquals("hardware".length, caret())
    }

    @Test
    fun `typing goes on from the caret, and a query set from outside puts the caret at its end`() {
        var query by mutableStateOf("gpu")
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = query, onQueryChange = { query = it }, onClear = { query = "" }, placeholder = PLACEHOLDER)
            }
        }
        rule.onNode(hasSetTextAction()).performTextInput("s")
        rule.waitForIdle()
        assertEquals("gpus", query)
        assertEquals(4, caret())

        query = "transcoding"
        rule.waitForIdle()
        assertEquals("transcoding".length, caret())
    }

    @Test
    fun `a query the caller lowercases keeps the caret where it was`() {
        var query by mutableStateOf("gpu")
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(
                    query = query,
                    onQueryChange = { query = it.lowercase().filterNot(Char::isWhitespace) },
                    onClear = { query = "" },
                    placeholder = PLACEHOLDER,
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(1))

        rule.onNode(hasSetTextAction()).performTextInput(" ")
        rule.waitForIdle()

        // The space is dropped, so the text is as it was and the caret is back after the g, not at the end.
        assertEquals("gpu", query)
        assertEquals(1, caret())
    }

    @Test
    fun `a query that arrives after the edit keeps the caret where the user typed`() {
        var query by mutableStateOf("gpu")
        var received = query
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeSearchField(query = query, onQueryChange = { received = it }, onClear = { query = "" }, placeholder = PLACEHOLDER)
            }
        }
        rule.onNode(hasSetTextAction()).performTextInputSelection(TextRange(1))

        rule.onNode(hasSetTextAction()).performTextInput("x")
        rule.waitForIdle()
        assertEquals(1, caret())

        query = received
        rule.waitForIdle()

        assertEquals("gxpu", query)
        assertEquals(2, caret())
    }
}
