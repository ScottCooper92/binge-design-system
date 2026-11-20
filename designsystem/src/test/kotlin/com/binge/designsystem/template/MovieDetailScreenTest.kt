package com.binge.designsystem.template

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.catalog.MovieDetailScreenLoadingSample
import com.binge.designsystem.catalog.MovieDetailScreenSample
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
 * The movie template's loading state holds the hero, the stat rows, the action row and the overview where the loaded page puts them,
 * at the size it gives them, at a font scale of 1, 1.3 and 2; at the expanded width the header carries the stats and the synopsis, so
 * the hero is the part that is held. Sizes are checked, so the graphics mode is native.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MovieDetailScreenTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors =
        listOf(
            LayoutAnchors.Detail.HERO,
            LayoutAnchors.Detail.STATS,
            LayoutAnchors.Detail.ACTIONS,
            LayoutAnchors.Detail.OVERVIEW,
        ).map(LayoutAnchors::section)

    private fun check() {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = true) { resolved ->
            if (resolved) MovieDetailScreenSample() else MovieDetailScreenLoadingSample()
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

    @Test
    @Config(qualifiers = "w840dp-h891dp-xhdpi")
    fun `the skeleton holds the hero and the actions at the expanded width`() {
        val wide = listOf(LayoutAnchors.Detail.HERO, LayoutAnchors.Detail.ACTIONS).map(LayoutAnchors::section)
        rule.assertSkeletonReservesGeometry(wide, checkSize = true) { resolved ->
            if (resolved) MovieDetailScreenSample() else MovieDetailScreenLoadingSample()
        }
    }

    /** The rails' keys are kept across a configuration change, so a page with rails must survive being saved and restored. */
    @Test
    @Config(qualifiers = "w411dp-h2400dp-xhdpi")
    fun `a page with rails survives a configuration change`() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent { MovieDetailScreenSample() }
        rule.waitForIdle()

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        rule.onNodeWithText("Cast").assertExists()
    }
}
