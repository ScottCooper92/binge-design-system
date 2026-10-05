package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Pins which nav presentation [rememberBingeNavPresentation] resolves for representative windows.
 *
 * The choice lives in qualifier folders, so a renamed or moved folder changes it with no code
 * diff. Each case sets the window through Robolectric's qualifiers, which resolves resources the
 * way a device would.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeNavPresentationTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun resolved(): BingeNavPresentation {
        var presentation: BingeNavPresentation? = null
        composeTestRule.setContent { presentation = rememberBingeNavPresentation() }
        composeTestRule.waitForIdle()
        return checkNotNull(presentation)
    }

    @Test
    @Config(qualifiers = "sw411dp-w411dp-h891dp-port-xhdpi")
    fun `a portrait phone gets the floating bar`() {
        assertEquals(BingeNavPresentation.FloatingBar, resolved())
    }

    @Test
    @Config(qualifiers = "sw411dp-w891dp-h411dp-land-xhdpi")
    fun `a landscape phone gets the floating bar`() {
        assertEquals(BingeNavPresentation.FloatingBar, resolved())
    }

    @Test
    @Config(qualifiers = "sw852dp-w852dp-h883dp-port-xhdpi")
    fun `an unfolded foldable gets the floating bar`() {
        assertEquals(BingeNavPresentation.FloatingBar, resolved())
    }

    @Test
    @Config(qualifiers = "sw852dp-w883dp-h852dp-land-xhdpi")
    fun `an unfolded foldable turned sideways gets the floating bar`() {
        assertEquals(BingeNavPresentation.FloatingBar, resolved())
    }

    @Test
    @Config(qualifiers = "sw800dp-w800dp-h1280dp-port-xhdpi")
    fun `a portrait tablet gets the floating bar`() {
        assertEquals(BingeNavPresentation.FloatingBar, resolved())
    }

    @Test
    @Config(qualifiers = "sw800dp-w1280dp-h800dp-land-xhdpi")
    fun `a landscape tablet gets the custom rail`() {
        assertEquals(BingeNavPresentation.CustomRail, resolved())
    }

    @Test
    @Config(qualifiers = "sw720dp-w1100dp-h720dp-land-xhdpi")
    fun `a small landscape tablet keeps the custom rail`() {
        assertEquals(BingeNavPresentation.CustomRail, resolved())
    }
}
