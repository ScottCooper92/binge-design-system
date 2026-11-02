package com.binge.designsystem.component

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
import org.robolectric.annotation.Config

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

    @Test
    fun `a long body scrolls`() {
        show((1..60).joinToString(" ") { "A long line of explanation number $it." })

        rule.onNode(hasScrollAction()).assertExists()
    }
}
