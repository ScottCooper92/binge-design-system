package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
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

/** A failure plate is announced as a whole, as the phone's `ErrorScreen` is (#526). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvMessagePlateAnnounceTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `an announced plate is one polite live region carrying its headline and body`() {
        rule.setContent { BingeTvTheme { TvMessagePlate(headline = "Nothing here", body = "Add something to see it.", announce = true) } }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
        rule.onNode(PoliteLiveRegion).assertTextContains("Nothing here", substring = true)
        rule.onNode(PoliteLiveRegion).assertTextContains("Add something to see it.", substring = true)
    }

    @Test
    fun `a plate is not a live region unless it is told to announce`() {
        rule.setContent { BingeTvTheme { TvMessagePlate(headline = "Nothing here", body = "Add something to see it.") } }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(0)
    }

    @Test
    fun `a failure plate with no retry is still announced`() {
        rule.setContent { BingeTvTheme { TvErrorPlate(kind = ErrorKind.Network, onRetry = null) } }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
    }

    @Test
    fun `a failure plate's Try again still runs the retry while the plate is announced`() {
        var retries = 0
        rule.setContent { BingeTvTheme { TvErrorPlate(kind = ErrorKind.Network, onRetry = { retries++ }) } }

        rule.onAllNodes(PoliteLiveRegion).assertCountEquals(1)
        rule.onNode(hasClickAction() and hasAnyDescendant(hasText("Try again")), useUnmergedTree = true).performClick()

        assertEquals(1, retries)
    }
}
