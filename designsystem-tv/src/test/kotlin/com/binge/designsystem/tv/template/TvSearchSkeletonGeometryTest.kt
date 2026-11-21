package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownKeyboardComposeRule
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.testing.LeanbackRule
import com.binge.designsystem.tv.testing.MainThreadImageLoaderRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The skeleton's plates land where the resolved cells and suggestion rows do, so loading resolves as a fill.
 *
 * The anchors are the grid's first cell and the list's first row: each sits below the live field and beside the other
 * pane, so a shortfall in the field's height, the pane gap or the column width moves one of them. The first row's icon is
 * anchored too, so a plate drawn without one, or at the wrong size, fails.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSearchSkeletonGeometryTest {
    @get:Rule
    val composeTestRule = createTakeDownKeyboardComposeRule()

    /** Coil writes painter state off the composition thread, which can kill this run. */
    @get:Rule
    val imageLoaderRule = MainThreadImageLoaderRule()

    @get:Rule
    val leanback = LeanbackRule()

    @Test
    fun `the plates land where the cells and suggestion rows do`() {
        composeTestRule.assertSkeletonReservesGeometry(
            anchors = listOf(
                TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_CELL),
                TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_SUGGESTION),
                TvLayoutAnchors.entry(SEARCH_ANCHOR_FIRST_SUGGESTION_ICON),
            ),
            checkSize = true,
        ) { resolved ->
            BingeTvTheme(reduceMotion = true) {
                TvSearchContent(
                    uiState = if (resolved) tvSearchIdleUiState() else tvSearchLoadingIdleUiState(),
                    onQueryChange = {},
                    onSubmit = {},
                    onSuggestionSelected = {},
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
