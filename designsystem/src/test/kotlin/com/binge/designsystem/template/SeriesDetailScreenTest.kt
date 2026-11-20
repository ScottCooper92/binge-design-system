package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.catalog.MEDIA_PAGE_OVERVIEW
import com.binge.designsystem.component.EpisodeRow
import com.binge.designsystem.component.SeasonRow
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The series template draws the season dropdown only where there is a season to choose, shows the episodes it is given with their marks,
 * and offers Try again on failed episodes, reporting each tap.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h2400dp-xhdpi",
)
class SeriesDetailScreenTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private val seasons = listOf(SeasonRow(1, "Season One", "7 episodes"), SeasonRow(2, "Season Two", "13 episodes"))

    private fun show(
        episodes: EpisodesState,
        seasons: List<SeasonRow> = this.seasons,
        callbacks: SeriesDetailCallbacks = SeriesDetailCallbacks(onSelectSeason = {}),
    ) {
        rule.setContent {
            ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
                SeriesDetailScreen(
                    state = DetailState.Content(MediaDetail(title = "A show", overview = MEDIA_PAGE_OVERVIEW)),
                    series = SeriesDetail(
                        seasons = seasons,
                        selectedSeason = 1,
                        episodes = episodes,
                        episodesTitle = "Episodes",
                        seasonSubtitle = "Season One · 7 episodes",
                        viewAllLabel = "View all",
                    ),
                    callbacks = MediaDetailCallbacks(onBack = {}),
                    seriesCallbacks = callbacks,
                )
            }
        }
        rule.waitForIdle()
    }

    private val ready = EpisodesState.Ready(listOf(EpisodeRow(1, "Pilot", null, played = true, playedLabel = "Watched", progress = 0.5f)))

    @Test
    fun `a show with several seasons draws the dropdown`() {
        show(ready)

        rule.onNodeWithText("Season One").assertExists()
    }

    @Test
    fun `a show with one season draws no dropdown`() {
        show(ready, seasons = seasons.take(1))

        rule.onAllNodesWithText("Season One").assertCountEquals(0)
    }

    @Test
    fun `the episodes of the season are drawn with the played mark announced`() {
        show(ready)

        rule.onNodeWithText("Pilot").assertExists()
        rule.onNodeWithContentDescription("Watched", useUnmergedTree = true).assertExists()
    }

    @Test
    fun `failed episodes show the app's message and Try again reports a tap`() {
        var retries = 0
        show(
            EpisodesState.Failed("The episodes could not be read."),
            callbacks = SeriesDetailCallbacks(onSelectSeason = {}, onRetryEpisodes = { retries++ }),
        )

        rule.onNodeWithText("The episodes could not be read.").assertExists()
        rule.onNodeWithText("Try again").performClick()

        assertEquals(1, retries)
    }

    @Test
    fun `failed episodes that cannot be retried offer no Try again`() {
        show(EpisodesState.Failed("Gone.", retryable = false))

        rule.onAllNodesWithText("Try again").assertCountEquals(0)
    }
}
