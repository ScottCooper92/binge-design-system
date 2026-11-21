package com.binge.designsystem.template

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.MEDIA_PAGE_OVERVIEW
import com.binge.designsystem.catalog.MEDIA_PAGE_TITLE
import com.binge.designsystem.catalog.MediaPageStats
import com.binge.designsystem.component.BingeStatusButton
import com.binge.designsystem.component.BingeStatusButtonEmphasis
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeSentiment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The series template's loading state holds the hero, the stat rows, the action row and the overview where the loaded page puts them, and
 * at the size it gives them, at a font scale of 1, 1.3 and 2. The episodes section is the loaded page's alone: the loading state is the
 * page's skeleton, which stops at the overview. Sizes are checked, so the graphics mode is native.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SeriesDetailSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors =
        listOf(
            LayoutAnchors.Detail.HERO,
            LayoutAnchors.Detail.STATS,
            LayoutAnchors.Detail.ACTIONS,
            LayoutAnchors.Detail.OVERVIEW,
        ).map(LayoutAnchors::section)

    private fun check(
        overview: String = MEDIA_PAGE_OVERVIEW,
        loading: DetailState.Loading = DetailState.Loading(stats = MediaPageStats.size, roundActions = 0, hasPrimary = true),
    ) {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = true) { resolved ->
            ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
                SeriesDetailScreen(
                    state = if (resolved) {
                        DetailState.Content(
                            MediaDetail(title = MEDIA_PAGE_TITLE, stats = MediaPageStats, overview = overview),
                        )
                    } else {
                        loading
                    },
                    series = null,
                    callbacks = MediaDetailCallbacks(onBack = {}),
                    seriesCallbacks = SeriesDetailCallbacks(onSelectSeason = {}),
                    primary = { modifier ->
                        BingeStatusButton(
                            label = "Play S1 · E1",
                            onClick = {},
                            sentiment = BingeSentiment.Neutral,
                            modifier = modifier,
                            icon = Icons.Filled.PlayArrow,
                            emphasis = BingeStatusButtonEmphasis.Filled,
                        )
                    },
                )
            }
        }
    }

    @Test
    fun `the skeleton holds the page's anchors`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds them at a font scale of 1 point 3`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds them at a font scale of 2`() = check()

    private val shortLoading =
        DetailState.Loading(stats = MediaPageStats.size, roundActions = 0, hasPrimary = true, hasOverviewToggle = false)

    @Test
    fun `an overview that fits its lines holds no toggle`() = check(OVERVIEW_THREE_LINES, shortLoading)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `an overview that fits its lines holds no toggle at a font scale of 1 point 3`() = check(OVERVIEW_THREE_LINES_LARGE, shortLoading)
}
