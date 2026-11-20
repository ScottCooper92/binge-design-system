package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
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

/**
 * The hero's rating and external scores share one line under the meta line, and that line never wraps: at a large font a score that does not
 * fit is dropped rather than wrapped or clipped. A title with no external scores keeps its rating on the first meta line.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroCarouselScoreLineTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val scores = listOf(
        ExternalScore(ExternalScoreKind.CriticsFresh, "97%", "Critics 97 percent"),
        ExternalScore(ExternalScoreKind.Audience, "95%", "Audience 95 percent"),
        ExternalScore(ExternalScoreKind.Imdb, "9.0", "IMDb 9.0"),
    )

    private fun show(item: HeroItem, fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme { HeroCarousel(items = listOf(item), onItemClick = {}) }
            }
        }
        rule.waitForIdle()
    }

    private fun withScores() =
        HeroItem(
            id = 1,
            imageUrl = null,
            title = "The Dark Knight",
            rating = 9.5f,
            genres = listOf("Action", "Crime"),
            year = "2008",
            runtimeMinutes = 152,
            externalScores = scores,
        )

    private fun shownScores() =
        scores.filter { score ->
            rule
                .onAllNodesWithContentDescription(score.contentDescription, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .any { it.layoutInfo.isPlaced }
        }

    private fun assertOneLine(fontScale: Float) {
        show(withScores(), fontScale)
        val shown = shownScores()
        val rating = rule.onNodeWithText("9.5", useUnmergedTree = true).getBoundsInRoot()
        val meta = rule.onNodeWithText("2008", useUnmergedTree = true).getBoundsInRoot()
        assertTrue("the rating sits below the meta line", rating.top.value > meta.bottom.value - LINE_TOLERANCE)
        shown.forEach { score ->
            val bounds = rule.onNodeWithContentDescription(score.contentDescription, useUnmergedTree = true).getBoundsInRoot()
            assertEquals("${score.contentDescription} is on the rating's line", rating.centerY(), bounds.centerY(), LINE_TOLERANCE)
        }
        assertTrue("the rating line clipped some text", rule.assertNoTextClipped() > 0)
    }

    private fun androidx.compose.ui.unit.DpRect.centerY() = (top.value + bottom.value) / 2

    @Test
    fun `the rating and every score share one line at 1_0`() {
        assertOneLine(1f)
        assertEquals(scores, shownScores())
    }

    @Test
    fun `the rating and the scores stay on one line at 1_3`() = assertOneLine(1.3f)

    @Test
    fun `the rating and the scores stay on one line at 2_0 with the trailing scores dropped`() {
        assertOneLine(2f)
        val shown = shownScores()
        assertTrue("every score fitted at 2.0, so nothing was dropped: $shown", shown.size < scores.size)
        assertEquals("the scores that stay are the leading ones", scores.take(shown.size), shown)
    }

    @Test
    fun `a title with no external scores keeps its rating on the first meta line`() {
        show(withScores().copy(externalScores = emptyList()), 1f)
        val rating = rule.onNodeWithText("9.5", useUnmergedTree = true).getBoundsInRoot()
        val year = rule.onNodeWithText("2008", useUnmergedTree = true).getBoundsInRoot()
        assertEquals("the rating shares the year's line", year.centerY(), rating.centerY(), LINE_TOLERANCE)
    }

    private companion object {
        val TOLERANCE = androidx.compose.ui.unit
            .Dp(2f)
        const val LINE_TOLERANCE = 2f
    }
}
