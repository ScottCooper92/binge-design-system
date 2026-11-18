package com.binge.designsystem.theme

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalReduceMotion = staticCompositionLocalOf { false }

/**
 * Whether an assistive technology, such as a screen reader or a switch-access service, is on. A component that moves content on its own, such
 * as an autoplaying carousel, holds still while this is true, so a page stays put under a user who is still reading it. Read it next to
 * [LocalReduceMotion]; [BingeExpressiveTheme] provides both. False where nothing provides it.
 */
val LocalAssistiveTechnology = staticCompositionLocalOf { false }

/**
 * An app's own pair of Material colour schemes, light and dark.
 *
 * The design system is shared and the brand is not: two apps built from these components should
 * still be tellable apart at a glance. Everything else a theme carries — the typography, the shapes,
 * [BingeColors] — stays shared, because those tokens mean the same thing in both apps.
 *
 * [BingeColors] is deliberately not part of this. Its tokens carry meaning rather than brand, and
 * two apps should agree on what amber-means-waiting means.
 *
 * A brand overrides `primaryFixed`, `primaryFixedDim`, `onPrimaryFixed` and `onPrimaryFixedVariant`
 * together with the primary family. Filled buttons, selected filter chips, switches, the month-year
 * picker header and the hero mesh wash sit on the fixed roles. A scheme that changes `primary` but keeps
 * any of Binge's fixed roles is rejected here, rather than rendering Binge's amber in another palette.
 */
@Immutable
class BingeBrand(
    val light: ColorScheme,
    val dark: ColorScheme,
) {
    init {
        requireOwnFixedRoles(light, LightColorScheme, "light")
        requireOwnFixedRoles(dark, DarkColorScheme, "dark")
    }

    companion object {
        /** Binge's own, and the default, so an app that passes nothing renders exactly as Binge does. */
        val Binge = BingeBrand(light = LightColorScheme, dark = DarkColorScheme)
    }
}

@Composable
fun BingeExpressiveTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Default to Binge's fixed brand palette; Material You wallpaper colour is an opt-in setting
    // (the app root passes the user's choice).
    dynamicColor: Boolean = false,
    reduceMotion: Boolean = systemReduceMotion(),
    brand: BingeBrand = BingeBrand.Binge,
    assistiveTechnology: Boolean = systemAssistiveTechnology(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        // Untouched by [brand]: an app opting into Material You has already said the wallpaper wins.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> brand.dark
        else -> brand.light
    }

    val bingeColors = if (darkTheme) DarkBingeColors else LightBingeColors

    CompositionLocalProvider(
        LocalReduceMotion provides reduceMotion,
        LocalAssistiveTechnology provides assistiveTechnology,
        LocalBingeColors provides bingeColors,
        LocalBingeDarkTheme provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = BingeTypography,
            shapes = BingeShapes.Material3,
            content = content,
        )
    }
}

private fun requireOwnFixedRoles(
    scheme: ColorScheme,
    binge: ColorScheme,
    name: String,
) {
    if (scheme.primary == binge.primary) return
    val kept = listOfNotNull(
        "primaryFixed".takeIf { scheme.primaryFixed == binge.primaryFixed },
        "primaryFixedDim".takeIf { scheme.primaryFixedDim == binge.primaryFixedDim },
        "onPrimaryFixed".takeIf { scheme.onPrimaryFixed == binge.onPrimaryFixed },
        "onPrimaryFixedVariant".takeIf { scheme.onPrimaryFixedVariant == binge.onPrimaryFixedVariant },
    )
    require(kept.isEmpty()) {
        "The $name scheme changes primary but keeps Binge's ${kept.joinToString()}: set them with the primary family."
    }
}

/**
 * Whether the user has asked the system for less animation.
 *
 * Public because `BingeTvTheme` needs to read it too: while it was private the TV theme couldn't, so
 * its `reduceMotion` defaulted to `false` and every gate built against `LocalReduceMotion` silently
 * did nothing on a television.
 */
@Composable
fun systemReduceMotion(): Boolean {
    val context = LocalContext.current
    var reduceMotion by remember(context) { mutableStateOf(context.readSystemReduceMotion()) }

    // The transition/animator scales are toggled from system Settings while the app stays alive
    // (unlike dark theme or locale, there is no config change to recreate this Context on), so a
    // one-shot read would only ever reflect whatever was set before the app launched.
    DisposableEffect(context) {
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reduceMotion = context.readSystemReduceMotion()
                }
            }
        val resolver = context.contentResolver
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE), false, observer)
        resolver.registerContentObserver(Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }

    return reduceMotion
}

/**
 * Whether an assistive technology is on: touch exploration (TalkBack's mode), or an enabled accessibility service that speaks, gives braille
 * or acts generically, as a switch-access service does. A service that only draws, such as a magnifier or a colour filter, does not count,
 * because it does not read the page to the user.
 *
 * Observed, because a user turns a service on and off from system Settings while the app stays alive. From API 33 a change to the set of
 * enabled services is observed too. Below that, only the first service switching on or off and touch exploration are, so a second service
 * is picked up on the next composition. Public beside [systemReduceMotion] so that a consumer that builds its own theme can provide
 * [LocalAssistiveTechnology] from it.
 */
@Composable
fun systemAssistiveTechnology(): Boolean {
    val context = LocalContext.current
    val manager = remember(context) { context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager }
    var active by remember(manager) { mutableStateOf(manager.readAssistiveTechnology()) }

    DisposableEffect(manager) {
        if (manager == null) return@DisposableEffect onDispose { }
        val onTouchExploration = AccessibilityManager.TouchExplorationStateChangeListener { active = manager.readAssistiveTechnology() }
        val onState = AccessibilityManager.AccessibilityStateChangeListener { active = manager.readAssistiveTechnology() }
        manager.addTouchExplorationStateChangeListener(onTouchExploration)
        manager.addAccessibilityStateChangeListener(onState)
        val onServices =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                AccessibilityManager.AccessibilityServicesStateChangeListener { active = manager.readAssistiveTechnology() }
            } else {
                null
            }
        if (onServices != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            manager.addAccessibilityServicesStateChangeListener(onServices)
        }
        onDispose {
            if (onServices != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                manager.removeAccessibilityServicesStateChangeListener(onServices)
            }
            manager.removeTouchExplorationStateChangeListener(onTouchExploration)
            manager.removeAccessibilityStateChangeListener(onState)
        }
    }

    return active
}

private const val ASSISTIVE_FEEDBACK =
    AccessibilityServiceInfo.FEEDBACK_SPOKEN or AccessibilityServiceInfo.FEEDBACK_BRAILLE or AccessibilityServiceInfo.FEEDBACK_GENERIC

private fun AccessibilityManager?.readAssistiveTechnology(): Boolean {
    if (this == null || !isEnabled) return false
    if (isTouchExplorationEnabled) return true
    return getEnabledAccessibilityServiceList(
        AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
    ).any { it.feedbackType and ASSISTIVE_FEEDBACK != 0 }
}

private fun Context.readSystemReduceMotion(): Boolean {
    val transition = Settings.Global.getFloat(contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)
    val animator = Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    return transition == 0f || animator == 0f
}
