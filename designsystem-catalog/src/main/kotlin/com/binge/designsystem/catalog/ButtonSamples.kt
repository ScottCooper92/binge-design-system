@file:CatalogGroup("Buttons")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Every state of [BingeFilledButton] in one list, each button labelled with the state it is in, so
 * the sample says what it shows without a caption: enabled, with a leading icon, disabled,
 * destructive (the tone only reads against the enabled one above it) and loading, where a spinner
 * replaces the label and taps are swallowed.
 */
@Composable
fun FilledButtonSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeFilledButton(label = "Enabled", onClick = {})
            BingeFilledButton(label = "Leading icon", onClick = {}, leadingIcon = Icons.Filled.PlayArrow)
            BingeFilledButton(label = "Disabled", onClick = {}, enabled = false)
            BingeFilledButton(label = "Destructive", onClick = {}, destructive = true)
            BingeFilledButton(label = "Loading", onClick = {}, loading = true)
        }
    }
}

/**
 * Every state of [BingeOutlinedButton] in one list, each labelled with its state: enabled, with a
 * leading icon, disabled, destructive, loading, and the icon-only collapse `showLabel = false` gives.
 */
@Composable
fun OutlinedButtonSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeOutlinedButton(label = "Enabled", onClick = {})
            BingeOutlinedButton(label = "Leading icon", onClick = {}, leadingIcon = Icons.Filled.Flag)
            BingeOutlinedButton(label = "Disabled", onClick = {}, enabled = false)
            BingeOutlinedButton(label = "Destructive", onClick = {}, destructive = true)
            BingeOutlinedButton(label = "Loading", onClick = {}, loading = true)
            BingeOutlinedButton(label = "Icon only", onClick = {}, leadingIcon = Icons.Filled.Flag, showLabel = false)
        }
    }
}
