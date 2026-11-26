package com.binge.designsystem

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The expanded tier answered for the space a screen is laid out in: a screen alone in an expanded window has it, one beside another
 * pane does not, as the pane is a fraction of the window, and a pane that knows its width is judged by that.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h900dp")
class IsExpandedPaneTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun expandedIn(edge: PaneEdge? = null, width: Dp? = null): Boolean {
        var expanded = false
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalPaneInnerEdge provides edge, LocalPaneWidth provides width) { expanded = isExpandedPane() }
            }
        }
        rule.waitForIdle()
        return expanded
    }

    @Test
    fun `a screen alone in an expanded window is expanded`() = assertEquals(true, expandedIn())

    @Test
    fun `a screen beside another pane in that window is not`() = assertEquals(false, expandedIn(edge = PaneEdge.Start))

    @Test
    fun `a narrow pane that knows its width is not`() = assertEquals(false, expandedIn(edge = PaneEdge.Start, width = 420.dp))

    @Test
    fun `a pane as wide as the expanded tier is`() = assertEquals(true, expandedIn(edge = PaneEdge.Start, width = 900.dp))
}
