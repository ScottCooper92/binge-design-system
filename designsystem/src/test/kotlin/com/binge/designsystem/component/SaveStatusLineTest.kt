package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.template.FormSaveStatus
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class SaveStatusLineTest {
    @get:Rule
    val rule = createComposeRule()

    private var status by mutableStateOf<FormSaveStatus>(FormSaveStatus.Idle)
    private var retries = 0

    private fun setLine() = rule.setContent { TestTheme { SaveStatusLine(status) } }

    @Test
    fun `an idle line draws nothing`() {
        setLine()

        rule.onNodeWithText("Saving…").assertDoesNotExist()
        rule.onNodeWithText("Saved").assertDoesNotExist()
        rule.onNodeWithText("Retry").assertDoesNotExist()
    }

    @Test
    fun `saving and saved each say so`() {
        setLine()

        status = FormSaveStatus.Saving
        rule.onNodeWithText("Saving…").assertIsDisplayed()
        status = FormSaveStatus.Saved
        rule.onNodeWithText("Saved").assertIsDisplayed()
        rule.onNodeWithText("Saving…").assertDoesNotExist()
    }

    @Test
    fun `a failure says so and Retry sends the change again`() {
        setLine()

        status = FormSaveStatus.Failed(onRetry = { retries++ })
        rule.onNodeWithText("Couldn't save your change").assertIsDisplayed()
        rule.onNodeWithText("Retry").performClick()

        assertEquals(1, retries)
    }
}
