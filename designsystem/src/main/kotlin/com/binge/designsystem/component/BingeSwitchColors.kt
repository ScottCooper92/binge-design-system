package com.binge.designsystem.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable

/**
 * Colours for every Binge `Switch`. A checked switch wears the fixed amber, the same in both
 * themes, so it matches [BingeFilledButton] rather than the darker light-theme `primary`.
 */
@Composable
fun bingeSwitchColors(): SwitchColors {
    val scheme = MaterialTheme.colorScheme
    return SwitchDefaults.colors(
        checkedTrackColor = scheme.primaryFixedDim,
        checkedThumbColor = scheme.onPrimaryFixed,
        checkedIconColor = scheme.primaryFixedDim,
        checkedBorderColor = scheme.primaryFixedDim,
    )
}
