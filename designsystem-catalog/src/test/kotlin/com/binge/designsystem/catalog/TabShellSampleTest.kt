package com.binge.designsystem.catalog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TabShellSampleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `choosing another tab shows its content`() {
        rule.setContent { TabShellSample() }
        rule.onNodeWithText("Movies content").assertIsDisplayed()
        rule.onNodeWithText("Search").performClick()
        rule.waitForIdle()
        rule.mainClock.advanceTimeBy(1_000)
        rule.onNodeWithText("Search content").assertIsDisplayed()
    }
}
