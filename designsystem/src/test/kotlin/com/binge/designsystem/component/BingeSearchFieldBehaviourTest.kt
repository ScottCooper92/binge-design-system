package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.input.ImeAction
import com.binge.designsystem.R
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

/** The search field's behaviour a frame cannot show (#394): the clear button, the keyboard's submit, a caller's focus. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSearchFieldBehaviourTest {
    @get:Rule
    val rule = createComposeRule()

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
}
