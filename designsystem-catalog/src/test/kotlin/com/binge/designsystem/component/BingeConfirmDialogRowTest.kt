package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalog.BingeConfirmDialogSwitchOffSample
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The dialog's two actions share one row whatever the labels and the font, and a switch in the body drives the confirm. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class BingeConfirmDialogRowTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `long labels at twice the font still sit on one row`() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(360.dp)) {
                        BingeConfirmDialogContent(
                            title = "Remove this request?",
                            message = "The request is removed.",
                            confirmLabel = "Remove it and delete the files from the server",
                            dismissLabel = "Keep the request",
                            onConfirm = {},
                            onDismissRequest = {},
                            destructive = true,
                        )
                    }
                }
            }
        }
        val cancel = rule.onNodeWithText("Keep the request").getUnclippedBoundsInRoot()
        val confirm = rule.onNodeWithText("Remove it and delete the files from the server").getUnclippedBoundsInRoot()
        assertTrue("Cancel and the confirm are on different rows", cancel.top < confirm.bottom && confirm.top < cancel.bottom)
        assertTrue("the confirm sits after Cancel", confirm.left >= cancel.right)
    }

    @Test
    fun `a short confirm keeps one line at twice the font when Cancel leaves room`() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 2f)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(360.dp)) {
                        BingeConfirmDialogContent(
                            title = "Include copies whatever their licence?",
                            message = "These copies may not be licensed for viewing.",
                            confirmLabel = "Include them",
                            onConfirm = {},
                            onDismissRequest = {},
                            destructive = true,
                        )
                    }
                }
            }
        }
        val layouts = mutableListOf<TextLayoutResult>()
        rule
            .onNode(
                hasText("Include them"),
            ).fetchSemanticsNode()
            .config
            .getOrNull(SemanticsActions.GetTextLayoutResult)
            ?.action
            ?.invoke(layouts)

        assertEquals("the confirm wrapped: ${layouts.single().lineCount} lines", 1, layouts.single().lineCount)
        val cancel = rule.onNodeWithText("Cancel").getUnclippedBoundsInRoot()
        val confirm = rule.onNodeWithText("Include them").getUnclippedBoundsInRoot()
        assertTrue("Cancel and the confirm are on different rows", cancel.top < confirm.bottom && confirm.top < cancel.bottom)
    }

    @Test
    fun `the switch changes the confirm label and nothing else`() {
        rule.setContent { BingeConfirmDialogSwitchOffSample() }
        rule.onNodeWithText("Remove request").assertIsDisplayed()
        rule.onNodeWithText("Also delete the files from the server").performClick()
        rule.onNodeWithText("Remove and delete files").assertIsDisplayed()
        rule.onNodeWithText("Cancel").assertIsDisplayed()
        assertEquals(0, rule.onAllNodesWithTextCount("Remove request"))
    }

    @Test
    fun `a screen reader meets the switch, then Cancel, then the confirm`() {
        rule.setContent { BingeConfirmDialogSwitchOffSample() }
        val labels = listOf("Also delete the files from the server", "Cancel", "Remove request")
        val inOrder =
            rule
                .onAllNodes(labels.map { hasText(it) }.reduce { a, b -> a or b })
                .fetchSemanticsNodes()
                .map { node -> labels.first { label -> node.config.getOrNull(SemanticsProperties.Text)?.any { it.text == label } == true } }
        assertEquals(labels, inOrder)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(
            androidx.compose.ui.test
                .hasText(text),
        ).fetchSemanticsNodes().size
}
