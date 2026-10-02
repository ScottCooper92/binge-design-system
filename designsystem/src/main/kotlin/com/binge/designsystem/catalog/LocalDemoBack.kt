package com.binge.designsystem.catalog

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * What a demo's back button does. A demo shown full screen (see [OnePerScreen]) has no host chrome
 * around it, so its own top bar's back button is the way out; the catalog app provides it. Anywhere
 * else it does nothing.
 */
val LocalDemoBack = staticCompositionLocalOf<() -> Unit> { {} }
