package com.binge.designsystem.template

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SAVING = "Saving…"
private const val SAVED = "Saved"
private const val FAILED = "Couldn't save your change"
private const val RETRY = "Retry"

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FormScreenSaveStatusTest {
    @get:Rule
    val rule = createComposeRule()

    private var status by mutableStateOf<FormSaveStatus>(FormSaveStatus.Idle)

    private fun setForm() {
        rule.setContent {
            TestTheme {
                FormScreen(title = "General", saveStatus = status) { Text("Fields") }
            }
        }
    }

    @Test
    fun `an idle form draws no status`() {
        setForm()

        rule.onNodeWithText(SAVING).assertDoesNotExist()
        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    @Test
    fun `saving and saved show in the bar`() {
        setForm()

        status = FormSaveStatus.Saving
        rule.onNodeWithText(SAVING).assertIsDisplayed()

        status = FormSaveStatus.Saved
        rule.onNodeWithText(SAVED).assertIsDisplayed()
    }

    @Test
    fun `a failed save shows a snackbar whose Retry calls back`() {
        var retries = 0
        status = FormSaveStatus.Failed(onRetry = { retries++ })
        setForm()

        rule.onNodeWithText(FAILED).assertIsDisplayed()
        rule.onNodeWithText(RETRY).performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `the failure snackbar stays until a save succeeds`() {
        status = FormSaveStatus.Failed(onRetry = {})
        setForm()
        rule.onNodeWithText(FAILED).assertIsDisplayed()

        // Long past a short snackbar's time: it is indefinite.
        rule.mainClock.advanceTimeBy(60_000)
        rule.onNodeWithText(FAILED).assertIsDisplayed()

        status = FormSaveStatus.Saved
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithText(FAILED).assertDoesNotExist()
    }

    @Test
    fun `a retry that fails at once shows the snackbar again`() {
        var retries = 0
        status = FormSaveStatus.Failed(onRetry = { retries++ })
        setForm()

        // The retry fails at once, so the status never leaves Failed and the effect is not restarted.
        rule.onNodeWithText(RETRY).performClick()
        rule.waitForIdle()

        assertEquals(1, retries)
        rule.onNodeWithText(FAILED).assertIsDisplayed()
    }

    @Test
    fun `a message on the host does not take the failure away for good`() {
        val host = SnackbarHostState()
        status = FormSaveStatus.Failed(onRetry = {})
        rule.setContent {
            TestTheme {
                FormScreen(title = "General", saveStatus = status, snackbarHostState = host) { Text("Fields") }
            }
        }
        rule.onNodeWithText(FAILED).assertIsDisplayed()

        // Showing a message dismisses the one showing; the failure comes back when it has gone.
        rule.runOnIdle { host.currentSnackbarData?.dismiss() }
        rule.waitForIdle()

        rule.onNodeWithText(FAILED).assertIsDisplayed()
    }

    @Test
    fun `retry calls the latest callback`() {
        val sent = mutableListOf<String>()
        var latest by mutableStateOf("X")
        rule.setContent {
            TestTheme {
                val current = latest
                FormScreen(title = "General", saveStatus = FormSaveStatus.Failed(onRetry = { sent += current })) { Text("Fields") }
            }
        }

        // A second failed save changes what Retry must send while the status stays Failed.
        latest = "Y"
        rule.waitForIdle()
        rule.onNodeWithText(RETRY).performClick()

        assertEquals(listOf("Y"), sent)
    }
}
