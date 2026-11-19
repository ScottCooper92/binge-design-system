package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [detailInfoColumns]: two columns on a phone, falling back to one from a font scale of 1.5, with both sides of that
 * threshold checked. Medium and expanded widths keep their count at any font.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class DetailInfoColumnsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun columns(fontScale: Float): Int {
        var result = 0
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme(dynamicColor = false) { result = detailInfoColumns() }
            }
        }
        rule.waitForIdle()
        return result
    }

    @Config(qualifiers = "w360dp-h800dp")
    @Test fun `a phone has two columns at 1_0`() = assertEquals(2, columns(1.0f))

    @Config(qualifiers = "w360dp-h800dp")
    @Test fun `a phone has two columns at 1_3`() = assertEquals(2, columns(1.3f))

    @Config(qualifiers = "w360dp-h800dp")
    @Test fun `a phone has two columns just under the threshold`() = assertEquals(2, columns(1.49f))

    @Config(qualifiers = "w360dp-h800dp")
    @Test fun `a phone has one column at the threshold`() = assertEquals(1, columns(1.5f))

    @Config(qualifiers = "w360dp-h800dp")
    @Test fun `a phone has one column at 2_0`() = assertEquals(1, columns(2.0f))

    @Config(qualifiers = "w700dp-h800dp")
    @Test fun `a medium window keeps two columns at 2_0`() = assertEquals(2, columns(2.0f))

    @Config(qualifiers = "w900dp-h800dp")
    @Test fun `an expanded window keeps three columns at 2_0`() = assertEquals(3, columns(2.0f))
}
