package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.CatalogKind
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The grid reflows with the window, and a card is one tap target whatever its preview holds. */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class CatalogGridTest {
    @get:Rule
    val rule = createComposeRule()

    private val samples =
        (0 until SAMPLE_COUNT).map { index ->
            CatalogEntry(id = "S$index", group = "Group", name = "Cell $index", description = "") { Box(Modifier.size(10.dp)) }
        }

    private fun show(entries: List<CatalogEntry>, onSelect: (CatalogEntry) -> Unit = {}) =
        rule.setContent { MaterialTheme { CatalogGrid(entries, onSelect) } }

    private fun columnsInFirstRow(): Int {
        val tops = (0 until SAMPLE_COUNT).map { rule.onNodeWithText("Cell $it").getBoundsInRoot().top }
        return tops.count { it == tops.first() }
    }

    @Test
    fun `a phone gets two columns`() {
        show(samples)

        assertEquals(2, columnsInFirstRow())
    }

    @Test
    @Config(qualifiers = "w1000dp-h700dp-xhdpi")
    fun `a wide window gets more columns from the same grid`() {
        show(samples)

        assertEquals(4, columnsInFirstRow())
    }

    @Test
    fun `a preview's own controls are not exposed and a tap anywhere on the card selects it`() {
        var selected: CatalogEntry? = null
        val entry =
            CatalogEntry(id = "B", group = "Group", name = "Has a button", description = "") {
                Text("Inner label")
            }
        show(listOf(entry)) { selected = it }

        rule.onNodeWithText("Inner label").assertDoesNotExist()
        rule.onNodeWithText("Has a button").performTouchInput { click(percentOffset(0.5f, 0.25f)) }
        assertEquals(entry, selected)
    }

    @Test
    fun `tapping the name selects the entry too`() {
        var selected: CatalogEntry? = null
        show(samples) { selected = it }

        rule.onNodeWithText("Cell 1").performClick()

        assertEquals("S1", selected?.id)
    }

    @Test
    fun `demos get a badge and their own heading, samples do not get a badge`() {
        val demo = CatalogEntry("D", "TopBar", "A demo", "", CatalogKind.Demo) {}
        show(listOf(demo) + samples.take(2))

        rule.onNodeWithText("Demos").assertIsDisplayed()
        rule.onAllNodesWithText("Demo").assertCountEquals(1)
    }

    @Test
    fun `Tab moves from one card straight to the next, not through its preview overlay`() {
        show(samples)

        rule.onNodeWithText("Cell 0").requestFocus()
        rule.onNodeWithText("Cell 0").performKeyInput { pressKey(Key.Tab) }
        rule.waitForIdle()

        rule.onNodeWithText("Cell 1").assertIsFocused()
    }

    @Test
    fun `Tab skips a focusable control inside a preview and reaches the next card`() {
        val withButton =
            CatalogEntry(id = "B0", group = "Group", name = "Cell 0", description = "") {
                Button(onClick = {}) { Text("Inner button") }
            }
        show(listOf(withButton) + samples.drop(1))

        rule.onNodeWithText("Cell 0").requestFocus()
        rule.onNodeWithText("Cell 0").performKeyInput { pressKey(Key.Tab) }
        rule.waitForIdle()

        rule.onNodeWithText("Cell 1").assertIsFocused()
    }

    private companion object {
        const val SAMPLE_COUNT = 6
    }
}
