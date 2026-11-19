package com.binge.designsystem.catalogapp.overrides

import androidx.compose.runtime.Composable
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.LocalReduceMotion
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A sample wraps itself in `ScreenshotTheme`, which switches animation off so a frame is one stable image. The catalog host
 * can run it with motion on, and a frame, which has no host, stays still.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h900dp-xxhdpi")
class SampleMotionTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var reduced: Boolean? = null

    @Composable
    private fun Sample() {
        ScreenshotTheme { reduced = LocalReduceMotion.current }
    }

    @Test
    fun `a sample with no host is still, as a screenshot frame renders it`() {
        rule.setContent { Sample() }
        rule.waitForIdle()

        assertTrue(reduced == true)
    }

    @Test
    fun `the host runs a sample with motion on`() {
        rule.setContent { WithOverrides(SampleOverrides(dark = false, fontScale = 1f, rtl = false, motion = true)) { Sample() } }
        rule.waitForIdle()

        assertFalse(reduced == true)
    }

    @Test
    fun `the host can switch motion off again`() {
        rule.setContent { WithOverrides(SampleOverrides(dark = false, fontScale = 1f, rtl = false, motion = false)) { Sample() } }
        rule.waitForIdle()

        assertTrue(reduced == true)
    }
}
