package com.binge.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The external scores sit on a line of their own, last in the meta block, on the hero and both detail headers. */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w360dp-h8000dp",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExternalScorePlacementTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val scores = listOf(
        ExternalScore(ExternalScoreKind.CriticsFresh, "97%", "Critics 97 percent"),
        ExternalScore(ExternalScoreKind.Audience, "95%", "Audience 95 percent"),
        ExternalScore(ExternalScoreKind.Imdb, "9.0", "IMDb 9.0"),
    )

    private fun scoresTop(): Dp =
        rule.onNodeWithContentDescription(scores.first().contentDescription, useUnmergedTree = true).getBoundsInRoot().top

    private fun scoresBottom(): Dp =
        rule.onNodeWithContentDescription(scores.last().contentDescription, useUnmergedTree = true).getBoundsInRoot().bottom

    private fun bounds(text: String) = rule.onNodeWithText(text, substring = true, useUnmergedTree = true).getBoundsInRoot()

    @Test
    fun `the detail hero draws the scores after the genres, the tagline and the meta text`() {
        rule.setContent {
            TestTheme {
                DetailHero(
                    title = "Title",
                    backdropUrl = null,
                    tagline = "Why so serious",
                    metaText = "2008 · 2h 32m",
                    genres = listOf("Action", "Crime"),
                    externalScores = scores,
                    metaContent = { Text("Chips") },
                    onBack = {},
                    showChrome = false,
                )
            }
        }
        rule.waitForIdle()
        assertTrue("below the genres", scoresTop() >= bounds("Action").bottom)
        assertTrue("below the tagline", scoresTop() >= bounds("Why so serious").bottom)
        assertTrue("below the meta text", scoresTop() >= bounds("2008 · 2h 32m").bottom)
        assertTrue("above the chips", scoresBottom() <= bounds("Chips").top)
    }

    @Test
    fun `the cinematic header draws the scores after the tagline and before the synopsis`() {
        rule.setContent {
            TestTheme {
                DetailCinematicHeader(
                    title = "Title",
                    genres = listOf("Action", "Crime"),
                    synopsis = "A long synopsis.",
                    tagline = "Why so serious",
                    stats = emptyList(),
                    backdropUrl = null,
                    posterUrl = null,
                    externalScores = scores,
                )
            }
        }
        rule.waitForIdle()
        assertTrue("below the genres", scoresTop() >= bounds("Action").bottom)
        assertTrue("below the tagline", scoresTop() >= bounds("Why so serious").bottom)
        assertTrue("above the synopsis", scoresBottom() <= bounds("A long synopsis.").top)
    }

    @Test
    fun `the hero draws the scores below the rating and genre line`() {
        rule.setContent {
            TestTheme {
                HeroCarousel(
                    items = listOf(
                        HeroItem(
                            id = 1,
                            imageUrl = null,
                            title = "Title",
                            rating = 9.0f,
                            genres = listOf("Action", "Crime"),
                            externalScores = scores,
                        ),
                    ),
                    onItemClick = {},
                )
            }
        }
        rule.waitForIdle()
        assertTrue("below the genres", scoresTop() >= bounds("Action · Crime").bottom)
    }
}
