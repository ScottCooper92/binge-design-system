package com.binge.designsystem.tv.theme

import android.app.Application
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.theme.BingeTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Under the dark-only TV theme, [BingeTheme.isDark] is true (#828). It once fell back to Material 3's theme, which
 * this tree does not provide, and read the baseline light surface as "not dark".
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvThemeDarkTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `BingeTheme isDark is true under BingeTvTheme`() {
        var dark: Boolean? = null
        rule.setContent { BingeTvTheme { dark = BingeTheme.isDark } }
        rule.waitForIdle()

        assertTrue(dark == true)
    }
}
