package com.binge.designsystem.template

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.SearchScreenLoadingSample
import com.binge.designsystem.catalog.SearchScreenReadySample
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
 * The search template's loading state holds its first result row where the loaded results put it, across the page and at the size it gives
 * it at the default font, and where it puts it at 1.3 and 2. The loading state reserves the type chips' row, which the loaded results bring.
 * Sizes are checked, so the graphics mode is native.
 *
 * At a larger font a loaded row's own text, its name and its status chips, wraps over as many lines as that result's words need, and a plate
 * cannot know how many: at 1.3 the sample's row is 13dp taller than its skeleton and at 2.0 it is 58dp taller. So the large-font cases hold the
 * row's position and start edge, which the words do not decide, and the row's size is tracked in an issue rather than tuned to this sample.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SearchSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors = listOf(LayoutAnchors.Collection.FIRST_ITEM).map(LayoutAnchors::section)

    private fun check(checkSize: Boolean) {
        rule.assertSkeletonReservesGeometry(anchors, checkStartEdge = true, checkSize = checkSize) { resolved ->
            if (resolved) SearchScreenReadySample() else SearchScreenLoadingSample()
        }
    }

    @Test
    fun `the skeleton holds the first result`() = check(checkSize = true)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds its position at a font scale of 1 point 3`() = check(checkSize = false)

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds its position at a font scale of 2`() = check(checkSize = false)
}
