package com.binge.designsystem.component

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The tint a top bar's action [ExpressiveIconButton]s should use on a transparent bar, so the
 * lerp from [com.binge.designsystem.theme.BingeTheme.colors.onScrim] to the bar's own
 * `scrimForegroundColor` is owned by the bar rather than re-derived at each call site.
 * [BingeTopBar] / [BingeMediumTopBar] provide it around their `actions` slot, already resolved for
 * the bar's current `foregroundScrimFraction` — the same value their own back button's tint uses.
 *
 * Read it instead of hardcoding a tint: a constant `onScrim` (or [androidx.compose.material3.LocalContentColor])
 * only happens to be correct while the bar's `scrimForegroundColor` is left at its own black-always
 * override, and silently stops tracking the ramp the moment a caller switches to the theme-following
 * default or a scrim of its own.
 *
 * Defaults to [Color.Unspecified] so an action rendered outside a Binge bar is unaffected.
 */
val LocalTopBarActionTint = compositionLocalOf { Color.Unspecified }
