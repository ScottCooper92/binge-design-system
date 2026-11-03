package com.binge.designsystem.component

import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

/**
 * Back closes the dialog whether or not a create is in flight. Mid-submit it stops the wait for the create→add round
 * trip, not the request, which the caller leaves running so a create that lands still lands.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CreateListDialogDismissTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    private fun backPressDismisses(isSubmitting: Boolean): Int {
        var dismissed = 0
        composeTestRule.setContent {
            TestTheme {
                CreateListDialog(onDismissRequest = { dismissed++ }, onConfirm = {}, isSubmitting = isSubmitting)
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
    fun `back dismisses the dialog mid-submit`() {
        assertEquals(1, backPressDismisses(isSubmitting = true))
    }
}
