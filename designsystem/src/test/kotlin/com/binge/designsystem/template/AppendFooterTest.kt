package com.binge.designsystem.template

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
class AppendFooterTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `a failed footer shows its label and retries when tapped`() {
        var retries = 0
        rule.setContent { TestTheme { AppendFooter(failed = true, failedLabel = "Could not load more", onRetry = { retries++ }) } }
        rule.onNodeWithText("Could not load more").assertIsDisplayed()
        rule.onNodeWithText("Could not load more").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `a loading footer shows no label`() {
        rule.setContent { TestTheme { AppendFooter(failed = false, failedLabel = "Could not load more", onRetry = {}) } }
        rule.onNodeWithText("Could not load more").assertDoesNotExist()
    }
}
