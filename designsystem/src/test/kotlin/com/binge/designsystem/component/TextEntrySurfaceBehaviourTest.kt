package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TextEntrySurfaceBehaviourTest {
    @get:Rule
    val rule = createComposeRule()

    private var current = ""

    private fun show(
        initial: String = "",
        maxLength: Int? = null,
        error: String? = null,
        isSubmitting: Boolean = false,
    ) = rule.setContent {
        var value by remember { mutableStateOf(initial) }
        current = value
        BingeExpressiveTheme(dynamicColor = false) {
            TextEntrySurface(
                title = "Note",
                value = value,
                onValueChange = { value = it },
                onSubmit = {},
                onCancel = {},
                submitLabel = "Save",
                maxLength = maxLength,
                error = error,
                isSubmitting = isSubmitting,
            )
        }
    }

    private fun isButton() = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    @Test
    fun `input past the cap is rejected whole, and input within it is taken`() {
        show(initial = "abc", maxLength = 5)

        rule.onNode(hasSetTextAction()).performTextInput("defg")
        rule.waitForIdle()
        assertEquals("abc", current)

        rule.onNode(hasSetTextAction()).performTextInput("de")
        rule.waitForIdle()
        assertEquals("abcde", current)
    }

    @Test
    fun `the counter shows the length against the cap`() {
        show(initial = "abc", maxLength = 5)

        rule.onNodeWithText("3 / 5").assertExists()
    }

    @Test
    fun `an error replaces the counter`() {
        show(initial = "abc", maxLength = 5, error = "Too short")

        rule.onNodeWithText("Too short").assertExists()
        rule.onNodeWithText("3 / 5").assertDoesNotExist()
    }

    @Test
    fun `no cap means no counter`() {
        show(initial = "abc")

        rule.onAllNodes(hasText("/", substring = true)).assertCountEquals(0)
    }

    @Test
    fun `submit is enabled with text and the field is idle`() {
        show(initial = "abc")

        rule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun `while submitting, both buttons and the field are disabled`() {
        show(initial = "abc", isSubmitting = true)

        rule.onNodeWithText("Cancel").assertIsNotEnabled()
        rule.onAllNodes(isButton() and isNotEnabled()).assertCountEquals(2)
        rule.onNode(hasText("abc")).assertIsNotEnabled()
    }
}
