package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
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
import kotlin.math.abs

/**
 * Every meta line, a line of scores, of plain entries or of wrapping text, wraps without a separator ever ending a line or starting one: a dot is
 * drawn between two entries on the same line and dropped at a break. Each case runs at 1.0, 1.3 and 2.0 in a 200dp column, and the 2.0 case of
 * each is checked to have wrapped, so the test cannot pass for a line that fitted.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w360dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MetaFlowSeparatorTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val scores = listOf(
        ExternalScore(ExternalScoreKind.CriticsFresh, "97%", "Critics 97 percent"),
        ExternalScore(ExternalScoreKind.Audience, "95%", "Audience 95 percent"),
        ExternalScore(ExternalScoreKind.Imdb, "9.0", "IMDb 9.0"),
    )

    private fun show(fontScale: Float, content: @Composable () -> Unit) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) { Box(Modifier.width(WIDTH.dp).testTag("flow")) { content() } }
            }
        }
        rule.waitForIdle()
    }

    private fun placedDots(): List<Rect> =
        rule
            .onAllNodes(hasTestTag(META_DOT_TAG), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .filter { it.layoutInfo.isPlaced }
            .map { it.boundsInRoot }

    /** Checks every dot has an entry on its line before it and after it, and returns how many lines the entries fall on. */
    private fun assertNoDanglingDot(
        entries: List<Rect>,
        dots: List<Rect>,
        label: String,
    ): Int {
        dots.forEach { dot ->
            val sameLine = entries.filter { abs(it.center.y - dot.center.y) < SAME_LINE_PX }
            assertTrue("$label: a dot has an entry before it on its line", sameLine.any { it.right <= dot.left + 1f })
            assertTrue("$label: a dot has an entry after it on its line", sameLine.any { it.left >= dot.right - 1f })
        }
        return entries.map { Math.round(it.top) }.distinct().size
    }

    private fun scoreBounds() =
        scores.flatMap { score ->
            rule
                .onAllNodesWithContentDescription(score.contentDescription, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .filter { it.layoutInfo.isPlaced }
                .map { it.boundsInRoot }
        }

    private fun textBounds(vararg texts: String) =
        texts.flatMap { text ->
            rule
                .onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .filter { it.layoutInfo.isPlaced }
                .map { it.boundsInRoot }
        }

    private fun checkScores(fontScale: Float): Int {
        show(fontScale) { ExternalScoreLine(scores = scores) }
        val entries = scoreBounds()
        assertEquals("every score is drawn", scores.size, entries.size)
        return assertNoDanglingDot(entries, placedDots(), "scores at ${fontScale}x")
    }

    private fun checkEntries(fontScale: Float): Int {
        show(fontScale) {
            MetaFlow(
                listOf(
                    MetaEntry.Content { Text("2008") },
                    MetaEntry.Content { Text("2h 32m") },
                    MetaEntry.Content { Text("Action Crime") },
                    MetaEntry.Content { Text("Available") },
                ),
            )
        }
        val entries = textBounds("2008", "2h 32m", "Action Crime", "Available")
        assertEquals("every entry is drawn", 4, entries.size)
        return assertNoDanglingDot(entries, placedDots(), "entries at ${fontScale}x")
    }

    private fun checkWrappingText(fontScale: Float): Int {
        show(fontScale) {
            MetaFlow(
                listOf(
                    MetaEntry.WrappingText("Acción · Crimen · Drama · Suspense · Ciencia ficción · Aventura", Color.Unspecified),
                    MetaEntry.Content { Text("Next") },
                ),
            )
        }
        val entries = textBounds("Acción", "Next")
        assertEquals("both entries are drawn", 2, entries.size)
        return assertNoDanglingDot(entries, placedDots(), "wrapping text at ${fontScale}x")
    }

    @Test fun `scores never end a line on a dot at 1_0`() {
        checkScores(1f)
    }

    @Test fun `scores never end a line on a dot at 1_3`() {
        checkScores(1.3f)
    }

    @Test
    fun `scores never end a line on a dot at 2_0, where they wrap`() {
        assertTrue("the scores did not wrap at 2.0, so the test proved nothing", checkScores(2f) > 1)
    }

    @Test fun `plain entries never end a line on a dot at 1_0`() {
        checkEntries(1f)
    }

    @Test fun `plain entries never end a line on a dot at 1_3`() {
        checkEntries(1.3f)
    }

    @Test
    fun `plain entries never end a line on a dot at 2_0, where they wrap`() {
        assertTrue("the entries did not wrap at 2.0, so the test proved nothing", checkEntries(2f) > 1)
    }

    @Test fun `wrapping text never ends a line on a dot at 1_0`() {
        checkWrappingText(1f)
    }

    @Test fun `wrapping text never ends a line on a dot at 1_3`() {
        checkWrappingText(1.3f)
    }

    @Test
    fun `wrapping text never ends a line on a dot at 2_0, and the next entry starts a line of its own`() {
        assertTrue("the text and the next entry share a line at 2.0, so the test proved nothing", checkWrappingText(2f) > 1)
    }

    private companion object {
        const val WIDTH = 200
        const val SAME_LINE_PX = 6f
    }
}
