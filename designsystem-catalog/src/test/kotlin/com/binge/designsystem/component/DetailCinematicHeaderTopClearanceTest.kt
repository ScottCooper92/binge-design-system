package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.catalog.DetailCinematicHeaderLongCopySample
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The cinematic header's title starts below the overlay bar at a large font, where the header grows with its copy. The test
 * environment has no status bar inset, so it holds the title to the overlay bar's height, which the header reserves under it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    qualifiers = "w840dp-h8000dp",
    shadows = [ShadowMeshSpecification::class],
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DetailCinematicHeaderTopClearanceTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun check(fontScale: Float) {
        val bar = mutableStateOf(Dp.Unspecified)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                bar.value = detailOverlayBarHeight()
                Box(Modifier.width(840.dp)) { DetailCinematicHeaderLongCopySample() }
            }
        }
        rule.waitForIdle()
        val barPx = bar.value.value * rule.density.density
        val top = rule
            .onNodeWithText("The Lord of the Rings: The Fellowship of the Ring", useUnmergedTree = true)
            .fetchSemanticsNode()
            .boundsInRoot.top
        assertTrue("the title starts at $top px, above the overlay bar's $barPx px at font $fontScale", top >= barPx - 1f)
    }

    @Test fun `the title starts below the overlay bar at 1x`() = check(1f)

    @Test fun `the title starts below the overlay bar at 2x`() = check(2f)
}
