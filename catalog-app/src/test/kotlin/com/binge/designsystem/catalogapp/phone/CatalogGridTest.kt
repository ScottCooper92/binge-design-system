package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalogapp.registry.CatalogComponent
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.CatalogKind
import com.binge.designsystem.catalogapp.registry.components
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The grid has two columns at any width, a card per component, and a card is one tap target whatever its preview holds. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class CatalogGridTest {
    @get:Rule
    val rule = createComposeRule()

    private val samples =
        (0 until SAMPLE_COUNT).map { index ->
            CatalogEntry(id = "S$index", group = "G$index", groupName = "Cell $index", name = "Default", description = "") {
                Box(Modifier.size(10.dp))
            }
        }

    private fun show(entries: List<CatalogEntry>, onSelect: (CatalogComponent) -> Unit = {}) =
        rule.setContent { BingeExpressiveTheme { CatalogGrid(entries.components(), onSelect) } }

    /** Counts the first row from the first three cells: enough to tell two columns from more, and all on screen. */
    private fun columnsInFirstRow(): Int {
        val tops = (0 until ROW_PROBE).map { rule.onNodeWithText("Cell $it").getBoundsInRoot().top }
        return tops.count { it == tops.first() }
    }

    @Test
    fun `a phone gets two columns`() {
        show(samples)

        assertEquals(2, columnsInFirstRow())
    }

    @Test
    @Config(qualifiers = "w1000dp-h700dp-xhdpi")
    fun `a wide window keeps two columns, as the list pane beside a component is phone width`() {
        show(samples)

        assertEquals(2, columnsInFirstRow())
    }

    @Test
    fun `a preview's own controls are not exposed and a tap anywhere on the card selects it`() {
        var selected: CatalogComponent? = null
        val entry =
            CatalogEntry(id = "B", group = "Group", groupName = "Has a button", name = "Default", description = "") {
                Text("Inner label")
            }
        show(listOf(entry)) { selected = it }

        rule.onNodeWithText("Inner label").assertDoesNotExist()
        rule.onNodeWithText("Has a button").performTouchInput { click(percentOffset(0.5f, 0.25f)) }
        assertEquals("Group", selected?.group)
    }

    @Test
    fun `tapping the name selects the component too`() {
        var selected: CatalogComponent? = null
        show(samples) { selected = it }

        rule.onNodeWithText("Cell 1").performClick()

        assertEquals("G1", selected?.group)
    }

    @Test
    fun `a component's variants share one card that counts them`() {
        val variants = (0 until 3).map { CatalogEntry("V$it", "Group", "One component", "Variant $it", "") {} }
        show(variants)

        rule.onAllNodesWithText("One component").assertCountEquals(1)
        rule.onNodeWithText("3 variants").assertIsDisplayed()
    }

    @Test
    fun `components with a demo get a badge and come first under their own heading`() {
        val demo = CatalogEntry("D", "G1", "Cell 1", "Enter always", "", CatalogKind.Demo) {}
        show(samples.take(2) + demo)

        rule.onNodeWithText("Demos").assertIsDisplayed()
        rule.onNodeWithText("Components").assertIsDisplayed()
        rule.onAllNodesWithText("Demo").assertCountEquals(1)
        assertTrue(rule.onNodeWithText("Cell 1").getBoundsInRoot().top < rule.onNodeWithText("Cell 0").getBoundsInRoot().top)
    }

    private companion object {
        const val SAMPLE_COUNT = 6
        const val ROW_PROBE = 3
    }
}
