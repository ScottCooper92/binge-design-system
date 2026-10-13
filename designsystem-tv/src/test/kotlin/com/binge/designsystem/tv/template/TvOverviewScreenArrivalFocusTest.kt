package com.binge.designsystem.tv.template

import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocusable
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * **The reader takes focus when it opens.** It is a single focus stop: with no focus owner a D-pad press has no
 * target and falls through to the shell, so arrival goes through the shared offered-until-granted effect rather
 * than a one-shot request that a frame too early would lose.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi")
class TvOverviewScreenArrivalFocusTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `the reader is focused once it opens`() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvOverviewScreen(title = "Title", overview = "Overview", backdropUrl = null, onDismiss = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNode(isFocusable()).assertIsFocused()
    }
}
