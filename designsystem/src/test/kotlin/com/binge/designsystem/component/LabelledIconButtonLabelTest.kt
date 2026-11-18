package com.binge.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
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
        assertTrue("fewer than two text nodes were checked", rule.assertNoTextClipped() >= 1)
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
}
