package com.binge.designsystem

import android.graphics.Rect
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.window.layout.FoldingFeature
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import androidx.window.testing.layout.FoldingFeature as posedFold

/**
 * Both hinge observers against a fold that is actually there.
 *
 * `androidx.window:window-testing` publishes a `WindowLayoutInfo` through the same
 * `WindowInfoTracker` the production code subscribes to, which is what makes the folded branch
 * reachable from the JVM at all — [VerticalHingeTest] covers only the null answer every non-foldable
 * window gives. It is not the device pass #819 asks for: this proves the filter, the axis and the
 * pixel-to-dp conversion, not that real hardware reports the bounds assumed here.
 *
 * Every test asserts a hinge is *seen* before asserting one is absent. The publisher's flow does not
 * replay, so a value that arrives before the composition subscribes is dropped silently — and a
 * null-only assertion would pass just as well on a fold that never got through as on one correctly
 * filtered out.
 *
 * Window bounds are handed to the fold builder rather than read from the window, so the expected dp
 * follow from the px named here and the `xhdpi` density alone.
 *
 * One thing the harness cannot pose is a fold that does **not** separate: `FakeFoldingFeature` returns
 * `isSeparating = true` for every state, `FLAT` included, where the real `HardwareFoldingFeature`
 * derives it from the state and the hinge type. So the filter's negative case — a foldable opened flat
 * leaving the layout alone — is asserted here only in the weaker form of a fold that disappears.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h1000dp-xhdpi")
class PosedFoldTest {
    private val publisher = WindowLayoutInfoPublisherRule()
    private val compose = createComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(publisher).around(compose)

    private var horizontal: HorizontalHinge? = null
    private var vertical: VerticalHinge? = null

    @Test
    fun `a half-opened horizontal fold is a tabletop hinge and nothing else`() {
        observeBothAxes()

        publish(fold(FoldingFeature.Orientation.HORIZONTAL))

        assertEquals(HorizontalHinge(top = CREASE_START_DP, height = CREASE_SIZE_DP), horizontal)
        assertNull("a horizontal fold was read as a two-pane hinge", vertical)
    }

    @Test
    fun `a half-opened vertical fold is a two-pane hinge and nothing else`() {
        observeBothAxes()

        publish(fold(FoldingFeature.Orientation.VERTICAL))

        assertEquals(VerticalHinge(start = CREASE_START_DP, width = CREASE_SIZE_DP), vertical)
        assertNull("a vertical fold was read as tabletop", horizontal)
    }

    @Test
    fun `a window that stops reporting a fold stops reporting a hinge`() {
        observeBothAxes()
        publish(fold(FoldingFeature.Orientation.HORIZONTAL))
        assertEquals(HorizontalHinge(top = CREASE_START_DP, height = CREASE_SIZE_DP), horizontal)

        publishNoFeatures()

        assertNull("the hinge latched on the first fold and never let go", horizontal)
    }

    private fun observeBothAxes() {
        compose.setContent {
            horizontal = rememberHorizontalHinge()
            vertical = rememberVerticalHinge()
        }
        compose.waitForIdle()
    }

    private fun publish(fold: FoldingFeature) {
        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(fold)))
        compose.waitForIdle()
    }

    private fun publishNoFeatures() {
        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(emptyList()))
        compose.waitForIdle()
    }

    private fun fold(
        orientation: FoldingFeature.Orientation,
        state: FoldingFeature.State = FoldingFeature.State.HALF_OPENED,
    ): FoldingFeature =
        posedFold(
            windowBounds = Rect(0, 0, WINDOW_WIDTH_PX, WINDOW_HEIGHT_PX),
            center = CREASE_CENTRE_PX,
            size = CREASE_SIZE_PX,
            state = state,
            orientation = orientation,
        )

    private companion object {
        const val WINDOW_WIDTH_PX = 1600
        const val WINDOW_HEIGHT_PX = 2000

        /** One centre for both axes, so either orientation lands at the same dp on its own axis. */
        const val CREASE_CENTRE_PX = 1000
        const val CREASE_SIZE_PX = 40

        val CREASE_START_DP = 490.dp
        val CREASE_SIZE_DP = 20.dp
    }
}
