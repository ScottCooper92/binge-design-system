package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.catalog.FilterFixture
import com.binge.designsystem.tv.catalog.SamplePaneCopy
import com.binge.designsystem.tv.component.TvPosterCell
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The filter page's shell, before it has a state, reserves what the resolved page fills.
 *
 * The anchors are the pane's first category row, which sits under the header and so moves if the header's plate is the wrong
 * height, and the grid's first cell, which sits beside the pane and so moves if the pane gap or width is wrong. Both are one
 * cell or one row, so each is held to its size as well as its place.
 *
 * `NATIVE` graphics because the category row holds two lines of text, which legacy mode measures at one pixel per character.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TvFilterGridPageGeometryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `the pane and the grid land where the resolved page's do`() {
        composeTestRule.assertSkeletonReservesGeometry(
            anchors = listOf(
                TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CATEGORY),
                TvLayoutAnchors.entry(TV_FILTER_ANCHOR_FIRST_CELL),
            ),
            checkSize = true,
        ) { resolved ->
            BingeTvTheme(reduceMotion = true) {
                if (resolved) {
                    TvFilterGridPage(
                        categories = FilterFixture().categories(),
                        paneCopy = SamplePaneCopy,
                        gridCopy = TvFilterGridCopy(empty = "Nothing here", stale = "Stale", retry = "Try again"),
                        filterCount = 0,
                        results = TvGridResults.Items(count = 12, key = { it }, at = { it }),
                        cell = { _, id -> TvPosterCell(null, "Film $id", onClick = {}) },
                        listKey = null,
                        onClearAll = {},
                        onToggle = { _, _ -> },
                        onClearCategory = {},
                        onRetry = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    TvFilterGridPageLoading(Modifier.fillMaxSize())
                }
            }
        }
    }
}
