package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private val NARROW_PARENT = 80.dp
private val WEIGHTED_ROW = 360.dp

/**
 * A [LabelledIconButton]'s width is a floor, so at a large font a short label such as "Trailer" or "Tráiler" reads in full on one line rather
 * than being cut to "Trail…", and no text is clipped by its parent (its bounds against its size), at 1.3x and 2x.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h800dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is cut.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LabelledIconButtonLabelTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(label: String, fontScale: Float) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    LabelledIconButton(icon = Icons.Filled.PlayArrow, label = label, onClick = {})
                }
            }
        }
        rule.waitForIdle()
    }

    private fun layoutOf(label: String): TextLayoutResult {
        val results = mutableListOf<TextLayoutResult>()
        rule
            .onNodeWithText(label, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)
        return results.single()
    }

    private fun assertReadsInFull(label: String, fontScale: Float) {
        show(label, fontScale)

        val layout = layoutOf(label)
        assertEquals("$label wrapped at $fontScale", 1, layout.lineCount)
        assertFalse("$label was cut at $fontScale", layout.isLineEllipsized(0))
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 1)
    }

    @Test fun `Trailer reads in full at 1_3`() = assertReadsInFull("Trailer", 1.3f)

    @Test fun `Trailer reads in full at 2_0`() = assertReadsInFull("Trailer", 2.0f)

    @Test fun `Tráiler reads in full at 1_3`() = assertReadsInFull("Tráiler", 1.3f)

    @Test fun `Tráiler reads in full at 2_0`() = assertReadsInFull("Tráiler", 2.0f)

    @Test
    fun `a long label wraps to two lines and is cut after that, at 2_0`() {
        val label = "Add this title to a list shared with all of your friends"
        show(label, 2.0f)

        val layout = layoutOf(label)
        assertEquals(2, layout.lineCount)
        assertTrue(layout.isLineEllipsized(1))
    }

    @Test fun `Subscribe reads in full at 1_0`() = assertReadsInFull("Subscribe", 1.0f)

    @Test fun `Subscribe reads in full at 1_3`() = assertReadsInFull("Subscribe", 1.3f)

    @Test fun `Subscribe reads in full at 2_0, as the cap grows with the font`() = assertReadsInFull("Subscribe", 2.0f)

    @Test fun `Suscribirse reads in full at 2_0`() = assertReadsInFull("Suscribirse", 2.0f)

    @Test
    fun `a word wider than even the scaled cap stays whole on one line and is cut with an ellipsis at 2_0`() {
        val word = "Unsubscribefromeverything"
        show(word, 2.0f)

        val layout = layoutOf(word)
        assertEquals("the word was broken across lines", 1, layout.lineCount)
        assertTrue(layout.isLineEllipsized(0))
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 1)
    }

    @Test
    fun `a word the parent leaves too little room for is not broken either`() {
        val word = "Subscribe"
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2.0f)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    // Narrower than the word needs at 2x, though inside the scaled cap.
                    Box(Modifier.width(NARROW_PARENT)) {
                        LabelledIconButton(icon = Icons.Filled.PlayArrow, label = word, onClick = {})
                    }
                }
            }
        }
        rule.waitForIdle()

        val layout = layoutOf(word)
        assertEquals("the word was broken across lines", 1, layout.lineCount)
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 1)
    }

    @Test
    fun `a row of buttons sized with intrinsic measurement does not throw, and each reads in full`() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2.0f)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    // IntrinsicSize.Min measures the row's children for their intrinsic sizes, which a subcomposing layout cannot answer.
                    Row(Modifier.height(IntrinsicSize.Min)) {
                        LabelledIconButton(icon = Icons.Filled.PlayArrow, label = "Trailer", onClick = {})
                        LabelledIconButton(icon = Icons.Filled.PlayArrow, label = "Subscribe", onClick = {})
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(1, layoutOf("Subscribe").lineCount)
        assertFalse(layoutOf("Subscribe").isLineEllipsized(0))
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 2)
    }

    @Test
    fun `a button given a weight fills its slot and keeps its label centred in it`() {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, 2.0f)) {
                BingeExpressiveTheme(dynamicColor = false) {
                    Row(Modifier.width(WEIGHTED_ROW)) {
                        LabelledIconButton(icon = Icons.Filled.PlayArrow, label = "Subscribe", onClick = {}, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        rule.waitForIdle()

        assertEquals(1, layoutOf("Subscribe").lineCount)
        assertFalse(layoutOf("Subscribe").isLineEllipsized(0))
        assertTrue("no text node was checked", rule.assertNoTextClipped() >= 1)
        val button = rule.onNode(hasClickAction()).getUnclippedBoundsInRoot()
        val label = rule.onNodeWithText("Subscribe", useUnmergedTree = true).getUnclippedBoundsInRoot()
        assertEquals("the slot's width is the button's", WEIGHTED_ROW.value, button.width.value, 0.5f)
        assertEquals("the label is centred in the slot", (button.left + button.width / 2).value, (label.left + label.width / 2).value, 1f)
    }
}
