package com.binge.designsystem.template

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.PersonDetailLoadingSample
import com.binge.designsystem.catalog.PersonDetailReadySample
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
 * The person template's loading state holds the profile, the biography and the first credit rail's heading where the loaded page puts them,
 * and at the size it gives them, at the default font. Sizes are checked, so the graphics mode is native.
 *
 * At a larger font the profile's own name and facts wrap, over as many lines as that person's words need, and a plate cannot know how many
 * that is: at 1.3 the sample's profile is 30dp taller than its skeleton and at 2.0 it is 132dp taller, and everything under it moves by the
 * same amount. So the large-font cases hold only what the words do not decide, the profile's own top, and the remainder is tracked in an issue
 * rather than tuned to this one sample.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PersonDetailSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors =
        listOf(LayoutAnchors.Detail.PROFILE, LayoutAnchors.Detail.OVERVIEW, LayoutAnchors.Detail.CREDITS).map(LayoutAnchors::section)

    private fun check(anchors: List<String>, checkSize: Boolean) {
        rule.assertSkeletonReservesGeometry(anchors, checkSize = checkSize) { resolved ->
            if (resolved) PersonDetailReadySample() else PersonDetailLoadingSample()
        }
    }

    @Test
    fun `the skeleton holds the profile, the biography and the first credit heading`() = check(anchors, checkSize = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds the profile's top at a font scale of 1 point 3`() =
        check(listOf(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE)), checkSize = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds the profile's top at a font scale of 2`() =
        check(listOf(LayoutAnchors.section(LayoutAnchors.Detail.PROFILE)), checkSize = false)
}
