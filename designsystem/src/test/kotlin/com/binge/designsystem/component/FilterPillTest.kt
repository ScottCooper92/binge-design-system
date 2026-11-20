package com.binge.designsystem.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import com.binge.designsystem.testing.assertNoTextClipped
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

/** [FilterPill], [CollapsedFilterAction] and [CountBadge]: one announced click target, taps only when allowed, and a badge that never clips. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is cut.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FilterPillTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(fontScale: Float = 1f, content: @Composable () -> Unit) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) { content() }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `a pill is one button announcing its description and taps once`() {
        var clicks = 0
        show {
            FilterPill(
                activeCount = 3,
                enabled = true,
                onClick = { clicks++ },
                label = "Filters",
                contentDescription = "Filters, 3 applied",
            )
        }

        val node = rule.onNode(hasClickAction()).assertContentDescriptionEquals("Filters, 3 applied")
        assertEquals(Role.Button, node.fetchSemanticsNode().config[SemanticsProperties.Role])
        node.performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a disabled pill takes no taps`() {
        var clicks = 0
        show { FilterPill(activeCount = 0, enabled = false, onClick = { clicks++ }, label = "Filters", contentDescription = "Filters") }

        rule.onNode(hasClickAction()).assertIsNotEnabled().performClick()

        assertEquals(0, clicks)
    }

    @Test
    fun `the pill shows a badge only when filters are applied`() {
        show { Row { FilterPill(activeCount = 0, enabled = true, onClick = {}, label = "None", contentDescription = "None") } }
        rule.onAllNodesWithText("0", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun `the pill and its badge are not clipped at 2_0`() {
        show(fontScale = 2f) {
            Row {
                FilterPill(
                    activeCount = 12,
                    enabled = true,
                    onClick = {},
                    label = "Filters",
                    contentDescription = "Filters, 12 applied",
                )
            }
        }
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 2)
    }

    @Test
    fun `a pill in a narrow slot at 2_0 keeps its badge`() {
        show(fontScale = 2f) {
            Row(Modifier.width(130.dp)) {
                FilterPill(
                    activeCount = 12,
                    enabled = true,
                    onClick = {},
                    label = "Filters",
                    contentDescription = "Filters, 12 applied",
                )
            }
        }
        val badge = rule.onNodeWithText("12", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertTrue("badge has no width", badge.width > 0.dp)
        assertTrue("badge is outside the slot", badge.right <= 130.dp)
    }

    private fun assertBadgeIsTwentyDp(fontScale: Float) {
        show(fontScale = fontScale) {
            CountBadge(count = 12, containerColor = Color.Red, contentColor = Color.White, modifier = Modifier.testTag("badge"))
        }
        val bounds = rule.onNodeWithTag("badge").getUnclippedBoundsInRoot()
        assertEquals("width at $fontScale", 20.dp, bounds.width)
        assertEquals("height at $fontScale", 20.dp, bounds.height)
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 1)
    }

    @Test fun `a badge is 20dp across at 1_0`() = assertBadgeIsTwentyDp(1f)

    @Test fun `a badge is 20dp across at 2_0`() = assertBadgeIsTwentyDp(2f)

    @Test
    fun `a count above 99 reads 99 plus and still fits`() {
        show(fontScale = 2f) { CountBadge(count = 150, containerColor = Color.Red, contentColor = Color.White) }

        rule.onNodeWithText("99+").assertExists()
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 1)
    }

    @Test
    fun `a collapsed action takes taps only once it is visible`() {
        var clicks = 0
        show {
            CollapsedFilterAction(
                activeCount = 0,
                enabled = true,
                collapsedFraction = 1f,
                contentDescription = "Filters",
                onClick = { clicks++ },
            )
        }

        rule.onNode(hasClickAction()).assertContentDescriptionEquals("Filters").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun `a hidden collapsed action is not an accessibility item`() {
        show {
            CollapsedFilterAction(
                activeCount = 3,
                enabled = true,
                collapsedFraction = 0f,
                contentDescription = "Filters",
                onClick = {},
            )
        }

        assertEquals(0, rule.onAllNodes(hasClickAction()).fetchSemanticsNodes().size)
    }
}
