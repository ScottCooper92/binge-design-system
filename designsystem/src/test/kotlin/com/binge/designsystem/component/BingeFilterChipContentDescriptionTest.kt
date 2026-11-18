package com.binge.designsystem.component

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
 * A chip's [FilterChipItem.contentDescription] reaches the chip's semantics through [BingeFilterChipRow]: when set it is
 * what a screen reader announces in place of the label, and when null the chip is read as its label, as before. The
 * chip is one merged node, so its content description is the whole announcement and its text is the fallback.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeFilterChipContentDescriptionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show() {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeFilterChipRow(
                    items = listOf(
                        FilterChipItem("Warnings", count = 4, contentDescription = "Warnings and errors"),
                        FilterChipItem("Info", count = 9),
                    ),
                    selectedIndex = 0,
                    onSelect = {},
                )
            }
        }
    }

    @Test
    fun `a description replaces the label as the chip's announcement`() {
        show()

        rule
            .onNodeWithContentDescription("Warnings and errors")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Selected, true))
        // The label is not announced as a description of its own: only the one chip carries one.
        rule.onAllNodesWithContentDescription("Warnings").assertCountEquals(0)
        assertEquals(
            listOf("Warnings and errors"),
            rule
                .onNodeWithContentDescription(
                    "Warnings and errors",
                ).fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.ContentDescription),
        )
    }

    @Test
    fun `without a description the chip is read as its label`() {
        show()

        val info = rule.onNodeWithText("Info").fetchSemanticsNode()
        assertNull(info.config.getOrNull(SemanticsProperties.ContentDescription))
        assertEquals(true, info.config.getOrNull(SemanticsProperties.Text)?.any { it.text == "Info" })
    }
}
