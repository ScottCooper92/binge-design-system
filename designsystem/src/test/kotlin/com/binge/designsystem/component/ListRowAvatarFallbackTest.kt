package com.binge.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** A person with no photo shows their initials, or a person glyph with no name, never the generic image glyph. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
class ListRowAvatarFallbackTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(name: String?) {
        rule.setContent { BingeExpressiveTheme(dynamicColor = false) { ListRowAvatar(imageUrl = null, name = name) } }
        rule.waitForIdle()
    }

    @Test
    fun `a person with no photo shows their initials`() {
        show("Greta Lee")
        rule.onNodeWithText("GL").assertIsDisplayed()
    }

    @Test
    fun `a single name shows one initial`() {
        show("Zendaya")
        rule.onNodeWithText("Z").assertIsDisplayed()
    }

    @Test
    fun `no name draws no initials`() {
        show(null)
        assertEquals(0, rule.onAllNodesWithText("", substring = true).fetchSemanticsNodes().size)
    }

    @Test
    fun `a blank name draws no initials`() {
        show("   ")
        assertEquals(0, rule.onAllNodesWithText("", substring = true).fetchSemanticsNodes().size)
    }
}
