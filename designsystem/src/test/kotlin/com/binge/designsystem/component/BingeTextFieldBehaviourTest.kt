package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.text.TextRange
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Where the caret sits in a pre-filled [BingeTextField]: at the end of the value, not before it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTextFieldBehaviourTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val clear = RuntimeEnvironment.getApplication().getString(R.string.field_clear)

    private fun field(index: Int = 0): SemanticsNodeInteraction = rule.onAllNodes(hasSetTextAction())[index]

    private fun caret(index: Int = 0): Int = field(index).fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange].start

    @Test
    fun `a pre-filled field shows its start until it is focused, then has its caret at the end`() {
        rule.setContent { TestTheme { BingeTextField(value = "192.168.1.20", label = "Address", onValueChange = {}) } }
        rule.waitForIdle()
        assertEquals(0, caret())

        field().requestFocus()
        rule.waitForIdle()
        assertEquals("192.168.1.20".length, caret())
    }

    @Test
    fun `Next lands in the following pre-filled field with the caret at its end`() {
        rule.setContent {
            TestTheme {
                var a by remember { mutableStateOf("host") }
                var b by remember { mutableStateOf("8096") }
                Column {
                    BingeTextField(value = a, label = "Host", onValueChange = { a = it })
                    BingeTextField(value = b, label = "Port", onValueChange = { b = it })
                }
            }
        }
        field(0).performTextInput("s")
        field(0).performImeAction()
        rule.waitForIdle()

        field(1).assertIsFocused()
        assertEquals("8096".length, caret(1))
    }

    @Test
    fun `a focus requester puts the caret at the end of a pre-filled field`() {
        val requester = FocusRequester()
        rule.setContent {
            TestTheme {
                BingeTextField(value = "Living room", label = "Name", onValueChange = {}, modifier = Modifier.focusRequester(requester))
            }
        }
        rule.runOnIdle { requester.requestFocus() }
        rule.waitForIdle()

        field().assertIsFocused()
        assertEquals("Living room".length, caret())
    }

    @Test
    fun `typing goes on from the caret, and a value set from outside puts the caret at its end`() {
        var value by mutableStateOf("gpu")
        rule.setContent { TestTheme { BingeTextField(value = value, label = "Name", onValueChange = { value = it }) } }
        field().performTextInput("s")
        rule.waitForIdle()
        assertEquals("gpus", value)
        assertEquals(4, caret())

        value = "transcoding"
        rule.waitForIdle()
        assertEquals("transcoding".length, caret())
    }

    @Test
    fun `the clear button empties the value and leaves the caret at zero`() {
        var value by mutableStateOf("abc")
        rule.setContent { TestTheme { BingeTextField(value = value, label = "Name", onValueChange = { value = it }) } }
        field().performTextInput("d")
        rule.onNodeWithContentDescription(clear).performClick()
        rule.waitForIdle()

        assertEquals("", value)
        assertEquals(0, caret())
    }

    @Test
    fun `a secret field has its caret at the end of its value once focused`() {
        rule.setContent { TestTheme { BingeTextField(value = "s3cret-key", label = "Key", secret = true, onValueChange = {}) } }
        field().requestFocus()
        rule.waitForIdle()
        assertEquals("s3cret-key".length, caret())
    }

    @Test
    fun `a filter that rewrites what was typed leaves the caret where it was`() {
        var value by mutableStateOf("8096")
        rule.setContent {
            TestTheme {
                BingeTextField(
                    value = value,
                    label = "Port",
                    onValueChange = { value = it.filter(Char::isDigit) },
                )
            }
        }
        field().requestFocus()
        field().performTextInputSelection(TextRange(2))

        field().performTextInput("x")
        rule.waitForIdle()

        assertEquals("8096", value)
        assertEquals(2, caret())
    }

    @Test
    fun `a value that arrives after the edit keeps the caret where the user typed`() {
        var value by mutableStateOf("8096")
        var received = value
        rule.setContent { TestTheme { BingeTextField(value = value, label = "Port", onValueChange = { received = it }) } }
        field().requestFocus()
        field().performTextInputSelection(TextRange(2))

        field().performTextInput("7")
        rule.waitForIdle()
        // The caller's state has not caught up, so the field still shows the old text, with the caret where the user left it.
        assertEquals("8096", value)
        assertEquals(2, caret())

        value = received
        rule.waitForIdle()

        assertEquals("80796", value)
        assertEquals(3, caret())
    }
}
