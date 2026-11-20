package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.height
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.component.TvExpandingMeta
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w960dp-h540dp-television-xhdpi", application = android.app.Application::class)
class TvExpandingMetaHeightTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @Test
    fun `the meta line is as tall without a rating as with one`() {
        val heights = heightsOf(Meta(rating = RATING), Meta(rating = null))

        assertTrue(heights.first().value > 0f)
        assertEquals(heights.first(), heights.last())
    }

    @Test
    fun `the meta line is as tall with only a certification as with a rating`() {
        val heights = heightsOf(Meta(rating = RATING), Meta(certification = "12A"))

        assertEquals(heights.first(), heights.last())
    }

    private class Meta(
        val rating: Float? = null,
        val certification: String? = null,
    )

    private fun heightsOf(vararg lines: Meta): List<Dp> {
        composeTestRule.setContent {
            BingeTvTheme {
                Column {
                    lines.forEachIndexed { index, line ->
                        TvExpandingMeta(
                            genres = listOf("Drama"),
                            trailingFacts = listOf("2024"),
                            rating = line.rating,
                            certification = line.certification,
                            modifier = Modifier.testTag("$TAG$index"),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        return lines.indices.map { composeTestRule.onNodeWithTag("$TAG$it").getUnclippedBoundsInRoot().height }
    }

    private companion object {
        const val TAG = "meta"
        const val RATING = 8.1f
    }
}
