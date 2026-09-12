package com.binge.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.binge.designsystem.R

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val outfitFont = GoogleFont("Outfit")

/**
 * Binge's typeface. Public because the TV design system builds its own 10-foot type scale from
 * tv-material's `Typography` defaults and needs the same family — the two Material libraries have
 * separate `Typography` types, so only the font can be shared, not the scale.
 */
val BingeFontFamily = FontFamily(
    Font(googleFont = outfitFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = outfitFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = outfitFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = outfitFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    // ExtraBold backs the big-number emphasis (rating scores, distribution headline); without it
    // those `fontWeight = ExtraBold` styles fall back to Bold rather than the intended weight.
    Font(googleFont = outfitFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
)

private fun Typography.withFontFamily(family: FontFamily): Typography =
    copy(
        displayLarge = displayLarge.copy(fontFamily = family),
        displayMedium = displayMedium.copy(fontFamily = family),
        displaySmall = displaySmall.copy(fontFamily = family),
        headlineLarge = headlineLarge.copy(fontFamily = family),
        headlineMedium = headlineMedium.copy(fontFamily = family),
        headlineSmall = headlineSmall.copy(fontFamily = family),
        titleLarge = titleLarge.copy(fontFamily = family),
        titleMedium = titleMedium.copy(fontFamily = family),
        titleSmall = titleSmall.copy(fontFamily = family),
        bodyLarge = bodyLarge.copy(fontFamily = family),
        bodyMedium = bodyMedium.copy(fontFamily = family),
        bodySmall = bodySmall.copy(fontFamily = family),
        labelLarge = labelLarge.copy(fontFamily = family),
        labelMedium = labelMedium.copy(fontFamily = family),
        labelSmall = labelSmall.copy(fontFamily = family),
    )

private val outfitBase = Typography().withFontFamily(BingeFontFamily)

val BingeTypography = outfitBase.copy(
    displayLarge = outfitBase.displayLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.2).sp,
    ),
    displayMedium = outfitBase.displayMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.2).sp,
    ),
    displaySmall = outfitBase.displaySmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.2).sp,
    ),
    headlineLarge = outfitBase.headlineLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-1.0).sp,
    ),
    headlineMedium = outfitBase.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = outfitBase.headlineSmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    titleLarge = outfitBase.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp,
    ),
    titleMedium = outfitBase.titleMedium.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    labelLarge = outfitBase.labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
    ),
    labelSmall = outfitBase.labelSmall.copy(
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
    ),
)

/**
 * Emphasis variants of the type scale — the same slot one weight heavier — for the recurring
 * "emphasized" roles (card titles, filter-button labels, code badges) that would otherwise
 * re-apply `.copy(fontWeight = …)` at each call site. Resolve via
 * `MaterialTheme.typography.<slot>Emphasis`. (For the section-label/eyebrow role, the base
 * `labelLarge`/`titleLarge` are already SemiBold/Bold, so no emphasis token is needed.)
 */
val Typography.titleMediumEmphasis: TextStyle get() = titleMedium.copy(fontWeight = FontWeight.Bold)
val Typography.labelLargeEmphasis: TextStyle get() = labelLarge.copy(fontWeight = FontWeight.Bold)
val Typography.labelSmallEmphasis: TextStyle get() = labelSmall.copy(fontWeight = FontWeight.Bold)
