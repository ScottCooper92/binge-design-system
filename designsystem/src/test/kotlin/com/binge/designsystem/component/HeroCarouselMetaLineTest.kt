package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The hero's meta line wraps at a large font, and a separator never ends a line or starts one: the dot is drawn only between two entries on the
 * same line, and at a break it is dropped.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroCarouselMetaLineTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val item = HeroItem(
        id = 1,
        imageUrl = null,
        title = "The Dark Knight",
        rating = 9.0f,
        genres = listOf("Action", "Crime"),
        year = "2008",
        runtimeMinutes = 152,
        status = MediaCardStatus("Available", BingeSentiment.Positive),
    )

    private fun show(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme { HeroCarousel(items = listOf(item), onItemClick = {}) }
            }
        }
        rule.waitForIdle()
    }

    private fun placed(nodes: androidx.compose.ui.test.SemanticsNodeInteractionCollection): List<Rect> =
        nodes.fetchSemanticsNodes().filter { it.layoutInfo.isPlaced }.map { it.boundsInRoot }

    private fun assertNoLineEndsOnADot(fontScale: Float): Int {
        show(fontScale)
        val dots = placed(rule.onAllNodes(hasTestTag(META_DOT_TAG), useUnmergedTree = true))
        val entries =
            listOf(
                "2008",
                "2h 32m",
                "Action · Crime",
            ).flatMap { placed(rule.onAllNodesWithText(it, substring = true, useUnmergedTree = true)) } +
                placed(rule.onAllNodesWithContentDescription("Available", useUnmergedTree = true)) +
                placed(rule.onAllNodesWithText("9.0", useUnmergedTree = true))
        dots.forEach { dot ->
            val sameLine = entries.filter { kotlin.math.abs(it.center.y - dot.center.y) < dot.height * LINE_FACTOR + SAME_LINE_PX }
            assertTrue("a dot has an entry before it on its line at ${fontScale}x", sameLine.any { it.right <= dot.left + 1f })
            assertTrue("a dot has an entry after it on its line at ${fontScale}x", sameLine.any { it.left >= dot.right - 1f })
        }
        // One dot between each pair of neighbours on a line: the dots are the entries on a line less one, summed over the lines.
        val lines = entries.map { Math.round(it.center.y) }.distinct().size
        assertEquals("a dot between neighbours on a line and none at a break, at ${fontScale}x", entries.size - lines, dots.size)
        return lines
    }

    @Test
    fun `no line ends or starts on a separator at 1_0`() {
        assertNoLineEndsOnADot(1f)
    }

    @Test
    fun `no line ends or starts on a separator at 1_3`() {
        assertNoLineEndsOnADot(1.3f)
    }

    @Test
    fun `no line ends or starts on a separator at 2_0, where the line wraps`() {
        val lines = assertNoLineEndsOnADot(2f)
        assertTrue("the line did not wrap at 2.0, so the test proved nothing: $lines line(s)", lines > 1)
    }

    private companion object {
        const val LINE_FACTOR = 0f
        const val SAME_LINE_PX = 6f
    }
}
