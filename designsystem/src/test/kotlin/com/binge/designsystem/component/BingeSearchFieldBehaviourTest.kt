package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
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
}
