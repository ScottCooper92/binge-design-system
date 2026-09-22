package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

private const val SCRIM_TOP_ALPHA = 0.92f
private const val SCRIM_MID_ALPHA = 0.60f
private const val SCRIM_MID_STOP = 0.70f
private const val SCRIM_LATE_ALPHA = 0.50f
private const val SCRIM_LATE_STOP = 0.90f

// Picked so the scrim's own alpha at the title's position is already dark enough for the scrimmed
// colour above this fraction, and still light enough below it for onSurface — see #93.
private const val TITLE_SCRIM_SWITCH_FRACTION = 0.75f

/**
 * The ramp that keeps a transparent top bar legible over whatever scrolls beneath it: opaque at the
 * status bar, thinning as it descends, and only fading fully away *past* [SCRIM_LATE_STOP] — deep
 * enough into the bar that the title and back arrow, which sit around the bar's own lower half, are
 * already behind it rather than in the thinning tail. Fading all the way to nothing at the very edge
 * reads better than a hard floor; doing it before the title clears it is the bug this stop exists to
 * avoid repeating (#87/#88).
 *
 * The stops above are all positioned within the bar's own height — [SCRIM_LATE_STOP] to `1f` is the
 * final taper down to fully transparent, and by default it *ends* exactly at [fraction]'s own bottom
 * edge, which squeezes that taper into a sliver of the caller's own height and can read as a hard
 * cutoff the instant scrolled content passes beneath it (#94). [tailHeight] extends the paint area
 * that same distance past the bottom, with the same four stops rescaled onto the taller area, so
 * [SCRIM_MID_STOP] and [SCRIM_LATE_STOP] land at the same *absolute* position within the caller's own
 * bounds either way. The final taper's *completion point* is not preserved, though: with a non-zero
 * [tailHeight] it now finishes past the bottom edge instead of at it, so the alpha across roughly the
 * last [SCRIM_LATE_STOP]-to-`1f` band of the caller's own height is measurably higher — the fade
 * continues past the boundary rather than rushing to zero before it, which is the fix #94 is for, at
 * the cost of that band no longer matching a zero-[tailHeight] render pixel for pixel. Defaults to
 * [R.dimen.zero]: [OverlaidHeaderContent] already runs its own separate fade band past the header, so
 * only [BingeTopBar]/[BingeMediumTopBar] — which have no such band of their own — opt in.
 *
 * [fraction] scales the whole ramp, so the scrim arrives with the state that puts content behind the
 * bar — a collapse, or a hero clearing the top — instead of resting there always-on. At 0 nothing is
 * drawn: a resting scrim over a bar with only the background behind it paints a band darker than the
 * surface alongside, which is the seam this exists to close.
 *
 * [scrimColor] defaults to black in both themes, so a foreground over it has to travel with the same
 * [fraction] — see [scrimmedTitleColor] — or light theme's dark-on-light text goes illegible exactly
 * as the scrim lands. A caller scrimming a *known* backdrop rather than arbitrary scrolled content —
 * [DetailOverlayTopBar] over its hero's own imagery — can pass a theme-following colour instead
 * (`MaterialTheme.colorScheme.background`) and travel its foreground with `onBackground` to match;
 * the default stays black-always for a bar scrimming unpredictable content, where only a
 * guaranteed-dark tone keeps contrast regardless of what scrolls under it.
 *
 * Call it as the first child of the [Box] the bar sits in, so it fills the bar's own bounds. A
 * non-zero [tailHeight] paints past those bounds too, so it needs to sit over content the bar has no
 * other claim on (a screen transparent-bars scroll over, never a [Box] sized to the bar alone).
 */
@Composable
fun BoxScope.TopBarScrim(
    fraction: Float,
    scrimColor: Color = BingeTheme.colors.scrim,
    tailHeight: Dp = dimensionResource(R.dimen.zero),
) {
    if (fraction <= 0f) return
    val scrim = scrimColor
    val tail = tailHeight
    Box(
        modifier =
            Modifier
                .matchParentSize()
                .drawBehind {
                    val paintHeight = size.height + tail.toPx()
                    // The caller's own height as a fraction of the taller paint area, so SCRIM_MID_STOP/
                    // SCRIM_LATE_STOP land at the same absolute position within it as a zero tailHeight
                    // would (paintHeight == size.height, barFraction == 1f, matching the old behaviour).
                    val barFraction = size.height / paintHeight
                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                0f to scrim.copy(alpha = SCRIM_TOP_ALPHA * fraction),
                                SCRIM_MID_STOP * barFraction to scrim.copy(alpha = SCRIM_MID_ALPHA * fraction),
                                SCRIM_LATE_STOP * barFraction to scrim.copy(alpha = SCRIM_LATE_ALPHA * fraction),
                                1f to Color.Transparent,
                                endY = paintHeight,
                            ),
                        size = Size(size.width, paintHeight),
                    )
                },
    )
}

/**
 * A transparent bar's title colour, switching with the scrim under it: `onSurface` below
 * [TITLE_SCRIM_SWITCH_FRACTION], [scrimmedColor] at or above it. An opaque bar keeps the theme's own
 * title colour.
 *
 * A continuous blend used to drive this instead, but both colours travel with the same [scrimFraction]
 * as the scrim's own opacity, so the title and the tone behind it crossed a near-invisible gray at the
 * same point mid-ramp — as low as ~1.2:1 contrast, the bug #93 is named for. A step keeps the title in
 * one of two already-legible states and never blends toward that third one; the cost is an instant
 * colour swap rather than a fade, which reads far better than illegible text.
 *
 * [scrimmedColor] defaults to [BingeTheme.colors.onScrim], matching [TopBarScrim]'s own black-always
 * default. A caller passing a theme-following [TopBarScrim.scrimColor] passes the matching
 * `onBackground` here too, or the title still switches as if the scrim were black.
 */
@Composable
internal fun scrimmedTitleColor(
    transparent: Boolean,
    scrimFraction: Float,
    scrimmedColor: Color = BingeTheme.colors.onScrim,
): Color =
    if (!transparent) {
        Color.Unspecified
    } else if (scrimFraction.coerceIn(0f, 1f) >= TITLE_SCRIM_SWITCH_FRACTION) {
        scrimmedColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }
