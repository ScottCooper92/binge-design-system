@file:SelfDescribing

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
 * sample says what it shows without a caption: enabled, disabled, destructive, with a leading icon,
 * and the icon-only collapse that `showLabel = false` switches to.
 */
@Composable
fun TextButtonSample() {
    ScreenshotTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeTextButton(label = "Enabled", onClick = {})
            BingeTextButton(label = "Disabled", onClick = {}, enabled = false)
            BingeTextButton(label = "Destructive", onClick = {}, destructive = true)
            BingeTextButton(label = "Leading icon", onClick = {}, leadingIcon = Icons.Filled.Block)
            BingeTextButton(label = "Icon only", onClick = {}, leadingIcon = Icons.Filled.Block, showLabel = false)
        }
    }
}

/** The text button's [trailingIcon][BingeTextButton] — the glyph after the label, for a destination outside the app. */
@Composable
fun TextButtonTrailingIconSample() {
    ScreenshotTheme {
        BingeTextButton(
            label = "Open server",
            onClick = {},
            trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
        )
    }
}
