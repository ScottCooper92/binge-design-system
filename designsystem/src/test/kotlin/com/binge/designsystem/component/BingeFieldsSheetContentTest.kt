package com.binge.designsystem.component

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** [BingeFieldsSheetContent] submits and cancels through its two buttons, gates submit, locks Cancel while busy and shows the error. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFieldsSheetContentTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(
        submitEnabled: Boolean = true,
        submitting: Boolean = false,
        error: String? = null,
        onSubmit: () -> Unit = {},
        onCancel: () -> Unit = {},
    ) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeFieldsSheetContent(
                    title = "Add",
                    body = "Fill these in.",
                    submitLabel = "Create",
                    onSubmit = onSubmit,
                    onCancel = onCancel,
                    submitting = submitting,
                    error = error,
                    submitEnabled = submitEnabled,
                ) { androidx.compose.material3.Text("A field") }
            }
        }
    }

    private fun button(label: String) = rule.onNode(hasClickAction() and hasText(label))

    @Test
    fun `submit and cancel call their callbacks`() {
        var submits = 0
        var cancels = 0
        show(onSubmit = { submits++ }, onCancel = { cancels++ })

        button("Create").performClick()
        button("Cancel").performClick()

        assertEquals(1, submits)
        assertEquals(1, cancels)
    }

    @Test
    fun `submit is off until the caller allows it`() {
        show(submitEnabled = false)

        button("Create").assertIsNotEnabled()
        button("Cancel").assertIsEnabled()
    }

    @Test
    fun `cancel is locked while submitting`() {
        show(submitting = true)

        button("Cancel").assertIsNotEnabled()
    }

    @Test
    fun `a failed submit shows its reason and keeps the fields`() {
        show(error = "Could not reach it.")

        rule.onNodeWithText("Could not reach it.").assertExists()
        rule.onNodeWithText("A field").assertExists()
    }
}
