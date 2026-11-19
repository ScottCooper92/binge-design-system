package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val SEND = "Send"
private const val PLACEHOLDER = "Add a comment"
private const val LONG_TEXT = "one\ntwo\nthree\nfour\nfive\nsix\nseven\neight\nnine\nten"
private const val WINDOW_WIDTH_DP = 360
private const val WINDOW_HEIGHT_DP = 640
private val KEYBOARD = 300.dp

/**
 * The floating composer's contract: Send follows the text, a send keeps the field, the card stays inside the window with a long
 * comment and a large font, it sits above the keyboard, and it reports the height it takes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w${WINDOW_WIDTH_DP}dp-h${WINDOW_HEIGHT_DP}dp")
class BingeFloatingComposerTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var text by mutableStateOf("")
    private var sent = 0
    private var occupied: Dp = Dp.Unspecified

    private fun show(sending: Boolean = false, insets: WindowInsets = WindowInsets(0, 0, 0, 0)) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                Box(Modifier.width(WINDOW_WIDTH_DP.dp).height(WINDOW_HEIGHT_DP.dp).testTag("window")) {
                    BingeFloatingComposer(
                        value = text,
                        onValueChange = { text = it },
                        onSend = {
                            sent++
                            text = ""
                        },
                        placeholder = PLACEHOLDER,
                        sendDescription = SEND,
                        sending = sending,
                        windowInsets = insets,
                        onOccupiedHeightChange = { occupied = it },
                        modifier = Modifier.fillMaxWidth().testTag("composer"),
                    )
                }
            }
        }
    }

    @Test
    fun `send is disabled until there is text`() {
        show()

        rule.onNodeWithContentDescription(SEND).assertIsNotEnabled()
        rule.onNode(hasSetTextAction()).performTextInput("hello")
        rule.onNodeWithContentDescription(SEND).assertIsEnabled()
    }

    @Test
    fun `a send clears the field and hands the tap to the caller once`() {
        show()
        rule.onNode(hasSetTextAction()).performTextInput("hello")

        rule.onNodeWithContentDescription(SEND).performClick()

        rule.runOnIdle {
            assertEquals(1, sent)
            assertEquals("", text)
        }
        rule.onNodeWithText(PLACEHOLDER).assertExists()
    }

    @Test
    fun `a send in flight keeps send enabled and the text in the field`() {
        text = "hello"
        show(sending = true)

        rule.onNodeWithContentDescription(SEND).assertIsEnabled()
        rule.onNodeWithText(PLACEHOLDER).assertDoesNotExist()
    }

    @Test
    fun `a long comment stays inside the window`() {
        text = LONG_TEXT
        show()

        val window = rule.onNodeWithTag("window").getUnclippedBoundsInRoot()
        val composer = rule.onNodeWithTag("composer").getUnclippedBoundsInRoot()
        assertTrue("the card ends inside the window", composer.bottom <= window.bottom)
        assertTrue("the card starts inside the window", composer.top >= window.top)
        assertTrue("five lines is a small part of the window", composer.height < window.height / 2)
    }

    @Test
    fun `the card sits above the keyboard and reports the height it takes`() {
        show(insets = WindowInsets(bottom = KEYBOARD))

        val window = rule.onNodeWithTag("window").getUnclippedBoundsInRoot()
        val composer = rule.onNodeWithTag("composer").getUnclippedBoundsInRoot()
        rule.runOnIdle {
            assertEquals("the reported height is the card plus the keyboard", composer.height.value, occupied.value, 0.5f)
            assertTrue("the keyboard is inside the reported height", occupied >= KEYBOARD)
        }
        val send = rule.onNodeWithContentDescription(SEND).getUnclippedBoundsInRoot()
        assertTrue("send is above the keyboard", send.bottom <= window.bottom - KEYBOARD)
    }
}
