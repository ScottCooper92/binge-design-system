package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
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
    fun `a katakana label with a prolonged sound mark wraps at every width and is never cut to one line`() {
        val text = "サーバーサーバーサーバーサーバーサーバー"
        rule.setContent {
            TestTheme {
                // A scrolling column, so no label is given a bounded height and cut to fit it.
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    KATAKANA_WIDTHS.forEach { width -> Box(Modifier.width(width.dp)) { Words(text) } }
                }
            }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText(text).fetchSemanticsNodes().forEach { node ->
            val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(results)
            assertTrue("a width cut the label to ${results.first().lineCount} line", results.first().lineCount > 1)
        }
    }

    @Test
    fun `a kana label with prolonged sound marks wraps rather than going to one line`() {
        val label = "サーバー".repeat(6)
        show(width = NARROW, text = label)

        assertTrue("expected several lines, got ${lineCount(label)}", lineCount(label) > 1)
    }

    @Test
    fun `an unspaced Thai label wider than the room wraps rather than going to one line`() {
        val label = "เซิร์ฟเวอร์ห้องนั่งเล่นของฉัน"
        show(width = NARROW, text = label)

        assertTrue("expected several lines, got ${lineCount(label)}", lineCount(label) > 1)
    }

    @Test
    fun `a label that mixes Latin letters and CJK wraps at every width and is never cut to one line`() {
        val text = "Plex映画ライブラリと4K映画コレクション"
        rule.setContent {
            TestTheme {
                // A scrolling column, so no label is given a bounded height and cut to fit it.
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    MIXED_WIDTHS.forEach { width -> Box(Modifier.width(width.dp)) { Words(text) } }
                }
            }
        }
        rule.waitForIdle()

        rule.onAllNodesWithText(text).fetchSemanticsNodes().forEach { node ->
            val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
            node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(results)
            assertTrue("a width cut the label to ${results.first().lineCount} line", results.first().lineCount > 1)
        }
    }

    @Test
    fun `a hyphenated name the line breaker would split inside a part is held to one ellipsized line`() {
        val text = "Fitzgerald-Montgomery"
        show(width = HYPHEN_ROOM, text = text)

        assertEquals(1, lineCount(text))
        rule.assertNoTextClipped()
    }

    @Test
    fun `it answers a height intrinsic query, so it sits in a row sized to its tallest child`() {
        rule.setContent {
            TestTheme {
                Row(Modifier.height(IntrinsicSize.Max).width(NARROW.dp)) {
                    Words("Living room server name")
                    Box(Modifier.testTag(TALL).width(10.dp).fillMaxHeight())
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag(TALL).assertHeightIsAtLeast(20.dp)
    }

    @Test
    fun `it answers a width intrinsic query, so it sits in a column sized to its widest child`() {
        rule.setContent {
            TestTheme {
                Column(Modifier.width(IntrinsicSize.Max)) {
                    Words("Living room")
                    Box(Modifier.testTag(WIDE).fillMaxWidth().height(10.dp))
                }
            }
        }
        rule.waitForIdle()

        rule.onNodeWithTag(WIDE).assertWidthIsAtLeast(40.dp)
    }

    @Test
    fun `the text is in the semantics tree once`() {
        show(width = NARROW, text = "Fitzgerald-Montgomery Alexandria")

        assertEquals(1, rule.onAllNodesWithText("Fitzgerald-Montgomery Alexandria").fetchSemanticsNodes().size)
    }

    private fun showBounded(fontScale: Float, text: String) {
        rule.setContent {
            val density = androidx.compose.ui.platform.LocalDensity.current
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit
                    .Density(density.density, fontScale = fontScale),
            ) {
                TestTheme { Box(Modifier.width(HYPHEN_ROOM.dp).height(BOUNDED_HEIGHT.dp)) { Words(text) } }
            }
        }
        rule.waitForIdle()
    }

    private fun checkBounded(fontScale: Float) {
        val text = "Signed in as the household's shared administrator account"
        showBounded(fontScale, text)

        val node = rule.onAllNodesWithText(text).fetchSemanticsNodes().single()
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        node.config[androidx.compose.ui.semantics.SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        val layout = results.first()
        assertTrue("the text should not fit the height", layout.didOverflowHeight)
        assertTrue("the last visible line should end in an ellipsis", layout.isLineEllipsized(layout.lineCount - 1))
        rule.assertNoTextClipped()
    }

    @Test
    fun `a bounded height ends the last line in an ellipsis at 1x`() = checkBounded(1f)

    @Test
    fun `a bounded height ends the last line in an ellipsis at 2x`() = checkBounded(2f)

    private companion object {
        const val BOUNDED_HEIGHT = 70
        const val NARROW = 90
        val MIXED_WIDTHS = listOf(44, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108)
        val KATAKANA_WIDTHS = listOf(64, 68, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112, 116)
        const val TALL = "tall"
        const val WIDE = "wide"
        const val HYPHEN_ROOM = 130
    }
}
