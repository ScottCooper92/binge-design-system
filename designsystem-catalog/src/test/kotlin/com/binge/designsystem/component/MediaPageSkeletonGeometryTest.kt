package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.MediaHeroDetailPageSample
import com.binge.designsystem.catalog.MediaHeroDetailPageWideSample
import com.binge.designsystem.catalog.MediaPageSkeletonSample
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.assertSkeletonReservesGeometry
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The media page's skeleton holds the hero, the stat rows and the overview where the loaded page puts them, and at the size it gives
 * them, at a font scale of 1, 1.3 and 2; at the expanded width the header carries the stats and the synopsis, so only the hero is the
 * page's. Sizes are checked, so the graphics mode is native: legacy mode measures text at one pixel per character.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaPageSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors =
        listOf(LayoutAnchors.Detail.HERO, LayoutAnchors.Detail.STATS, LayoutAnchors.Detail.OVERVIEW).map(LayoutAnchors::section)

    private fun check() {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = true) { resolved ->
            if (resolved) MediaHeroDetailPageSample() else MediaPageSkeletonSample()
        }
    }

    @Test
    fun `the skeleton holds the page's first anchors`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds them at a font scale of 1 point 3`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds them at a font scale of 2`() = check()

    @Test
    @Config(qualifiers = "w840dp-h891dp-xhdpi")
    fun `the skeleton holds the hero at the expanded width`() {
        rule.assertSkeletonReservesGeometry(listOf(LayoutAnchors.section(LayoutAnchors.Detail.HERO)), checkSize = true) { resolved ->
            if (resolved) MediaHeroDetailPageWideSample() else MediaPageSkeletonSample()
        }
    }
}
