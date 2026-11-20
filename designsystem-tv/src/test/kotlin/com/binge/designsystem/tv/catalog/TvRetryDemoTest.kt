package com.binge.designsystem.tv.catalog

import android.app.Application
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TRY_AGAIN = "Try again"
private const val LOADED = "Loaded. Show the failure again"

/** The failure samples work in the catalog app: Try again leaves the failure, and the button there brings it back. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvRetryDemoTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @Test
    fun `try again on an error plate leaves the failure, and the demo can fail again`() {
        composeTestRule.setContent { BingeTvTheme { TvErrorPlateSample() } }
        composeTestRule.onNodeWithText(TRY_AGAIN).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(LOADED).assertExists()
        composeTestRule.onNodeWithText(LOADED).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasText(TRY_AGAIN)).assertExists()
    }

    @Test
    fun `try again on an error page leaves the failure`() {
        composeTestRule.setContent { BingeTvTheme { TvErrorPageSample() } }
        composeTestRule.onNodeWithText(TRY_AGAIN).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(LOADED).assertExists()
    }

    @Test
    fun `retry on the failed hub row loads its cards in place`() {
        composeTestRule.setContent { BingeTvTheme { TvImmersiveHubFailedRowSample() } }
        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Retry").assertDoesNotExist()
    }
}
