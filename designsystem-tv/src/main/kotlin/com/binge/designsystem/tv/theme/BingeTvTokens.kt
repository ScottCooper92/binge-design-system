package com.binge.designsystem.tv.theme

import androidx.tv.material3.ColorScheme
import androidx.tv.material3.Shapes
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme
import com.binge.designsystem.theme.BingeFontFamily
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.DarkColorScheme
import androidx.compose.material3.ColorScheme as M3ColorScheme

/**
 * The TV colour scheme, projected slot-for-slot from the phone theme's [DarkColorScheme] rather than
 * re-declaring hex values, so a palette change lands on both surfaces at once.
 *
 * Two slots are not a literal copy because tv-material has no `surfaceContainer*` ramp: without it, `Surface`
 * falls back to M3 `surface` (#161618), only 8 values from `background` (#0E0E0F), so on a TV with no elevation
 * overlay unfocused cards disappear. The container tones are promoted a step — TV `surface` ← M3
 * `surfaceContainerHigh`, TV `surfaceVariant` ← M3 `surfaceContainer` — keeping the phone ramp's *intent* (a
 * card reads as lifted) across a library with fewer slots. tv-material also renames `outline`/`outlineVariant`
 * to `border`/`borderVariant`.
 *
 * Public, like [DarkColorScheme], so a consumer's contrast tests can measure its own components on these grounds.
 */
val BingeTvColorScheme: ColorScheme = DarkColorScheme.toTvColorScheme()

/**
 * The projection [BingeTvColorScheme] is built with, public so an app with its own brand can build
 * its TV scheme the same way rather than re-deciding which slots move.
 */
fun M3ColorScheme.toTvColorScheme(): ColorScheme =
    darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surfaceContainerHigh,
        onSurface = onSurface,
        surfaceVariant = surfaceContainer,
        onSurfaceVariant = onSurfaceVariant,
        surfaceTint = surfaceTint,
        inverseSurface = inverseSurface,
        inverseOnSurface = inverseOnSurface,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
        border = outline,
        borderVariant = outlineVariant,
        scrim = scrim,
    )

/** The same radii as `BingeShapes.Material3`, in tv-material's own [Shapes] container. */
internal val BingeTvShapes = Shapes(
    extraSmall = BingeShapes.ElementExtraSmall,
    small = BingeShapes.ElementSmall,
    medium = BingeShapes.Large,
    large = BingeShapes.MediaCard,
    extraLarge = BingeShapes.AccountCard,
)

/**
 * tv-material's default type scale — already sized for 10-foot legibility — restyled onto Binge's
 * typeface. The phone scale is not reused: its sizes are tuned for arm's length, and the two
 * libraries' `Typography` types are unrelated, so only the family crosses over.
 */
internal val BingeTvTypography: Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = BingeFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = BingeFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = BingeFontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = BingeFontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = BingeFontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = BingeFontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = BingeFontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = BingeFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = BingeFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = BingeFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = BingeFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = BingeFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = BingeFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = BingeFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = BingeFontFamily),
    )
}
