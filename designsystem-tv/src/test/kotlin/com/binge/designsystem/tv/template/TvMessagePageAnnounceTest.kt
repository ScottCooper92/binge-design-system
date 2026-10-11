package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val PoliteLiveRegion = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)
private const val HEADLINE = "Can't reach the server"
private const val BODY = "Check the connection and try again."

/** A screen-wide failure on a [TvMessagePage] is announced as a whole when the page is told to (#596). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvMessagePageAnnounceTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `an announced page is one polite live region carrying its headline and body`() {
        rule.setContent {
            BingeTvTheme {
                TvMessagePage(
                    body = BODY,
                    headline = HEADLINE,
                    hosting = TvPageHosting.Overlay,
                    primary = TvPageAction("Try again") {},
                    announce = true,
                )
            }
        }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
        rule.onNode(PoliteLiveRegion).assertTextContains(HEADLINE, substring = true)
        rule.onNode(PoliteLiveRegion).assertTextContains(BODY, substring = true)
    }

    /** With no action, focus rests on the sink and reads nothing, so the live region is the only way the page is heard. */
    @Test
    fun `an announced page with no action is still announced`() {
        rule.setContent {
            BingeTvTheme { TvMessagePage(body = BODY, headline = HEADLINE, hosting = TvPageHosting.Overlay, announce = true) }
        }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
    }

    @Test
    fun `a page is not a live region unless it is told to announce`() {
        rule.setContent { BingeTvTheme { TvMessagePage(body = BODY, headline = HEADLINE, hosting = TvPageHosting.Overlay) } }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(0)
    }
}
