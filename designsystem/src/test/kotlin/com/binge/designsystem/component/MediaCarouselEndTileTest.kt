package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The trailing tile of a [MediaCarousel]: poster height unless the caller gives its own. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class MediaCarouselEndTileTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    // "See all" is on the header link too; the end tile is the second.
    private fun endTileHeightPx(): Int {
        val endTile = rule.onAllNodesWithText("See all")[1]
        return endTile.fetchSemanticsNode().size.height
    }

    @Test
    fun `the trailing tile is poster height by default`() {
        rule.setContent {
            TestTheme { MediaCarousel(title = "Row", items = listOf(1), itemKey = { it }, onMoreClick = {}) { _, _ -> Box { Text("x") } } }
        }
        rule.waitForIdle()

        val density = rule.density.density
        assertEquals(216f, endTileHeightPx() / density, 1f)
    }

    @Test
    fun `the trailing tile takes the height the caller gives`() {
        rule.setContent {
            TestTheme {
                MediaCarousel(
                    title = "Row",
                    items = listOf(1),
                    itemKey = { it },
                    onMoreClick = {},
                    endTileHeight = 100.dp,
                ) { _, _ -> Box { Text("x") } }
            }
        }
        rule.waitForIdle()

        val density = rule.density.density
        assertEquals(100f, endTileHeightPx() / density, 1f)
    }
}
