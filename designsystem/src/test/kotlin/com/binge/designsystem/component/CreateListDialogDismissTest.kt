package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

/**
 * The caller keeps the dialog open for the whole create→add round trip. A back press that dismissed
 * it mid-submit would close it with the request still running, so every caller would have to guard
 * [CreateListDialog]'s `onDismiss` itself (#227).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CreateListDialogDismissTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun backPressDismisses(isSubmitting: Boolean): Int {
        var dismissed = 0
        composeTestRule.setContent {
            TestTheme {
                CreateListDialog(onDismiss = { dismissed++ }, onConfirm = {}, isSubmitting = isSubmitting)
            }
        }
        composeTestRule.waitForIdle()
        // The window Compose's AlertDialog opens. Its back press goes through the same
        // onDismissRequest a tap outside does.
        val dialog = checkNotNull(ShadowDialog.getLatestDialog()) { "No dialog was shown" }
        composeTestRule.runOnUiThread {
            @Suppress("DEPRECATION")
            dialog.onBackPressed()
        }
        composeTestRule.waitForIdle()
        return dismissed
    }

    @Test
    fun `back dismisses the dialog when nothing is submitting`() {
        assertEquals(1, backPressDismisses(isSubmitting = false))
    }

    @Test
    fun `back does not dismiss the dialog mid-submit`() {
        assertEquals(0, backPressDismisses(isSubmitting = true))
    }
}
