@file:SelfDescribing
@file:CatalogGroup("Buttons")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Every state of [BingeTextButton] in one list, each button labelled with the state it is in, so the
 * sample says what it shows without a caption: enabled, disabled, loading, destructive, with a leading icon,
 * with a trailing one (the glyph for a destination outside the app), and the icon-only collapse that
 * `showLabel = false` switches to.
 */
@Composable
fun TextButtonSample() {
    ScreenshotTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeTextButton(label = "Enabled", onClick = {})
            BingeTextButton(label = "Disabled", onClick = {}, enabled = false)
            BingeTextButton(label = "Loading", onClick = {}, loading = true)
            BingeTextButton(label = "Destructive", onClick = {}, destructive = true)
            BingeTextButton(label = "Leading icon", onClick = {}, leadingIcon = Icons.Filled.Block)
            BingeTextButton(label = "Trailing icon", onClick = {}, trailingIcon = Icons.AutoMirrored.Filled.OpenInNew)
            BingeTextButton(label = "Icon only", onClick = {}, leadingIcon = Icons.Filled.Block, showLabel = false)
        }
    }
}
