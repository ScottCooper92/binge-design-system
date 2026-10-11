package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MOVIES = "movies"
private const val TV = "tv"
private const val MOVIES_LABEL = "Movies"
private const val TV_LABEL = "TV shows"

/**
 * [TvChoiceRow] is the deliberate opposite of [TvTabRow]: **OK commits, focus does not.** Both render the same pills,
 * so only a test under a real D-pad can tell the two apart.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvChoiceRowTraversalTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private val committed = mutableListOf<String>()

    @Test
    fun `moving focus to the other option commits nothing`() {
        setChoiceRow()
        option(MOVIES_LABEL).requestFocus()
        composeTestRule.waitForIdle()
        option(MOVIES_LABEL).assertIsFocused()

        press(Key.DirectionRight)

        option(TV_LABEL).assertIsFocused()
        assertEquals("Arriving on an option must commit nothing, but committed $committed", emptyList<String>(), committed)
    }

    @Test
    fun `pressing OK on the other option commits it`() {
        setChoiceRow()
        option(TV_LABEL).requestFocus()
        composeTestRule.waitForIdle()
        option(TV_LABEL).assertIsFocused()

        press(Key.DirectionCenter)

        assertEquals(listOf(TV), committed)
    }

    @Test
    fun `pressing OK on the option already chosen commits nothing`() {
        setChoiceRow()
        option(MOVIES_LABEL).requestFocus()
        composeTestRule.waitForIdle()
        option(MOVIES_LABEL).assertIsFocused()

        press(Key.DirectionCenter)

        assertEquals("Re-pressing the chosen option must commit nothing, but committed $committed", emptyList<String>(), committed)
    }

    private fun setChoiceRow() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvChoiceRow(
                    choices = listOf(TvChoiceUi(MOVIES, MOVIES_LABEL), TvChoiceUi(TV, TV_LABEL)),
                    selectedKey = MOVIES,
                    onSelect = { committed += it },
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    /** An option is one clickable node wrapping its label. */
    private fun option(label: String): SemanticsNodeInteraction =
        composeTestRule.onNode(hasClickAction() and hasAnyDescendant(hasText(label)), useUnmergedTree = true)

    private fun press(key: Key) {
        composeTestRule.onNode(isFocused()).performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }
}
