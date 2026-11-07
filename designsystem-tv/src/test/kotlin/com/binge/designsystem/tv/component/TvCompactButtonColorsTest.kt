package com.binge.designsystem.tv.component

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.theme.tvButtonColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [tvCompactButtonColors] is public so a control that must look like the compact buttons can take their palette. These
 * pin what it hands out: the white focus fill, the faint white resting primary, and the shared palette for the rest.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvCompactButtonColorsTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    /** Reads [read] inside one composition, with the theme's colour scheme, because a rule takes one `setContent` per test. */
    private fun <T> inTheme(read: @Composable (ColorScheme) -> T): T {
        var result: T? = null
        rule.setContent { BingeTvTheme { result = read(MaterialTheme.colorScheme) } }
        rule.waitForIdle()
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    @Test
    fun `a focused primary or secondary fills white with dark content`() {
        val (scheme, palettes) =
            inTheme { scheme ->
                scheme to
                    listOf(TvButtonStyle.Primary, TvButtonStyle.Secondary).map {
                        tvCompactButtonColors(style = it, enabled = true, isFocused = true)
                    }
            }

        palettes.forEach {
            assertEquals(scheme.onSurface, it.container)
            assertEquals(scheme.surface, it.content)
            assertEquals(scheme.onSurface, it.border)
        }
    }

    @Test
    fun `a resting primary is a faint white pill with no border`() {
        val (scheme, palette) = inTheme { it to tvCompactButtonColors(style = TvButtonStyle.Primary, enabled = true, isFocused = false) }

        assertEquals(scheme.onSurface.copy(alpha = 0.2f), palette.container)
        assertEquals(scheme.onSurface, palette.content)
        assertEquals(Color.Transparent, palette.border)
    }

    @Test
    fun `a disabled button and the destructive style keep the shared palette`() {
        val (compact, shared) =
            inTheme {
                val styles = listOf(TvButtonStyle.Primary to false, TvButtonStyle.Destructive to true)
                styles.map { (style, enabled) -> tvCompactButtonColors(style, enabled, isFocused = true) } to
                    styles.map { (style, enabled) -> tvButtonColors(style, enabled, isFocused = true) }
            }

        assertEquals(shared, compact)
        assertNotEquals(compact[0], compact[1])
    }
}
