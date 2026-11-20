package com.binge.designsystem.tv.template

import androidx.compose.runtime.Composable
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A heroless hub's first row holds its place through every state before the titles land: no pages yet, and a count whose first
 * page is still paging in (pages exist, `at` answers null). The plates beside the slot must not leave and come back between them.
 *
 * `NATIVE` graphics because the row's heading and details hold text.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvExpandingRowPlateGeometryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `the plates beside the slot stay where they are while the first page pages in`() {
        composeTestRule.assertSkeletonReservesGeometry(
            anchors = listOf(TvExpandingAnchors.OPEN_SLOT, TvExpandingAnchors.OPEN_FIRST_POSTER),
            checkStartEdge = true,
            checkSize = true,
        ) { pagingIn -> Hub(pagingIn) }
    }

    @Composable
    private fun Hub(pagingIn: Boolean) {
        BingeTvTheme(reduceMotion = true) {
            TvExpandingRowsHub(
                rows = listOf(TvHubRow<TvHubItem>(key = "row", title = "Row", items = emptyList(), isLoading = true)),
                onItemClick = {},
                rowPages = { if (pagingIn) TvRowPages(count = PAGED_COUNT, endReached = true, at = { null }) else null },
            )
        }
    }

    private companion object {
        const val PAGED_COUNT = 20
    }
}
