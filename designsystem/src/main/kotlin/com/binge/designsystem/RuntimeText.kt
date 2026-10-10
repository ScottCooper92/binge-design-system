package com.binge.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

private const val MINUTES_PER_HOUR = 60

/**
 * A runtime as hours and minutes in the shown language ("2h 15m", "2 h 15 min"). The one place the split into
 * hours and minutes happens, so every screen and companion app that shows a runtime reads it the same way.
 * A runtime under an hour reads "0h 45m", as the hero always has.
 */
@Composable
fun formatRuntime(minutes: Int): String = stringResource(R.string.hero_meta_runtime, minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
