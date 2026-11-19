package com.binge.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The vote count under a rating is formatted in the locale the string resources resolved to, so the number and the word
 * beside it agree: a shipped language (Spanish) reads in that language, and one the app does not ship (Arabic) falls
 * back to the default language for both.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class RatingSubLabelLocaleTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show() {
        rule.setContent { BingeExpressiveTheme(dynamicColor = false) { Text(ratingSubLabel(1300)) } }
        rule.waitForIdle()
    }

    @Config(qualifiers = "ar")
    @Test
    fun `an unshipped language falls back to the default language for the number and the word`() {
        show()

        rule.onNodeWithText("1.3k votes").assertExists()
    }

    @Config(qualifiers = "es")
    @Test
    fun `a shipped language reads in that language`() {
        show()

        rule.onNodeWithText("1,3 mil votos").assertExists()
    }

    @Config(qualifiers = "es-rMX")
    @Test
    fun `a shipped language in another region reads in that language`() {
        show()

        rule.onNodeWithText("1,3 mil votos").assertExists()
    }

    @Config(qualifiers = "en")
    @Test
    fun `the default language reads as before`() {
        show()

        rule.onNodeWithText("1.3k votes").assertExists()
    }
}
