package com.binge.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TOLERANCE = 0.01f
private val OverlayEnd = 24.dp

/**
 * The carousel skeleton and the filter chip row clear an end inset a shell or hub publishes, as MediaCarousel and
 * SectionHeader do (#512). Read from the padding functions both call, because the end term shows only once a row is
 * scrolled to its end.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class OverlayEndPaddingTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the carousel skeleton's end padding grows by the published end inset`() = assertGrowsByTheOverlay { carouselSkeletonSidePadding() }

    /** No pane beside it here, so the side insets are zero and the overlay is the larger term. */
    @Test
    fun `the chip row's end padding grows by the published end inset`() = assertGrowsByTheOverlay { filterChipRowPadding(NoInsets) }

    /** Reads [padding]'s end with no overlay and with one, in one composition, and expects them [OverlayEnd] apart. */
    private fun assertGrowsByTheOverlay(padding: @Composable () -> PaddingValues) {
        var without = Dp.Unspecified
        var with = Dp.Unspecified
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                without = padding().calculateEndPadding(LayoutDirection.Ltr)
                CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(end = OverlayEnd)) {
                    with = padding().calculateEndPadding(LayoutDirection.Ltr)
                }
            }
        }
        rule.waitForIdle()

        assertEquals((without + OverlayEnd).value, with.value, TOLERANCE)
    }
}
