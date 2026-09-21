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
private const val SCRIM_MID_ALPHA = 0.72f
private const val SCRIM_MID_STOP = 0.55f
private const val SCRIM_BOTTOM_ALPHA = 0.45f

/**
 * The ramp that keeps a transparent top bar legible over whatever scrolls beneath it: opaque at the
 * status bar, thinning as it descends but never to nothing — the bar's lower edge, where the title
 * and back arrow sit, keeps enough tone that scrolled content passing under it doesn't collide with
 * them.
 *
 * [fraction] scales the whole ramp, so the scrim arrives with the state that puts content behind the
 * bar — a collapse, or a hero clearing the top — instead of resting there always-on. At 0 nothing is
 * drawn: a resting scrim over a bar with only the background behind it paints a band darker than the
 * surface alongside, which is the seam this exists to close.
 *
 * The tone is black in both themes, so a foreground over it has to travel with the same [fraction] —
 * `lerp(onSurface, BingeTheme.colors.onScrim, fraction)` — or light theme's dark-on-light text goes
 * illegible exactly as the scrim lands.
 *
 * Call it as the first child of the [Box] the bar sits in, so it fills the bar's own bounds.
 */
@Composable
fun BoxScope.TopBarScrim(fraction: Float) {
    if (fraction <= 0f) return
    val scrim = BingeTheme.colors.scrim
    Box(
        modifier =
            Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to scrim.copy(alpha = SCRIM_TOP_ALPHA * fraction),
                        SCRIM_MID_STOP to scrim.copy(alpha = SCRIM_MID_ALPHA * fraction),
                        1f to scrim.copy(alpha = SCRIM_BOTTOM_ALPHA * fraction),
                    ),
                ),
    )
}

/**
 * A transparent bar's title colour, travelling with the scrim under it: `onSurface` where there is
 * no scrim, `onScrim` where it is full. An opaque bar keeps the theme's own title colour.
 *
 * The hard switch this replaces set `onScrim` unconditionally, which reads as white-on-white at rest
 * in light theme — the illegibility this file's KDoc warns about, shipped on every transparent bar.
 */
@Composable
internal fun scrimmedTitleColor(transparent: Boolean, scrimFraction: Float): Color =
    if (transparent) {
        lerp(MaterialTheme.colorScheme.onSurface, BingeTheme.colors.onScrim, scrimFraction.coerceIn(0f, 1f))
    } else {
        Color.Unspecified
    }
