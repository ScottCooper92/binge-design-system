package com.binge.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import me.tatarka.google.material.blend.Blend
import me.tatarka.google.material.hct.Hct

/** How many tones an avatar can take: enough that people sitting side by side rarely share one. */
private const val AVATAR_TONE_COUNT = 8

private const val FULL_TURN_DEGREES = 360.0

/** Chroma of the hue a tone is harmonised from: high, so the hue survives the move to the container's lower chroma. */
private const val SOURCE_CHROMA = 60.0

/** The tone the source hue is sampled at before it is harmonised. Only its hue is kept. */
private const val SOURCE_TONE = 50.0

/** Container tone in a light theme, the tone of `primaryContainer`: light enough to sit among the app's chips. */
private const val LIGHT_TONE = 90.0

/** Container tone in a dark theme, the tone of `primaryContainer`. */
private const val DARK_TONE = 30.0

/** Chroma of a tone: below the primary container's, so an avatar reads as a container, not a sticker. */
private const val TONE_CHROMA = 28.0

/** Tone of the initials on a container: 10 over the light containers, 90 over the dark ones. */
private const val LIGHT_INK_TONE = 10.0
private const val DARK_INK_TONE = 90.0

/** Chroma of the initials: a hint of the primary's hue, so the ink belongs to the tones. */
private const val INK_CHROMA = 6.0

/**
 * The tones and ink of the initials avatar for a theme whose accent is [primary].
 *
 * Eight hues are spread evenly round the wheel, so different people stay apart. Each is harmonised toward
 * [primary], which leans the whole set to the app's own accent while keeping the hues distinct. The result is
 * set at the container tone and chroma of the scheme ([isDark] picks the tone), so an avatar reads like the
 * chips and containers around it.
 *
 * Pure, so it is unit-tested without a theme. [BingeExpressiveTheme] and the TV theme call it with the scheme's
 * primary, which follows dynamic colour and a brand.
 */
fun avatarTones(primary: Color, isDark: Boolean): AvatarTones {
    val primaryArgb = primary.toArgb()
    val tone = if (isDark) DARK_TONE else LIGHT_TONE
    val tones = List(AVATAR_TONE_COUNT) { index ->
        val hue = index * FULL_TURN_DEGREES / AVATAR_TONE_COUNT
        val harmonised = Blend.harmonize(Hct.from(hue, SOURCE_CHROMA, SOURCE_TONE).toInt(), primaryArgb)
        Color(Hct.from(Hct.fromInt(harmonised).hue, TONE_CHROMA, tone).toInt())
    }
    val inkHue = Hct.fromInt(primaryArgb).hue
    val ink = Color(Hct.from(inkHue, INK_CHROMA, if (isDark) DARK_INK_TONE else LIGHT_INK_TONE).toInt())
    return AvatarTones(tones = tones, ink = ink)
}

/** The avatar's [tones] and the [ink] its initials are set in; [contrastSafeOn] still guards each pairing at draw time. */
class AvatarTones internal constructor(
    val tones: List<Color>,
    val ink: Color,
)

/**
 * These colours with the avatar's tones and ink derived from [primary], for a theme whose accent is [primary]. The
 * rest of [BingeColors] carries meaning rather than brand, so it is kept as it is.
 */
fun BingeColors.withAvatarTonesFor(primary: Color, isDark: Boolean): BingeColors {
    val avatar = avatarTones(primary, isDark)
    return copy(avatarPalette = avatar.tones, avatarInk = avatar.ink)
}
