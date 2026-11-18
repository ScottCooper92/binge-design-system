package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A [LabelledIconButton] is one button node: it reads as its label, or as its description when it has one, takes the click on the
 * circle and on the label alike, and takes no click when it is disabled. The label is not an item of its own.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class LabelledIconButtonTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var clicks = 0

    private fun show(enabled: Boolean = true, contentDescription: String? = null) {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                LabelledIconButton(
                    icon = Icons.Filled.Share,
                    label = "Share",
                    onClick = { clicks++ },
                    enabled = enabled,
                    contentDescription = contentDescription,
                )
            }
        }
    }

    @Test
    fun `the button is one node reading its label, with the button role`() {
        show()

        val node = rule
            .onNodeWithContentDescription("Share")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assertHasClickAction()
            .fetchSemanticsNode()
        assertEquals(listOf("Share"), node.config.getOrNull(SemanticsProperties.ContentDescription))
        // The label is not announced as an item of its own.
        assertEquals(null, node.config.getOrNull(SemanticsProperties.Text))
    }

    @Test
    fun `a description replaces the label for a screen reader`() {
        show(contentDescription = "Share this title")

        val node = rule.onNodeWithContentDescription("Share this title").fetchSemanticsNode()
        assertEquals(listOf("Share this title"), node.config.getOrNull(SemanticsProperties.ContentDescription))
    }

    @Test
    fun `a click reports once`() {
        show()

        rule.onNodeWithContentDescription("Share").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a disabled button is disabled and takes no click`() {
        show(enabled = false)

        rule.onNodeWithContentDescription("Share").assertIsNotEnabled().performClick()

        assertEquals(0, clicks)
    }
}
