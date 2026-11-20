package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PLAIN = "plain"
private const val MARKED = "marked"
private const val FAILED_APPROVE = "Couldn't approve"
private const val FAILED_DECLINE = "Couldn't decline"
private val FONT_SCALES = listOf(1f, 1.3f, 2f)

/**
 * #4345: a "Waiting for approval" card marked as failed reads its failure in the meta line's own slot, so the details under the card
 * are as tall with the mark as without it at every font size, and nothing below them moves when the mark comes or goes.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvExpandingDecisionFailureHeightTest {
    @get:Rule
    val composeTestRule = createTakeDownComposeRule()

    @Test
    fun `the details are as tall with the mark as without it, at every font size`() {
        show(FAILED_APPROVE)
        FONT_SCALES.forEach { scale ->
            fontScale = scale
            composeTestRule.waitForIdle()

            assertEquals("font scale $scale", height(PLAIN), height(MARKED))
        }
    }

    @Test
    fun `a failed decline reads its own words`() {
        show(FAILED_DECLINE)

        composeTestRule.onNodeWithText(FAILED_DECLINE).assertExists()
    }

    @Test
    fun `an unmarked card reads its facts, and a marked one reads the failure in their place`() {
        show(FAILED_APPROVE)

        composeTestRule.onNodeWithText(FAILED_APPROVE).assertExists()
        composeTestRule.onNodeWithText("Drama", substring = true).assertExists()
    }

    private var fontScale by mutableFloatStateOf(1f)

    private fun height(tag: String) = composeTestRule.onNodeWithTag(tag).getUnclippedBoundsInRoot().let { it.bottom - it.top }

    private fun show(failure: String) {
        composeTestRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                BingeTvTheme {
                    Column {
                        Box(Modifier.testTag(PLAIN)) { TvExpandingDetails(card(null), certification = "PG-13", metaWidth = 600.dp) }
                        Box(Modifier.testTag(MARKED)) { TvExpandingDetails(card(failure), certification = "PG-13", metaWidth = 600.dp) }
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun card(failure: String?) =
        TvHubItem(
            key = "movie_11",
            posterUrl = null,
            title = "Dune",
            rating = 8.1f,
            genres = listOf("Drama"),
            year = "2021",
            overview = "A long synopsis that runs past its three lines so the details fill all of them, whatever the font size.",
            failure = failure,
        )
}
