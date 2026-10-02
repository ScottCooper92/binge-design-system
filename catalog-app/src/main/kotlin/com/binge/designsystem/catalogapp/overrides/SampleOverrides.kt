package com.binge.designsystem.catalogapp.overrides

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/** Font scales the detail view offers: the default, a common accessibility setting, and the maximum. */
val FontScalePresets = listOf(1.0f, 1.3f, 2.0f)

/** What the host forces on a sample without editing it. */
data class SampleOverrides(
    val dark: Boolean,
    val fontScale: Float,
    val rtl: Boolean,
)

/**
 * Overrides the three composition locals a sample's theme reads: `LocalConfiguration` for dark mode
 * (samples wrap themselves in `ScreenshotTheme`, which follows `isSystemInDarkTheme()`),
 * `LocalDensity` for font scale and `LocalLayoutDirection` for RTL.
 *
 * Resource qualifiers are not reached: resources come from the `Context`, so a width or locale
 * override cannot be made this way.
 */
@Composable
fun WithOverrides(overrides: SampleOverrides, content: @Composable () -> Unit) {
    val base = LocalConfiguration.current
    val density = LocalDensity.current
    val night = if (overrides.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val configuration =
        Configuration(base).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
            fontScale = overrides.fontScale
        }
    CompositionLocalProvider(
        LocalConfiguration provides configuration,
        LocalDensity provides Density(density.density, overrides.fontScale),
        LocalLayoutDirection provides if (overrides.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
        content = content,
    )
}
