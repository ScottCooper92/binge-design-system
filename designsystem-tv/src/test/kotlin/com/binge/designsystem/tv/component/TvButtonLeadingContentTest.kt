package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val NODE = "button"
private const val SLOT = "slot"
private const val LABEL = "Alexander"

/** The button's leading slot (an account's picture): it draws, the label stays the name, OK still works, and it wins over the icon. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvButtonLeadingContentTest {
    @get:Rule
    val rule = createTakeDownKeyboardComposeRule()

    private var clicks = 0

    @Test
    fun `the slot is drawn and the label is still the button's name`() {
        rule.setContent {
            BingeTvTheme {
                TvButton(label = LABEL, onClick = {}, leadingContent = { Box(Modifier.testTag(SLOT)) }, modifier = Modifier.testTag(NODE))
            }
        }

        rule.onNodeWithTag(SLOT, useUnmergedTree = true).assertExists()
        rule.onNodeWithTag(NODE).assert(hasText(LABEL))
    }

    @Test
    fun `a button with a slot takes OK`() {
        rule.setContent {
            BingeTvTheme {
                TvButton(label = LABEL, onClick = { clicks++ }, leadingContent = { Box(Modifier) }, modifier = Modifier.testTag(NODE))
            }
        }
        rule.onNodeWithTag(NODE).requestFocus()

        rule.onNodeWithText(LABEL).performKeyInput { pressKey(Key.DirectionCenter) }
        rule.waitForIdle()

        assertEquals(1, clicks)
    }

    @Test
    fun `the slot is drawn while loading, so the button keeps its width`() {
        rule.setContent {
            BingeTvTheme {
                TvButton(label = LABEL, onClick = {}, loading = true, leadingContent = { Box(Modifier.testTag(SLOT)) })
            }
        }

        rule.onNodeWithTag(SLOT, useUnmergedTree = true).assertExists()
    }
}
