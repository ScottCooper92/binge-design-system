package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val LABEL = "Save"
private const val TOLERANCE = 1f

/** A footer given the edge-aware padding lines its button up with a body beside another pane (#354). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeActionFooterSidePaddingTest {
    @get:Rule
    val rule = createComposeRule()

    private fun buttonLeft(innerEdge: PaneEdge?): Dp {
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                CompositionLocalProvider(LocalPaneInnerEdge provides innerEdge) {
                    BingeActionFooter(label = LABEL, onClick = {}, sidePadding = resolvedContentPadding())
                }
            }
        }
        return rule.onNode(hasClickAction()).getBoundsInRoot().left
    }

    @Test
    fun `alone in the window, the button sits the window inset from the edge`() {
        assertEquals(16f, buttonLeft(innerEdge = null).value, TOLERANCE)
    }

    @Test
    fun `in the end pane, the button sits the inner inset from the shared edge`() {
        assertEquals(8f, buttonLeft(innerEdge = PaneEdge.Start).value, TOLERANCE)
    }
}
