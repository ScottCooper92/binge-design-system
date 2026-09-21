package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.binge.designsystem.theme.BingeTheme

private const val SCRIM_TOP_ALPHA = 0.92f
private const val SCRIM_MID_ALPHA = 0.60f
private const val SCRIM_MID_STOP = 0.70f
private const val SCRIM_LATE_ALPHA = 0.50f
private const val SCRIM_LATE_STOP = 0.90f

/**
 * The ramp that keeps a transparent top bar legible over whatever scrolls beneath it: opaque at the
 * status bar, thinning as it descends, and only fading fully away *past* [SCRIM_LATE_STOP] — deep
 * enough into the bar that the title and back arrow, which sit around the bar's own lower half, are
 * already behind it rather than in the thinning tail. Fading all the way to nothing at the very edge
 * reads better than a hard floor; doing it before the title clears it is the bug this stop exists to
 * avoid repeating (#87/#88).
 *
 * [fraction] scales the whole ramp, so the scrim arrives with the state that puts content behind the
 * bar — a collapse, or a hero clearing the top — instead of resting there always-on. At 0 nothing is
 * drawn: a resting scrim over a bar with only the background behind it paints a band darker than the
 * surface alongside, which is the seam this exists to close.
 *
 * [scrimColor] defaults to black in both themes, so a foreground over it has to travel with the same
 * [fraction] — `lerp(onSurface, BingeTheme.colors.onScrim, fraction)` — or light theme's dark-on-light
 * text goes illegible exactly as the scrim lands. A caller scrimming a *known* backdrop rather than
 * arbitrary scrolled content — [DetailOverlayTopBar] over its hero's own imagery — can pass a
 * theme-following colour instead (`MaterialTheme.colorScheme.background`) and travel its foreground
 * with `onBackground` to match; the default stays black-always for a bar scrimming unpredictable
 * content, where only a guaranteed-dark tone keeps contrast regardless of what scrolls under it.
 *
 * Call it as the first child of the [Box] the bar sits in, so it fills the bar's own bounds.
 */
@Composable
fun BoxScope.TopBarScrim(fraction: Float, scrimColor: Color = BingeTheme.colors.scrim) {
    if (fraction <= 0f) return
    val scrim = scrimColor
    Box(
        modifier =
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to scrim.copy(alpha = SCRIM_TOP_ALPHA * fraction),
                        SCRIM_MID_STOP to scrim.copy(alpha = SCRIM_MID_ALPHA * fraction),
                        SCRIM_LATE_STOP to scrim.copy(alpha = SCRIM_LATE_ALPHA * fraction),
                        1f to Color.Transparent,
                    ),
                ),
    )
}

/**
 * A transparent bar's title colour, travelling with the scrim under it: `onSurface` where there is
 * no scrim, [scrimmedColor] where it is full. An opaque bar keeps the theme's own title colour.
 *
 * The hard switch this replaces set `onScrim` unconditionally, which reads as white-on-white at rest
 * in light theme — the illegibility this file's KDoc warns about, shipped on every transparent bar.
 *
 * [scrimmedColor] defaults to [BingeTheme.colors.onScrim], matching [TopBarScrim]'s own black-always
 * default. A caller passing a theme-following [TopBarScrim.scrimColor] passes the matching
 * `onBackground` here too, or the title still travels toward white as if the scrim were black.
 */
@Composable
internal fun scrimmedTitleColor(
    transparent: Boolean,
    scrimFraction: Float,
    scrimmedColor: Color = BingeTheme.colors.onScrim,
): Color =
    if (transparent) {
        lerp(MaterialTheme.colorScheme.onSurface, scrimmedColor, scrimFraction.coerceIn(0f, 1f))
    } else {
        Color.Unspecified
    }
