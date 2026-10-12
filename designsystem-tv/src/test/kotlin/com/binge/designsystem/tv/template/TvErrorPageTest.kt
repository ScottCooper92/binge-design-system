package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val PoliteLiveRegion = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

/** [TvErrorPage] is a [TvMessagePage] driven by an [ErrorKind], announced as a whole, with Try again as its lead. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvErrorPageTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `a failure page is announced as one live region and focus lands on Try again`() {
        var retries = 0
        rule.setContent {
            BingeTvTheme { TvErrorPage(kind = ErrorKind.Network, onRetry = { retries++ }, hosting = TvPageHosting.Overlay) }
        }
        rule.waitForIdle()

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
        rule.onNode(hasText("Try again") and isFocusable()).assertIsFocused()
        rule.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test
    fun `a failure with no retry draws no button, and the caller's words stand in for the kind's`() {
        rule.setContent {
            BingeTvTheme {
                TvErrorPage(
                    kind = ErrorKind.Configuration,
                    onRetry = null,
                    hosting = TvPageHosting.Overlay,
                    title = "No API key",
                    message = "Add a key first.",
                )
            }
        }

        rule.onNodeWithText("No API key").assertExists()
        rule.onNodeWithText("Add a key first.").assertExists()
        rule.onAllNodes(hasText("Try again")).assertCountEquals(0)
        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
    }
}
