package com.binge.designsystem.tv.theme

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.tv.material3.MaterialTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The TV style's roles, and how the theme passes the style down. The accent roles are the colours the components read
 * before the style existed, so a consumer that sets no style draws exactly as it did; the screenshot baselines are the
 * pixel proof, and this names the slots.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvStyleTest {
    @get:Rule
    val rule = createComposeRule()

    private val scheme = BingeTvColorScheme

    @Test
    fun `the accent style draws focus in primary and panels in surface`() {
        val colors = TvStyle.Accent.colors(scheme)

        assertEquals(scheme.primary, colors.focusContainer)
        assertEquals(scheme.onPrimary, colors.onFocusContainer)
        assertEquals(scheme.primary, colors.focusRing)
        assertEquals(scheme.error, colors.destructiveFocusContainer)
        assertEquals(scheme.onError, colors.onDestructiveFocusContainer)
        assertEquals(scheme.surface, colors.panel)
        assertEquals(scheme.primary, colors.switch.checkedTrack)
        assertEquals(scheme.onPrimary, colors.switch.checkedThumb)
        assertEquals(scheme.onPrimary, colors.switch.focusedCheckedTrack)
        assertEquals(scheme.primary, colors.switch.focusedCheckedThumb)
    }

    @Test
    fun `the monochrome style draws focus in white and panels in surfaceVariant, and keeps destructive red`() {
        val colors = TvStyle.Monochrome.colors(scheme)

        assertEquals(scheme.onSurface, colors.focusContainer)
        assertEquals(scheme.surface, colors.onFocusContainer)
        assertEquals(scheme.onSurface, colors.focusRing)
        assertEquals(scheme.error, colors.destructiveFocusContainer)
        assertEquals(scheme.onError, colors.onDestructiveFocusContainer)
        assertEquals(scheme.surfaceVariant, colors.panel)
        assertEquals(scheme.onSurface, colors.switch.checkedTrack)
        assertEquals(scheme.surface, colors.switch.checkedThumb)
        assertEquals(Color.Transparent, colors.switch.uncheckedTrack)
        assertEquals(scheme.surface, colors.switch.focusedUncheckedThumb)
    }

    @Test
    fun `the default style is the accent`() {
        var style: TvStyle? = null
        rule.setContent { BingeTvTheme { style = LocalTvStyle.current } }
        rule.waitForIdle()

        assertEquals(TvStyle.Accent, style)
    }

    @Test
    fun `a nested theme with no style keeps the style around it`() {
        var inner: TvStyle? = null
        var focus: Color? = null
        rule.setContent {
            BingeTvTheme(style = TvStyle.Monochrome) {
                BingeTvTheme {
                    inner = LocalTvStyle.current
                    focus = MaterialTheme.tvStyle.focusContainer
                }
            }
        }
        rule.waitForIdle()

        assertEquals(TvStyle.Monochrome, inner)
        assertEquals(scheme.onSurface, focus)
    }

    @Test
    fun `a nested theme that names a style changes it`() {
        var inner: TvStyle? = null
        rule.setContent {
            BingeTvTheme(style = TvStyle.Monochrome) {
                BingeTvTheme(style = TvStyle.Accent) { inner = LocalTvStyle.current }
            }
        }
        rule.waitForIdle()

        assertEquals(TvStyle.Accent, inner)
    }
}
