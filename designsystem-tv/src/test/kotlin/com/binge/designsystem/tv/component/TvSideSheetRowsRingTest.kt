package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.binge.designsystem.tv.R as TvR

private const val FIRST = "first-ring"
private const val LAST = "last-ring"
private const val ROWS = 12
private const val TOLERANCE = 0.5f

/**
 * A ringed control in a sheet's rows draws its focus ring outside its bounds, so the rows' scroll must not cut it off: not
 * at the top with the list at rest, and not at the bottom once scrolled to the end. Each probe is the band a ring takes,
 * laid out as a node, so clipping shows as its clipped bounds falling short of its size.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvSideSheetRowsRingTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the first and last rings show in full`() = checkRings(fontScale = 1f)

    @Test
    fun `the first and last rings show in full at 1_3x font scale`() = checkRings(fontScale = 1.3f)

    @Test
    fun `the first and last rings show in full at 2x font scale`() = checkRings(fontScale = 2f)

    @Test
    fun `the first and last rings show in full on a panel to the bottom edge`() = checkRings(fontScale = 1f, toBottomEdge = true)

    private fun checkRings(fontScale: Float, toBottomEdge: Boolean = false) {
        val scroll = ScrollState(0)
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeTvTheme {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        TvSideSheetPanel(toBottomEdge = toBottomEdge) {
                            TvSideSheetTitle("Server")
                            TvSideSheetRows(scrollState = scroll) {
                                repeat(ROWS) { index ->
                                    val tag =
                                        when (index) {
                                            0 -> FIRST
                                            ROWS - 1 -> LAST
                                            else -> null
                                        }
                                    RingProbe(tag)
                                }
                            }
                        }
                    }
                }
            }
        }
        rule.waitForIdle()
        assertTrue("the probes should outgrow the panel, so the end needs a scroll", scroll.maxValue > 0)

        assertNotClipped(FIRST)

        rule.runOnIdle { runBlocking { scroll.scrollTo(scroll.maxValue) } }
        rule.waitForIdle()

        assertNotClipped(LAST)
    }

    private fun assertNotClipped(tag: String) {
        val node = rule.onNodeWithTag(tag).fetchSemanticsNode()
        // Height only: the scroll's clip reaches past its sides, which boundsInRoot, clipping to the plain rectangle, cannot see.
        assertEquals("$tag is clipped", node.size.height.toFloat(), node.boundsInRoot.height, TOLERANCE)
    }
}

/** A control's bounds, with the band its focus ring takes as a node reaching that far outside them. */
@Composable
private fun RingProbe(tag: String?) {
    val ring = dimensionResource(TvR.dimen.tv_focus_ring_offset) + dimensionResource(TvR.dimen.tv_focus_ring_width)
    Box(Modifier.fillMaxWidth().height(56.dp)) {
        if (tag != null) Box(Modifier.matchParentSize().outset(ring).testTag(tag))
    }
}

private fun Modifier.outset(by: Dp): Modifier =
    layout { measurable, constraints ->
        val px = by.roundToPx()
        val placeable = measurable.measure(Constraints.fixed(constraints.maxWidth + px * 2, constraints.maxHeight + px * 2))
        layout(constraints.maxWidth, constraints.maxHeight) { placeable.place(-px, -px) }
    }
