package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.template.LocalHeroReadingMargin
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

/**
 * [DetailInfoGrid]: the column count it is given, long values wrapping inside their column without being cropped at 1.3x
 * and 2x font, blank facts left out, and the hero reading margin insetting the whole grid.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w840dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailInfoGridTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val long = "A very long value that has to wrap inside its own column rather than push its neighbour"

    private fun entries() =
        listOf(
            DetailInfoEntry(Icons.Filled.Person, "One", long),
            DetailInfoEntry(Icons.Filled.Person, "Two", "b"),
            DetailInfoEntry(Icons.Filled.Person, "Three", "c"),
            DetailInfoEntry(Icons.Filled.Person, "Blank", "  "),
        )

    private fun show(
        columns: Int,
        fontScale: Float = 1f,
        margin: Float = 0f,
    ) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(density.density, fontScale = fontScale),
                LocalHeroReadingMargin provides margin.dp,
            ) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(600.dp).testTag("grid")) { DetailInfoGrid(entries(), columns = columns) }
                }
            }
        }
        rule.waitForIdle()
    }

    @Test
    fun `three columns put three facts on the first line and the next below`() {
        show(columns = 3)

        val one = rule.onNodeWithText("One").getBoundsInRoot()
        val two = rule.onNodeWithText("Two").getBoundsInRoot()
        val three = rule.onNodeWithText("Three").getBoundsInRoot()
        assertTrue(one.left < two.left && two.left < three.left)
        assertEquals(one.top, two.top)
        assertEquals(two.top, three.top)
    }

    @Test
    fun `one column is a list, each fact below the last`() {
        show(columns = 1)

        val one = rule.onNodeWithText("One").getBoundsInRoot()
        val two = rule.onNodeWithText("Two").getBoundsInRoot()
        assertEquals(one.left, two.left)
        assertTrue(two.top > one.top)
    }

    @Test
    fun `a blank fact is left out`() {
        show(columns = 2)

        rule.onNodeWithText("Blank").assertDoesNotExist()
    }

    private fun assertLongValueInsideItsColumn(fontScale: Float) {
        show(columns = 3, fontScale = fontScale)

        val grid = rule.onNodeWithTag("grid").getBoundsInRoot()
        val value = rule.onNodeWithText(long).getBoundsInRoot()
        val neighbour = rule.onNodeWithText("Two").getBoundsInRoot()
        assertTrue("the value stays inside the grid", value.left >= grid.left && value.right <= grid.right)
        assertTrue("the value does not run into the next column", value.right <= neighbour.left)
    }

    @Test fun `a long value wraps inside its column at 1_3`() = assertLongValueInsideItsColumn(1.3f)

    @Test fun `a long value wraps inside its column at 2_0`() = assertLongValueInsideItsColumn(2.0f)

    @Test
    fun `the reading margin insets the grid`() {
        var margin by mutableStateOf(0.dp)
        rule.setContent {
            CompositionLocalProvider(LocalHeroReadingMargin provides margin) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Box(Modifier.width(600.dp)) { DetailInfoGrid(entries(), columns = 2) }
                }
            }
        }
        rule.waitForIdle()
        val flush = rule.onNodeWithText("One").getBoundsInRoot().left

        margin = 40.dp
        rule.waitForIdle()
        val inset = rule.onNodeWithText("One").getBoundsInRoot().left

        assertEquals(40.dp.value, (inset - flush).value, 0.5f)
    }
}
