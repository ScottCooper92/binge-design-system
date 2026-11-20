package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * `TvGridPage`'s skeleton reserves what its resolved grid fills.
 *
 * This one assertion covers the widest surface on the TV app. Every "see all" destination is a `TvGridPage`:
 * Discover, the movies and shows see-all grids, a movie collection, the library grid, and the Lists detail
 * pane — seven production call sites of `TvMediaGridSkeleton`. They differ in heading and empty copy, not in
 * geometry, so the check belongs here rather than repeated per screen.
 *
 * The anchor is the **first cell**, not the grid container: the container fills the pane, so its top never
 * moves and anchoring there would pass whatever the cells did. Every inset the skeleton must reproduce lands
 * above the first cell — and the pane's own KDoc records a skeleton that reserved the vertical overscan
 * instead of the row gap and dropped every plate by 11dp, which is precisely this measurement.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvGridSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `the skeleton reserves the geometry the resolved grid fills`() {
        composeTestRule.assertSkeletonReservesGeometry(
            anchors = listOf(TvLayoutAnchors.GRID_FIRST_CELL),
            checkSize = true,
        ) { resolved ->
            BingeTvTheme(reduceMotion = true) {
                Box(modifier = Modifier.fillMaxSize()) {
                    TestGridPage(
                        heading = "Horror",
                        results = if (resolved) gridItems(ITEMS) else TvGridResults.Loading,
                        onItemClick = {},
                        onRetry = {},
                        emptyMessage = "No films here right now.",
                    )
                }
            }
        }
    }
}

private val ITEMS = List(12) { GridItem(id = it + 1, title = "Film $it") }
