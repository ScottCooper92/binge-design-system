package com.binge.designsystem.component

import androidx.activity.ComponentDialog
import androidx.compose.material3.Text
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

/** A message dialog has one action, which dismisses it once, and a body too long for it scrolls. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeMessageDialogTest {
    @get:Rule
    val rule = createComposeRule()

    private var dismissed = 0

    private fun show(message: String) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeMessageDialogContent(title = "Title", message = message, onDismiss = { dismissed++ })
            }
        }
    }

    @Test
    fun `the one button dismisses it once, labelled OK by default`() {
        show("Short.")

        rule.onNodeWithText("OK").performClick()

        assertEquals(1, dismissed)
    }

    private fun showInWindow(dismissible: Boolean) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeMessageDialog(title = "Title", message = "Body", onDismiss = { dismissed++ }, dismissible = dismissible)
            }
        }
        rule.waitForIdle()
    }

    private fun pressBack() {
        rule.runOnIdle { (ShadowDialog.getLatestDialog() as ComponentDialog).onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun `Back dismisses a dialog by default, and a tap outside is allowed to`() {
        showInWindow(dismissible = true)

        pressBack()

        assertEquals(1, dismissed)
        assertEquals(true, shadowOf(ShadowDialog.getLatestDialog()).isCancelableOnTouchOutside)
    }

    @Test
    fun `a dialog that is not dismissible ignores Back, and a tap outside cannot cancel it`() {
        showInWindow(dismissible = false)

        pressBack()

        assertEquals(0, dismissed)
        assertEquals(false, shadowOf(ShadowDialog.getLatestDialog()).isCancelableOnTouchOutside)
    }

    @Test
    fun `the button still closes a dialog that is not dismissible, once`() {
        showInWindow(dismissible = false)

        rule.onNodeWithText("OK").performClick()

        assertEquals(1, dismissed)
    }

    @Test
    fun `a long body scrolls`() {
        show((1..60).joinToString(" ") { "A long line of explanation number $it." })

        rule.onNode(hasScrollAction()).assertExists()
    }

    @Test
    fun `extra content sits in the body, its button works, and OK still dismisses once`() {
        var copied = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeMessageDialogContent(
                    title = "Title",
                    message = "Body",
                    onDismiss = { dismissed++ },
                    extraContent = {
                        Text("the-key")
                        BingeOutlinedButton(label = "Copy", onClick = { copied++ })
                    },
                )
            }
        }

        rule.onNodeWithText("the-key").assertExists()
        rule.onNodeWithText("Copy").performClick()
        assertEquals(0, dismissed)
        assertEquals(1, copied)
        rule.onNodeWithText("OK").performClick()
        assertEquals(1, dismissed)
    }

    @Test
    fun `extra content in a dialog that is not dismissible does not close it, and Back still does nothing`() {
        var copied = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeMessageDialog(
                    title = "Title",
                    message = "Body",
                    onDismiss = { dismissed++ },
                    dismissible = false,
                    extraContent = { BingeOutlinedButton(label = "Copy", onClick = { copied++ }) },
                )
            }
        }
        rule.waitForIdle()

        rule.onNodeWithText("Copy").performClick()
        pressBack()

        assertEquals(1, copied)
        assertEquals(0, dismissed)
    }
}
