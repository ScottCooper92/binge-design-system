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
            solid = tvButtonColors(TvButtonStyle.Destructive, enabled = false, isFocused = true)
            outline = tvButtonColors(TvButtonStyle.DestructiveOutline, enabled = false, isFocused = true)
        }

        listOf(solid, outline).forEach { disabled ->
            assertEquals(Color.Transparent, disabled.container)
            assertEquals(scheme.onSurfaceVariant, disabled.content)
        }
    }
}
