package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.input.ImeAction
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeTextFieldTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun text(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private val clear get() = text(R.string.field_clear)
    private val show get() = text(R.string.field_secret_show)
    private val hide get() = text(R.string.field_secret_hide)

    private fun setTwoFields(
        secondAction: ImeAction = ImeAction.Done,
        onDone: (() -> Unit)? = null,
        clearable: Boolean = true,
    ) {
        rule.setContent {
            TestTheme {
                var a by remember { mutableStateOf("one") }
                var b by remember { mutableStateOf("two") }
                Column {
                    BingeTextField(value = a, label = "First", onValueChange = { a = it }, clearable = clearable)
                    BingeTextField(value = b, label = "Second", imeAction = secondAction, onDone = onDone, onValueChange = { b = it })
                }
            }
        }
    }

    @Test
    fun `Next moves focus to the following field`() {
        setTwoFields()
        rule.onNodeWithText("First").performTextInput("x")
        rule.onNodeWithText("First").performImeAction()
        rule.waitForIdle()
        rule.onNodeWithText("Second").assertIsFocused()
    }

    @Test
    fun `Done drops focus and runs the submit callback`() {
        var submitted = false
        setTwoFields(onDone = { submitted = true })
        rule.onNodeWithText("Second").performTextInput("x")
        rule.onNodeWithText("Second").performImeAction()
        rule.onNodeWithText("Second").assertIsNotFocused()
        assertTrue(submitted)
    }

    @Test
    fun `Go drops focus and runs the submit callback as Done does`() {
        var submitted = false
        setTwoFields(secondAction = ImeAction.Go, onDone = { submitted = true })
        rule.onNodeWithText("Second").performTextInput("x")
        rule.onNodeWithText("Second").performImeAction()
        rule.onNodeWithText("Second").assertIsNotFocused()
        assertTrue(submitted)
    }

    @Test
    fun `a focused non-empty field offers a clear button that empties it`() {
        setTwoFields()
        rule.onNodeWithContentDescription(clear).assertDoesNotExist()
        rule.onNodeWithText("First").performTextInput("x")
        rule.onNodeWithContentDescription(clear).performClick()
        rule.onNodeWithText("First").assertIsFocused()
        rule.onNodeWithContentDescription(clear).assertDoesNotExist()
    }

    @Test
    fun `a field that is not clearable never offers the clear button`() {
        setTwoFields(clearable = false)
        rule.onNodeWithText("First").performTextInput("x")
        rule.onNodeWithContentDescription(clear).assertDoesNotExist()
    }

    @Test
    fun `an empty, secret or read-only field carries no clear button`() {
        rule.setContent {
            TestTheme {
                Column {
                    BingeTextField(value = "", label = "Empty", onValueChange = {})
                    BingeTextField(value = "k", label = "Secret", secret = true, onValueChange = {})
                    BingeTextField(value = "k", label = "Locked", readOnly = true, onValueChange = {})
                }
            }
        }
        listOf("Empty", "Secret", "Locked").forEach { rule.onNodeWithText(it).performClick() }
        rule.onNodeWithContentDescription(clear).assertDoesNotExist()
    }

    @Test
    fun `a secret field's eye reveals and hides its own value`() {
        rule.setContent { TestTheme { BingeTextField(value = "k", label = "Key", secret = true, onValueChange = {}) } }
        rule.onNodeWithContentDescription(show).performClick()
        rule.onNodeWithContentDescription(hide).assertExists()
        rule.onNodeWithContentDescription(hide).performClick()
        rule.onNodeWithContentDescription(show).assertExists()
    }

    @Test
    fun `a caller-owned reveal state is the one that is drawn`() {
        var toggles = 0
        rule.setContent {
            TestTheme {
                BingeTextField(
                    value = "k",
                    label = "Key",
                    secret = true,
                    revealed = true,
                    onToggleReveal = { toggles++ },
                    onValueChange = {},
                )
            }
        }
        rule.onNodeWithContentDescription(hide).performClick()
        assertEquals(1, toggles)
        rule.onNodeWithContentDescription(hide).assertExists()
    }

    @Test
    fun `a required field marks its label`() {
        rule.setContent { TestTheme { BingeTextField(value = "", label = "Name", required = true, onValueChange = {}) } }
        rule.onNodeWithText("Name *").assertExists()
    }
}
