package com.binge.designsystem.tv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.DarkBingeColors
import com.binge.designsystem.theme.LocalBingeColors
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.theme.systemReduceMotion

/**
 * The TV theme root. Provide it once, at the TV navigation host — never nested inside
 * `BingeExpressiveTheme`, and never the reverse.
 *
 * Its colour, shape and type scales are projected from the same `BingeColors`/`BingeShapes`/
 * `BingeFontFamily` tokens the phone theme uses, so the two surfaces cannot drift apart even
 * though tv-material and Material 3 ship separate `MaterialTheme` trees.
 *
 * Dark-only, deliberately: a 10-foot UI is viewed in a dim room at distance, where the light
 * surface ramp is glaring. There is no `darkTheme` parameter to pass by mistake.
 *
 * [LocalBingeColors] and [LocalReduceMotion] are re-provided here because they are plain token
 * CompositionLocals rather than Material 3 types — they carry across the seam unchanged.
 *
 * [reduceMotion] defaults to the system setting rather than `false`: that old default was the whole of a motion-sickness bug,
 * where no call site passed anything so `LocalReduceMotion` was permanently `false` on a TV and every gate
 * against it was dead code that read as working. Reading the setting here keeps a call site from having to
 * remember; a screenshot theme still overrides it to pin the reduced branch.
 */
@Composable
fun BingeTvTheme(reduceMotion: Boolean = systemReduceMotion(), content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalBingeColors provides DarkBingeColors,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = BingeTvColorScheme,
            shapes = BingeTvShapes,
            typography = BingeTvTypography,
            content = content,
        )
    }
}
