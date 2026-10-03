package com.binge.designsystem.catalogapp.overrides

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

/** Font scales the detail view offers: the default, a common accessibility setting, and the maximum. */
val FontScalePresets = listOf(1.0f, 1.3f, 2.0f)

/**
 * The locales the detail view offers: the system's own, English, Spanish (the one the design system
 * translates into) and the two pseudolocales debug builds carry, accented and mirrored.
 */
enum class SampleLocale(
    val tag: String?,
) {
    System(null),
    English("en"),
    Spanish("es"),
    PseudoAccented("en-XA"),
    PseudoMirrored("ar-XB"),
}

/** What the host forces on a sample without editing it. */
data class SampleOverrides(
    val dark: Boolean,
    val fontScale: Float,
    val rtl: Boolean,
    val locale: SampleLocale = SampleLocale.System,
)

/**
 * Overrides what a sample's theme and strings read. Dark mode, font scale and RTL are composition
 * locals (`LocalConfiguration`, `LocalDensity`, `LocalLayoutDirection`): samples wrap themselves in
 * `ScreenshotTheme`, which follows `isSystemInDarkTheme()`. Resource qualifiers are not reachable that
 * way, because resources come from the `Context`, so a locale is applied by handing the sample a
 * context localized for it ([LocalContext] and [LocalResources]), which `stringResource` reads.
 */
@Composable
fun WithOverrides(overrides: SampleOverrides, content: @Composable () -> Unit) {
    val base = LocalConfiguration.current
    val density = LocalDensity.current
    val context = LocalContext.current
    val locale = overrides.locale.tag?.let(Locale::forLanguageTag)
    val localized = remember(context, locale) { locale?.let { context.localizedTo(it) } ?: context }
    val night = if (overrides.dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
    val configuration =
        Configuration(base).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
            fontScale = overrides.fontScale
            locale?.let {
                setLocale(it)
                setLayoutDirection(it)
            }
        }
    val mirrored = overrides.rtl || configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
    CompositionLocalProvider(
        LocalContext provides localized,
        LocalResources provides localized.resources,
        LocalConfiguration provides configuration,
        LocalDensity provides Density(density.density, overrides.fontScale),
        LocalLayoutDirection provides if (mirrored) LayoutDirection.Rtl else LayoutDirection.Ltr,
        content = content,
    )
}

/**
 * This context with its resources in [locale]. A wrapper over this context rather than the bare one
 * `createConfigurationContext` returns, so the activity stays reachable by unwrapping: a sample that
 * hands [LocalContext] to `WindowInfoTracker`, as `FoldPosture` does, needs a UI context, and a bare
 * configuration context is not one (#215).
 */
internal fun Context.localizedTo(locale: Locale): Context {
    val resources =
        createConfigurationContext(
            Configuration(resources.configuration).apply {
                setLocale(locale)
                setLayoutDirection(locale)
            },
        ).resources
    return LocalizedContext(this, resources)
}

private class LocalizedContext(
    base: Context,
    private val localized: Resources,
) : ContextWrapper(base) {
    override fun getResources(): Resources = localized
}
