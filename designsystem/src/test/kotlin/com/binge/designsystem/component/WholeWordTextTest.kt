package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** [WholeWordText] wraps at spaces while every word fits, and goes to one ellipsized line instead of breaking a word. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h640dp")
// Native graphics, because legacy graphics measures text at a pixel a character: nothing wraps and nothing is clipped.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class WholeWordTextTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun show(width: Int, text: String) {
        rule.setContent { TestTheme { Box(Modifier.width(width.dp)) { Words(text) } } }
        rule.waitForIdle()
    }

    @Composable
    private fun Words(text: String) = WholeWordText(text, MaterialTheme.typography.bodyLarge, MaterialTheme.colorScheme.onSurface)

    private fun lineCount(text: String): Int {
        val node = rule.onAllNodesWithText(text).fetchSemanticsNodes().single()
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        return results.first().lineCount
    }

    @Test
    fun `words that each fit wrap onto lines at their spaces`() {
        show(width = NARROW, text = "Living room server name")

        assertTrue("expected several lines, got ${lineCount("Living room server name")}", lineCount("Living room server name") > 1)
        rule.assertNoTextClipped()
    }

    @Test
    fun `a word wider than the room keeps the text on one ellipsized line, with no broken word`() {
        show(width = NARROW, text = "Fitzgeraldmontgomery Alexandria")

        assertEquals(1, lineCount("Fitzgeraldmontgomery Alexandria"))
        rule.assertNoTextClipped()
    }

    @Test
    fun `an unspaced CJK label wider than the room still wraps between characters`() {
        val label = "リビングルームのサーバーの名前です"
        show(width = NARROW, text = label)

        assertTrue("expected several lines, got ${lineCount(label)}", lineCount(label) > 1)
        rule.assertNoTextClipped()
    }

    @Test
    fun `a hyphenated name the line breaker would split inside a part is held to one ellipsized line`() {
        val text = "Fitzgerald-Montgomery"
        show(width = HYPHEN_ROOM, text = text)

        assertEquals(1, lineCount(text))
        rule.assertNoTextClipped()
    }

    @Test
    fun `the text is in the semantics tree once`() {
        show(width = NARROW, text = "Fitzgerald-Montgomery Alexandria")

        assertEquals(1, rule.onAllNodesWithText("Fitzgerald-Montgomery Alexandria").fetchSemanticsNodes().size)
    }

    private companion object {
        const val NARROW = 90
        const val HYPHEN_ROOM = 130
    }
}
