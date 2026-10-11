package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
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

private const val LABEL = "Allow plain HTTP"
private const val EFFECT = "Connects without encryption"

/** The one-line second line an option row, a checkbox row and a side-sheet row can carry, and that it leaves OK alone. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvRowSupportingTextTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    @Test
    fun `an option row shows its supporting line and still commits on OK`() {
        var clicks = 0
        composeTestRule.setContent {
            BingeTvTheme {
                TvOptionRow(label = LABEL, selected = false, onClick = { clicks++ }, supportingText = EFFECT)
            }
        }
        composeTestRule.onNodeWithText(EFFECT).assertIsDisplayed()

        composeTestRule.onNodeWithText(LABEL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(LABEL).performKeyInput { pressKey(Key.DirectionCenter) }

        assertEquals(1, clicks)
    }

    @Test
    fun `a checkbox row shows its supporting line and still flips on OK`() {
        val checks = mutableListOf<Boolean>()
        composeTestRule.setContent {
            BingeTvTheme {
                TvCheckboxRow(label = LABEL, checked = false, onCheckedChange = { checks += it }, supportingText = EFFECT)
            }
        }
        composeTestRule.onNodeWithText(EFFECT).assertIsDisplayed()

        composeTestRule.onNodeWithText(LABEL).requestFocus()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(LABEL).performKeyInput { pressKey(Key.DirectionCenter) }

        assertEquals(listOf(true), checks)
    }

    @Test
    fun `a side-sheet row shows its supporting line`() {
        composeTestRule.setContent {
            BingeTvTheme { TvSideSheetRow(label = LABEL, supportingText = EFFECT, onClick = {}) }
        }

        composeTestRule.onNodeWithText(EFFECT).assertIsDisplayed()
    }
}
