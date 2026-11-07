package com.binge.designsystem.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.BingeLetterRail
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * At a large font the rail's letters fit their slots, so it draws fewer of them with dots between rather than cutting
 * them off top and bottom (#1088).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h1000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would clip.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LetterRailClippingTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val alphabet = ('A'..'Z').toList() + '#'

    private fun drawnAt(fontScale: Float): List<Char> {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme {
                    Box(Modifier.height(400.dp)) { BingeLetterRail(letters = alphabet, onLetter = {}) }
                }
            }
        }
        rule.waitForIdle()
        val checked = rule.assertNoTextClipped()
        val drawn = alphabet.filter { rule.onAllNodesWithText(it.toString()).fetchSemanticsNodes().isNotEmpty() }
        assertTrue("only $checked text nodes were checked", checked >= 2 && checked == drawn.size)
        return drawn
    }

    @Test fun `at 1_3x every drawn letter fits its slot`() {
        drawnAt(1.3f)
    }

    @Test fun `at 2x every drawn letter fits its slot, and the rail thins its letters`() {
        val drawn = drawnAt(2f)

        assertTrue("the rail draws fewer letters than there are, not all ${alphabet.size}", drawn.size < alphabet.size)
    }
}
