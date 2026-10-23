package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.binge.designsystem.testing.createKeyboardComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import com.binge.designsystem.tv.theme.TvStyle
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A page opened over another names the page Back returns to, in the monochrome style only: a small line above its own
 * title, no control and no focus of its own.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetParentTitleTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private fun show(style: TvStyle) {
        composeTestRule.setContent {
            BingeTvTheme(style = style) {
                TvSideSheetStack(root = "list", onDismissRequest = {}) { nav ->
                    if (nav.page == "list") {
                        TvSideSheetTitle("Settings")
                        TvSideSheetRow("Alpha", onClick = { nav.push("alpha", openedBy = "alpha") }, modifier = nav.row("alpha"))
                    } else {
                        TvSideSheetTitle("Alpha page")
                        TvSideSheetRow("First", onClick = {}, modifier = nav.row("first"))
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun press(key: Key) {
        composeTestRule.onRoot().performKeyInput { pressKey(key) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun `a page opened over another names its parent under the monochrome style, and the root names none`() {
        show(TvStyle.Monochrome)
        composeTestRule.onNodeWithText(PARENT_LINE).assertDoesNotExist()

        press(Key.DirectionCenter)

        composeTestRule.onNodeWithText("Alpha page").assertExists()
        composeTestRule.onNodeWithText(PARENT_LINE).assertExists()
        composeTestRule.onNodeWithText("First").assertIsFocused()
    }

    @Test
    fun `going back drops the parent line`() {
        show(TvStyle.Monochrome)
        press(Key.DirectionCenter)

        press(Key.DirectionLeft)

        composeTestRule.onNodeWithText(PARENT_LINE).assertDoesNotExist()
        composeTestRule.onNodeWithText("Alpha").assertIsFocused()
    }

    @Test
    fun `the accent style draws no parent line`() {
        show(TvStyle.Accent)

        press(Key.DirectionCenter)

        composeTestRule.onNodeWithText("Alpha page").assertExists()
        composeTestRule.onNodeWithText(PARENT_LINE).assertDoesNotExist()
    }

    private companion object {
        const val PARENT_LINE = "‹ Settings"
    }
}
