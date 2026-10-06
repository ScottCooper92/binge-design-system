package com.binge.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The system runs light under Robolectric's default `notnight`, so a dark answer can only come from the theme. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeThemeIsDarkTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun anAppForcedDarkReadsDarkWhateverTheSystemSays() {
        assertEquals(true, isDarkUnder { content -> BingeExpressiveTheme(darkTheme = true, content = content) })
    }

    @Test
    fun anAppForcedLightReadsLight() {
        assertEquals(false, isDarkUnder { content -> BingeExpressiveTheme(darkTheme = false, content = content) })
    }

    @Test
    fun outsideTheThemeADarkSurfaceReadsDark() {
        assertEquals(true, isDarkUnder { content -> MaterialTheme(colorScheme = darkColorScheme(), content = content) })
    }

    @Test
    fun outsideTheThemeALightSurfaceReadsLight() {
        assertEquals(false, isDarkUnder { content -> MaterialTheme(colorScheme = lightColorScheme(), content = content) })
    }

    private fun isDarkUnder(theme: @Composable (@Composable () -> Unit) -> Unit): Boolean? {
        var isDark: Boolean? = null
        composeTestRule.setContent { theme { isDark = BingeTheme.isDark } }
        composeTestRule.waitForIdle()
        return isDark
    }
}
