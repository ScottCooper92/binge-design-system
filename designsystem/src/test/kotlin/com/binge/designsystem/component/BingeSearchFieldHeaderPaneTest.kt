package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FIELD = "field"
private const val TOLERANCE = 1f

/** The search field header keeps the page's own edge: beside another pane the field sits where the cards sit. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeSearchFieldHeaderPaneTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun fieldLeft(innerEdge: PaneEdge?): Dp {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalPaneInnerEdge provides innerEdge) {
                    BingeSearchFieldHeader { Box(Modifier.testTag(FIELD).fillMaxWidth().height(40.dp)) }
                }
            }
        }
        return rule.onNodeWithTag(FIELD).getBoundsInRoot().left
    }

    @Test
    fun `alone in the window, the field sits the window inset from the edge`() {
        assertEquals(16f, fieldLeft(innerEdge = null).value, TOLERANCE)
    }

    @Test
    fun `in the end pane, the field sits the inner inset from the shared edge`() {
        assertEquals(8f, fieldLeft(innerEdge = PaneEdge.Start).value, TOLERANCE)
    }
}
