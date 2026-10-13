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
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetStackTest {
    @get:Rule
    val composeTestRule = createKeyboardComposeRule()

    private var dismissed = 0

    private fun show() {
        composeTestRule.setContent {
            BingeTvTheme {
                TvSideSheetStack(root = "list", onDismissRequest = { dismissed++ }) { nav ->
                    when (nav.page) {
                        "list" -> {
                            TvSideSheetTitle("Settings")
                            TvSideSheetRow("Alpha", onClick = { nav.push("alpha", openedBy = "alpha") }, modifier = nav.row("alpha"))
                            TvSideSheetRow("Beta", onClick = { nav.push("beta", openedBy = "beta") }, modifier = nav.row("beta"))
                            TvSideSheetRow("Gamma", onClick = { nav.push("gamma", openedBy = "gamma") }, modifier = nav.row("gamma"))
                        }
                        "gamma" -> {
                            TvSideSheetTitle("Gamma page")
                            TvSideSheetRow("Keep", onClick = {}, modifier = nav.row("keep"))
                            TvSideSheetRow("Cancel", onClick = nav::pop, modifier = nav.row("cancel"))
                            nav.arriveAt("cancel")
                        }
                        else -> {
                            TvSideSheetTitle("${nav.page} page")
                            TvSideSheetRow("First", onClick = {}, modifier = nav.row("first"))
                        }
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
    fun `the root's first row takes focus when the sheet opens`() {
        show()

        composeTestRule.onNodeWithText("Alpha").assertIsFocused()
    }

    @Test
    fun `opening a page puts focus on its first row, and back returns it to the row that opened the page`() {
        show()
        press(Key.DirectionDown) // Beta
        composeTestRule.onNodeWithText("Beta").assertIsFocused()
        press(Key.DirectionCenter)
        composeTestRule.onNodeWithText("beta page").assertExists()
        composeTestRule.onNodeWithText("First").assertIsFocused()
        composeTestRule.onNodeWithText("Alpha").assertDoesNotExist()

        press(Key.DirectionLeft)

        composeTestRule.onNodeWithText("Settings").assertExists()
        composeTestRule.onNodeWithText("Beta").assertIsFocused()
        assertEquals(0, dismissed)
    }

    @Test
    fun `a page can name another row to arrive at, and its own pop returns to the row that opened it`() {
        show()
        press(Key.DirectionDown)
        press(Key.DirectionDown) // Gamma
        press(Key.DirectionCenter)
        composeTestRule.onNodeWithText("Cancel").assertIsFocused()

        press(Key.DirectionCenter)

        composeTestRule.onNodeWithText("Gamma").assertIsFocused()
    }

    @Test
    fun `the start key at the root closes the sheet rather than going back`() {
        show()

        press(Key.DirectionLeft)

        assertEquals(1, dismissed)
    }
}
