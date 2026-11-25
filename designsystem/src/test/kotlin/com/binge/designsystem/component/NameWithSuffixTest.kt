package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.assertNoTextClipped
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The name and the suffix each keep their words whole at any font size, as a requests screen needs at the largest one. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w412dp-h900dp")
class NameWithSuffixTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private fun check(fontScale: Float, width: Int) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = fontScale)) {
                TestTheme {
                    Box(Modifier.width(width.dp)) { NameWithSuffix(name = "Peaky Blinders", suffix = "Seasons 1, 2, 4–6") }
                }
            }
        }
        rule.waitForIdle()
        assertTrue("nothing was checked", rule.assertNoTextClipped() >= 2)
    }

    @Test fun `no word is broken at the default font size`() = check(fontScale = 1f, width = 280)

    @Test fun `no word is broken at 1_3`() = check(fontScale = 1.3f, width = 240)

    @Test fun `no word is broken at 2_0 in a narrow row`() = check(fontScale = 2f, width = 240)
}
