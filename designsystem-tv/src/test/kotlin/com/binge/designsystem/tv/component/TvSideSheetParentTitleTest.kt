package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.onAllNodesWithText
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
 * A page opened over another carries an app bar's back: an arrow and the name of the page Back returns to, in every style, no
 * control and no focus of its own. The root has none.
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

    private fun backNamesSettings() = composeTestRule.onAllNodesWithText("Settings")

    @Test
    fun `a page opened over another names its parent in the back, and the root has none`() {
        show(TvStyle.Monochrome)
        backNamesSettings().assertCountEquals(1)
        composeTestRule.onNodeWithText("Alpha page").assertDoesNotExist()

        press(Key.DirectionCenter)

        composeTestRule.onNodeWithText("Alpha page").assertExists()
        backNamesSettings().assertCountEquals(1)
        composeTestRule.onNodeWithText("First").assertIsFocused()
    }

    @Test
    fun `going back drops the back from the page`() {
        show(TvStyle.Monochrome)
        press(Key.DirectionCenter)

        press(Key.DirectionLeft)

        composeTestRule.onNodeWithText("Alpha page").assertDoesNotExist()
        backNamesSettings().assertCountEquals(1)
        composeTestRule.onNodeWithText("Alpha").assertIsFocused()
    }

    @Test
    fun `the accent style draws the back too`() {
        show(TvStyle.Accent)

        press(Key.DirectionCenter)

        composeTestRule.onNodeWithText("Alpha page").assertExists()
        backNamesSettings().assertCountEquals(1)
    }
}
