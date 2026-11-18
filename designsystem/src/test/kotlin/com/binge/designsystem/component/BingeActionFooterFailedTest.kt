package com.binge.designsystem.component

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A failed [BingeActionFooter] announces its reason as an error and still hands the tap to its caller; an ordinary or a
 * destructive one carries no error semantics, since only a send that did not go through is an error.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeActionFooterFailedTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var clicks = 0

    private fun show(
        label: String = "Couldn't reach your server",
        failed: Boolean = false,
        destructive: Boolean = false,
    ) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeActionFooter(label = label, onClick = { clicks++ }, failed = failed, destructive = destructive)
            }
        }
    }

    private fun error(label: String) =
        rule
            .onNodeWithText(label)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Error)

    @Test
    fun `a failed footer announces its reason as an error`() {
        show(failed = true)

        assertEquals("Couldn't reach your server", error("Couldn't reach your server"))
    }

    @Test
    fun `a failed footer still reports its tap to the caller`() {
        show(failed = true)

        rule.onNodeWithText("Couldn't reach your server").assertHasClickAction().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `an ordinary footer carries no error semantics`() {
        show(label = "Send")

        assertNull(error("Send"))
    }

    @Test
    fun `a destructive footer is not an error`() {
        show(label = "Decline request", destructive = true)

        assertNull(error("Decline request"))
    }
}
