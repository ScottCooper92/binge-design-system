package com.binge.designsystem.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.MaterialTheme

/**
 * How the TV components draw focus, switches and sheet panels. It picks the colours for those roles from the colour
 * scheme, so it is separate from the accent: `primary` stays the brand accent whichever style is set. See
 * `docs/tv-foundation.md` > TV styles.
 *
 * Set it once, on the outermost [BingeTvTheme]. A nested [BingeTvTheme] with no `style` keeps the style around it, so a
 * screen that wraps itself in the theme again still draws in the app's style.
 */
enum class TvStyle {
    /**
     * Focus in the accent: a focused control fills with `primary` and its content takes `onPrimary`, the focus ring
     * is `primary`, a checked switch is `primary`, and a sheet's panel is `surface`. The default.
     */
    Accent,

    /**
     * Focus in white, as Google TV draws it: a focused control fills with `onSurface` and its content takes `surface`,
     * the focus ring is `onSurface`, a switch is black and white, and a sheet's panel is `surfaceVariant`. The accent
     * stays where it marks something other than focus, such as a rating, a selection tick or a count badge.
     */
    Monochrome,
}

/**
 * A switch's track and thumb in each state [TvSwitch][com.binge.designsystem.tv.component.TvSwitch] draws when
 * enabled. Its outline takes the track's colour when checked and the thumb's when not. A disabled switch is drawn the
 * same way in every style, so it has no roles here, and a destructive one takes the destructive focus roles instead.
 */
@Immutable
data class TvSwitchColors(
    val checkedTrack: Color,
    val checkedThumb: Color,
    val uncheckedTrack: Color,
    val uncheckedThumb: Color,
    val focusedCheckedTrack: Color,
    val focusedCheckedThumb: Color,
    val focusedUncheckedTrack: Color,
    val focusedUncheckedThumb: Color,
)

/**
 * The colours a [TvStyle] gives each role, resolved against a colour scheme. Read them with [MaterialTheme.tvStyle].
 *
 * - [focusContainer] fills a focused row or button, and [onFocusContainer] is its label, icon and tick.
 * - [focusRing] outlines a focused element whose content a fill would cover, such as a poster. A text field's caret
 *   takes it too, since the caret shows while the field holds focus.
 * - [destructiveFocusContainer] fills a focused destructive control, and [onDestructiveFocusContainer] is its content.
 * - [panel] is the background of a side sheet and a confirm dialog.
 * - [switch] is a switch's track and thumb.
 */
@Immutable
data class TvStyleColors(
    val focusContainer: Color,
    val onFocusContainer: Color,
    val focusRing: Color,
    val destructiveFocusContainer: Color,
    val onDestructiveFocusContainer: Color,
    val panel: Color,
    val switch: TvSwitchColors,
)

/** The colours this style gives each role in [scheme]. */
fun TvStyle.colors(scheme: ColorScheme): TvStyleColors {
    val (focus, onFocus, panel) =
        when (this) {
            TvStyle.Accent -> Triple(scheme.primary, scheme.onPrimary, scheme.surface)
            TvStyle.Monochrome -> Triple(scheme.onSurface, scheme.surface, scheme.surfaceVariant)
        }
    return TvStyleColors(
        focusContainer = focus,
        onFocusContainer = onFocus,
        focusRing = focus,
        destructiveFocusContainer = scheme.error,
        onDestructiveFocusContainer = scheme.onError,
        panel = panel,
        // A checked switch is the focus colour at rest, and inverts against the focus fill when its row is focused.
        switch =
            TvSwitchColors(
                checkedTrack = focus,
                checkedThumb = onFocus,
                uncheckedTrack = Color.Transparent,
                uncheckedThumb = scheme.onSurfaceVariant,
                focusedCheckedTrack = onFocus,
                focusedCheckedThumb = focus,
                focusedUncheckedTrack = Color.Transparent,
                focusedUncheckedThumb = onFocus,
            ),
    )
}

/**
 * The style the TV components draw in. [BingeTvTheme] provides it; providing it directly also works, for a frame that
 * needs one style without a theme of its own.
 */
val LocalTvStyle = staticCompositionLocalOf { TvStyle.Accent }

/** The current [TvStyle]'s colours, resolved against the current colour scheme. */
val MaterialTheme.tvStyle: TvStyleColors
    @Composable
    @ReadOnlyComposable
    get() = LocalTvStyle.current.colors(colorScheme)
