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
import com.binge.designsystem.testing.TestTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val OverlayEnd = 24.dp

/**
 * The carousel skeleton and the filter chip row clear an end inset a shell publishes, as the loaded carousel and the
 * section header do (#512). With none published, each keeps the end it had.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class OverlayEndPaddingTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the carousel skeleton's end clears the published end inset`() {
        val (without, with) = endsWithAndWithoutOverlay { carouselSkeletonSides() }

        assertEquals(without + OverlayEnd, with)
    }

    @Test
    fun `the chip row's end clears the published end inset`() {
        val (without, with) = endsWithAndWithoutOverlay { filterChipRowPadding(NoInsets) }

        assertEquals(without + OverlayEnd, with)
    }

    /** The end padding [padding] gives with no overlay published, then with [OverlayEnd] published. */
    private fun endsWithAndWithoutOverlay(padding: @Composable () -> PaddingValues): Pair<Dp, Dp> {
        var without = 0.dp
        var with = 0.dp
        rule.setContent {
            TestTheme {
                without = padding().calculateEndPadding(LayoutDirection.Ltr)
                CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(end = OverlayEnd)) {
                    with = padding().calculateEndPadding(LayoutDirection.Ltr)
                }
            }
        }
        rule.waitForIdle()
        return without to with
    }
}
