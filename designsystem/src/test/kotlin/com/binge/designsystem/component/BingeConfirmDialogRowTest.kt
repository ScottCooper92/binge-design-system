package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalog.BingeConfirmDialogSwitchOffSample
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
    val rule = createComposeRule()

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
    fun `the switch changes the confirm label and nothing else`() {
        rule.setContent { BingeConfirmDialogSwitchOffSample() }
        rule.onNodeWithText("Remove request").assertIsDisplayed()
        rule.onNodeWithText("Also delete the files from the server").performClick()
        rule.onNodeWithText("Remove and delete files").assertIsDisplayed()
        rule.onNodeWithText("Cancel").assertIsDisplayed()
        assertEquals(0, rule.onAllNodesWithTextCount("Remove request"))
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(
            androidx.compose.ui.test
                .hasText(text),
        ).fetchSemanticsNodes().size
}
