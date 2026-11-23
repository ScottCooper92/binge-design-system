package com.binge.designsystem.template

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import com.binge.designsystem.catalog.PagedGridLoadingSample
import com.binge.designsystem.catalog.PagedGridReadySample
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
 * The paged grid's loading state holds its first cell where the loaded grid puts it, across the page and at the size it gives it, at a
 * font scale of 1, 1.3 and 2. A cell is checked for size as well as position, and for its start edge, since a grid whose cells are the wrong
 * width starts at the right corner and puts every cell after the first somewhere else. Sizes are checked, so the graphics mode is native.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PagedGridSkeletonGeometryTest {
    @get:Rule
    val rule: ComposeContentTestRule = createTakeDownComposeRule()

    private val anchors = listOf(LayoutAnchors.Collection.FIRST_ITEM).map(LayoutAnchors::section)

    private fun check() {
        rule.assertSkeletonReservesGeometry(anchors, checkStartEdge = true, checkSize = true) { resolved ->
            if (resolved) PagedGridReadySample() else PagedGridLoadingSample()
        }
    }

    @Test
    fun `the skeleton holds the first cell`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 1.3f)
    fun `the skeleton holds it at a font scale of 1 point 3`() = check()

    @Test
    @Config(qualifiers = "w411dp-h891dp-xhdpi", fontScale = 2f)
    fun `the skeleton holds it at a font scale of 2`() = check()
}
