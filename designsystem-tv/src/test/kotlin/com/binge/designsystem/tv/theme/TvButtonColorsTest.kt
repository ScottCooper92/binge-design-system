package com.binge.designsystem.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.testing.createKeyboardComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TvButtonColorsTest {
    @get:Rule
    val rule = createKeyboardComposeRule()

    /** Composes once, so a test that compares two states reads both from the same theme. */
    private fun inTheme(read: @Composable (ColorScheme) -> Unit) {
        rule.setContent { BingeTvTheme { read(MaterialTheme.colorScheme) } }
        rule.waitForIdle()
    }

    @Test
    fun `destructive is a solid error fill at rest`() {
        lateinit var rest: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            rest = tvButtonColors(TvButtonStyle.Destructive, enabled = true, isFocused = false)
        }

        assertEquals(scheme.error, rest.container)
        assertEquals(scheme.onError, rest.content)
        assertEquals(Color.Transparent, rest.border)
    }

    @Test
    fun `focused destructive keeps its fill and shows focus with a border`() {
        lateinit var rest: TvButtonColors
        lateinit var focused: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            rest = tvButtonColors(TvButtonStyle.Destructive, enabled = true, isFocused = false)
            focused = tvButtonColors(TvButtonStyle.Destructive, enabled = true, isFocused = true)
        }

        assertEquals(rest.container, focused.container)
        assertEquals(scheme.onError, focused.border)
        assertNotEquals(rest.border, focused.border)
    }

    @Test
    fun `destructive outline rests as a red outline`() {
        lateinit var rest: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            rest = tvButtonColors(TvButtonStyle.DestructiveOutline, enabled = true, isFocused = false)
        }

        assertEquals(Color.Transparent, rest.container)
        assertEquals(scheme.error, rest.content)
        assertEquals(scheme.error, rest.border)
    }

    @Test
    fun `focused destructive outline fills solid error`() {
        lateinit var focused: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            focused = tvButtonColors(TvButtonStyle.DestructiveOutline, enabled = true, isFocused = true)
        }

        assertEquals(scheme.error, focused.container)
        assertEquals(scheme.onError, focused.content)
        assertEquals(Color.Transparent, focused.border)
    }

    @Test
    fun `a disabled red style is an outline that is not red`() {
        lateinit var solid: TvButtonColors
        lateinit var outline: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            solid = tvButtonColors(TvButtonStyle.Destructive, enabled = false, isFocused = false)
            outline = tvButtonColors(TvButtonStyle.DestructiveOutline, enabled = false, isFocused = false)
        }

        listOf(solid, outline).forEach { disabled ->
            assertEquals(Color.Transparent, disabled.container)
            assertEquals(scheme.onSurfaceVariant, disabled.content)
        }
    }

    @Test
    fun `a text button is its label alone at rest`() {
        lateinit var plain: TvButtonColors
        lateinit var red: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            plain = tvButtonColors(TvButtonStyle.Text, enabled = true, isFocused = false)
            red = tvButtonColors(TvButtonStyle.TextDestructive, enabled = true, isFocused = false)
        }

        listOf(plain, red).forEach { rest ->
            assertEquals(Color.Transparent, rest.container)
            assertEquals(Color.Transparent, rest.border)
        }
        assertEquals(scheme.onSurface, plain.content)
        assertEquals(scheme.error, red.content)
    }

    @Test
    fun `a focused text button fills with the accent, or with error when destructive`() {
        lateinit var plain: TvButtonColors
        lateinit var red: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            plain = tvButtonColors(TvButtonStyle.Text, enabled = true, isFocused = true)
            red = tvButtonColors(TvButtonStyle.TextDestructive, enabled = true, isFocused = true)
        }

        assertEquals(scheme.primary, plain.container)
        assertEquals(scheme.onPrimary, plain.content)
        assertEquals(scheme.error, red.container)
        assertEquals(scheme.onError, red.content)
        listOf(plain, red).forEach { assertEquals(Color.Transparent, it.border) }
    }

    @Test
    fun `a disabled text button is a dimmed label with no edge until focused`() {
        lateinit var plain: TvButtonColors
        lateinit var red: TvButtonColors
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            plain = tvButtonColors(TvButtonStyle.Text, enabled = false, isFocused = false)
            red = tvButtonColors(TvButtonStyle.TextDestructive, enabled = false, isFocused = false)
        }

        listOf(plain, red).forEach { disabled ->
            assertEquals(Color.Transparent, disabled.container)
            assertEquals(Color.Transparent, disabled.border)
            assertEquals(scheme.onSurfaceVariant, disabled.content)
        }
    }

    @Test
    fun `a focused disabled control draws a full-alpha edge and no fill, on every style`() {
        val rest = mutableMapOf<TvButtonStyle, TvButtonColors>()
        val focused = mutableMapOf<TvButtonStyle, TvButtonColors>()
        lateinit var scheme: ColorScheme
        inTheme {
            scheme = it
            TvButtonStyle.entries.forEach { style ->
                rest[style] = tvButtonColors(style, enabled = false, isFocused = false)
                focused[style] = tvButtonColors(style, enabled = false, isFocused = true)
            }
        }

        val red = setOf(TvButtonStyle.Destructive, TvButtonStyle.DestructiveOutline, TvButtonStyle.TextDestructive)
        TvButtonStyle.entries.forEach { style ->
            val edge = if (style in red) scheme.error else scheme.border
            assertEquals(style.name, edge, focused.getValue(style).border)
            assertEquals(style.name, Color.Transparent, focused.getValue(style).container)
            assertEquals(style.name, scheme.onSurfaceVariant, focused.getValue(style).content)
            assertNotEquals(style.name, rest.getValue(style).border, focused.getValue(style).border)
        }
    }
}
