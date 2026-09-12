package com.binge.designsystem.component

import androidx.compose.runtime.compositionLocalOf

/**
 * The [IconButtonTone] a top bar's action [ExpressiveIconButton]s should use, so tone is owned by the
 * bar rather than hardcoded at each call site. [BingeTopBar] / [BingeMediumTopBar] provide it around
 * their `actions` slot: [IconButtonTone.Default] on an opaque bar, [IconButtonTone.Glass] on a
 * transparent/hero bar where the scrim container is load-bearing over arbitrary imagery.
 *
 * Defaults to [IconButtonTone.Default] so an action rendered outside a Binge bar is unaffected.
 */
val LocalTopBarActionTone = compositionLocalOf { IconButtonTone.Default }
