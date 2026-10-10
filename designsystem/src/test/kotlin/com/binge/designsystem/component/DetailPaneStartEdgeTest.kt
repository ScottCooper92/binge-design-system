package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.LocalPaneInnerEdge
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.R
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val TITLE = "Dune: Part Two"
private const val INFO_LABEL = "Director"
private const val STAT_VALUE = "8.4"
private const val TOLERANCE = 1f

/**
 * The components under a detail hero take their side padding from the pane, as the hero does: in the end pane the
 * edge shared with the list gets the inner inset and the window-edge inset is 16dp at this width, so each content edge
 * moves in by 8dp (#534). A lone stat is centred in the row, so it moves by half that.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailPaneStartEdgeTest {
    @get:Rule
    val rule = createComposeRule()

    private var innerEdge by mutableStateOf<PaneEdge?>(null)

    private fun leftOf(text: String): Dp = rule.onNodeWithText(text).getUnclippedBoundsInRoot().left

    @Test
    fun `in the end pane the hero, rating card, info rows and stat row all move in from the shared edge`() {
        rule.setContent {
            TestTheme {
                CompositionLocalProvider(LocalPaneInnerEdge provides innerEdge) {
                    // Scrolls, so each component lays out at its own height instead of being squeezed by the hero.
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        DetailHero(title = TITLE, backdropUrl = null, tagline = null, metaText = "2024", onBack = {}, showChrome = false)
                        DetailStatRow(stats = listOf(DetailStat(Icons.Filled.Star, STAT_VALUE, "Rating")))
                        RatingCard(
                            userRating = null,
                            isSignedIn = true,
                            reviewCount = 0,
                            averageReviewRating = null,
                            onRate = {},
                            onRemoveRating = {},
                            onReviewsClick = {},
                        )
                        InfoRowList(listOf(InfoRowEntry(INFO_LABEL, "Denis Villeneuve")))
                    }
                }
            }
        }
        val prompt = RuntimeEnvironment.getApplication().getString(R.string.rating_card_prompt_label)
        rule.waitForIdle()
        val alone = listOf(TITLE, STAT_VALUE, prompt, INFO_LABEL).map(::leftOf)

        innerEdge = PaneEdge.Start
        rule.waitForIdle()
        val inPane = listOf(TITLE, STAT_VALUE, prompt, INFO_LABEL).map(::leftOf)

        val moved = alone.zip(inPane) { before, after -> (before - after).value }
        assertEquals("hero title", 8f, moved[0], TOLERANCE)
        assertEquals("a lone stat, centred", 4f, moved[1], TOLERANCE)
        assertEquals("rating card", 8f, moved[2], TOLERANCE)
        assertEquals("info row", 8f, moved[3], TOLERANCE)
    }
}
