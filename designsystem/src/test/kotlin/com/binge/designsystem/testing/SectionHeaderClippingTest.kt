package com.binge.designsystem.testing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** A long section title at a large font wraps inside what the "More" action leaves, instead of squeezing it away. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h8000dp")
// Native graphics, because legacy graphics measures text at a pixel a character and nothing would wrap.
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SectionHeaderClippingTest {
    @get:Rule
    val rule = createComposeRule()

    private fun check(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                BingeExpressiveTheme {
                    Box(Modifier.width(412.dp)) { SectionHeader(title = LONG_TITLE, onMoreClick = {}) }
                }
            }
        }
        rule.waitForIdle()
        val checked = rule.assertNoTextClipped()
        assertTrue("only $checked text nodes were checked", checked >= 2)
    }

    @Test fun `a long title with More at 1_3`() = check(1.3f)

    @Test fun `a long title with More at 2_0`() = check(2.0f)

    private companion object {
        const val LONG_TITLE = "Popular on your streaming services"
    }
}
