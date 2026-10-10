package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The plate's headline is the one heading, as `MessageScreen`'s is on the phone (#478). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvMessagePlateHeadingTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    @Test
    fun `the headline is the one heading, and the body is not`() {
        rule.setContent { BingeTvTheme { TvMessagePlate(headline = "Nothing here", body = "Add something to see it.") } }

        rule.onAllNodes(isHeading()).assertCountEquals(1)
        rule.onAllNodes(isHeading() and hasText("Nothing here")).assertCountEquals(1)
    }

    @Test
    fun `a plate without a headline has no heading`() {
        rule.setContent { BingeTvTheme { TvMessagePlate(body = "Add something to see it.") } }

        rule.onAllNodes(isHeading()).assertCountEquals(0)
    }
}
