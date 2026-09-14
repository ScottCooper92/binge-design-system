package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.component.FilterChipItem
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The chip row clears an overlaying expanded nav rail.
 *
 * On a tablet or unfolded foldable the rail overlays content rather than reserving width beside it, so
 * every screen under the shell opts out of the covered strip individually. Search's field above the
 * chips and its result grids below both did; the chips did not, and the leading chip rendered against
 * the rail's glass.
 *
 * Driven through `LocalNavOverlayInsets` directly rather than by composing the shell: the row is a
 * design-system component and the inset is the whole contract between it and the rail, so the shell
 * would only add a second thing that could fail.
 *
 * [FilterChipRowSkeleton] is not asserted separately — it reads the same [filterChipRowPadding], so
 * there is no second value to check.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "sw600dp-w840dp-h782dp-xhdpi")
class BingeFilterChipRowOverlayInsetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun chipRow(overlayStart: Dp) {
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(start = overlayStart)) {
                    BingeFilterChipRow(items = ITEMS, selectedIndex = 0, onSelect = {})
                }
            }
        }
    }

    private fun firstChipStart(): Dp = composeTestRule.onNodeWithText(FIRST_LABEL).getBoundsInRoot().left

    /**
     * Resolved rather than hardcoded: `screen_content_inset` ramps with width (16 / 24 / 32dp) and this
     * class runs at `w840dp`, so a literal would pin the phone value on a canvas that never uses it.
     */
    private fun edgePadding(): Dp {
        val resources = RuntimeEnvironment.getApplication().resources
        return (resources.getDimension(R.dimen.screen_content_inset) / resources.displayMetrics.density).dp
    }

    @Test
    fun `with no rail overlaying, the row keeps its own edge padding`() {
        chipRow(overlayStart = 0.dp)
        assertEquals(edgePadding().value, firstChipStart().value, TOLERANCE)
    }

    @Test
    fun `an overlaying rail pushes the leading chip clear of it`() {
        chipRow(overlayStart = RAIL_WIDTH)
        assertEquals((edgePadding() + RAIL_WIDTH).value, firstChipStart().value, TOLERANCE)
    }

    private companion object {
        const val FIRST_LABEL = "All"
        val ITEMS = listOf(
            FilterChipItem(label = FIRST_LABEL, count = 12),
            FilterChipItem(label = "Movies", count = 5),
            FilterChipItem(label = "TV", count = 7),
        )

        /** `nav_custom_rail_width` — what the expanded rail publishes when it is overlaying. */
        val RAIL_WIDTH = 96.dp

        /** Rounding between dp and the px the layout is measured in. */
        const val TOLERANCE = 1f
    }
}
