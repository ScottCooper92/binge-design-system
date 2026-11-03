package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TextEntrySurfaceSecretTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun text(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    private fun show(secret: Boolean) =
        rule.setContent {
            TestTheme {
                var value by remember { mutableStateOf("key") }
                TextEntrySurface(
                    title = "Key",
                    value = value,
                    onValueChange = { value = it },
                    onSubmit = {},
                    onCancel = {},
                    submitLabel = "Save",
                    singleLine = true,
                    secret = secret,
                )
            }
        }

    @Test
    fun `a secret surface draws the eye and its tap swaps it`() {
        show(secret = true)
        rule.onNodeWithContentDescription(text(R.string.field_secret_show)).performClick()
        rule.onNodeWithContentDescription(text(R.string.field_secret_hide)).assertExists()
    }

    @Test
    fun `a surface that is not secret draws no eye`() {
        show(secret = false)
        rule.onNodeWithContentDescription(text(R.string.field_secret_show)).assertDoesNotExist()
    }
}
