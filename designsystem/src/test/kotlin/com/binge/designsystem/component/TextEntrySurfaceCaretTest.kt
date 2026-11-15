package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Where the caret sits in a pre-filled [TextEntrySurface] (#1151): at the end of the value once focused, not before it. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TextEntrySurfaceCaretTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var value by mutableStateOf("")

    private fun show(
        initial: String,
        maxLength: Int? = null,
        secret: Boolean = false,
    ) {
        value = initial
        rule.setContent {
            TestTheme {
                TextEntrySurface(
                    title = "Address",
                    value = value,
                    onValueChange = { value = it },
                    onSubmit = {},
                    onCancel = {},
                    submitLabel = "Save",
                    singleLine = true,
                    maxLength = maxLength,
                    secret = secret,
                )
            }
        }
    }

    private fun caret(): Int =
        rule
            .onNode(hasSetTextAction())
            .fetchSemanticsNode()
            .config[SemanticsProperties.TextSelectionRange]
            .start

    @Test
    fun `a pre-filled field shows its start until it is focused, then has its caret at the end`() {
        show("192.168.1.20")
        rule.waitForIdle()
        assertEquals(0, caret())

        rule.onNode(hasSetTextAction()).requestFocus()
        rule.waitForIdle()
        assertEquals("192.168.1.20".length, caret())
    }

    @Test
    fun `typing goes on from the caret, and a value set from outside puts the caret at its end`() {
        show("gpu")
        rule.onNode(hasSetTextAction()).performTextInput("s")
        rule.waitForIdle()
        assertEquals("gpus", value)
        assertEquals(4, caret())

        value = "transcoding"
        rule.waitForIdle()
        assertEquals("transcoding".length, caret())
    }

    @Test
    fun `input past the cap is dropped whole and the caret stays at the end of what was kept`() {
        show("abc", maxLength = 5)
        rule.onNode(hasSetTextAction()).requestFocus()

        rule.onNode(hasSetTextAction()).performTextInput("defg")
        rule.waitForIdle()

        assertEquals("abc", value)
        assertEquals(3, caret())
    }

    @Test
    fun `a secret field has its caret at the end of its value once focused`() {
        show("s3cret-key", secret = true)
        rule.onNode(hasSetTextAction()).requestFocus()
        rule.waitForIdle()

        assertEquals("s3cret-key".length, caret())
    }
}
