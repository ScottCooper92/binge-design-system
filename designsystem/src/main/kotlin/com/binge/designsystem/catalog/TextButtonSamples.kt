package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [BingeTextButton] — the borderless button variant.
 * See the full catalog convention KDoc on [MediaCardRatedSample]; group these under `"Buttons"`.
 */
@Composable
fun TextButtonSample() {
    ScreenshotTheme {
        BingeTextButton(label = "Cancel request", onClick = {})
    }
}

/** Disabled text button — the greyed, non-tappable variant. */
@Composable
fun TextButtonDisabledSample() {
    ScreenshotTheme {
        BingeTextButton(label = "Cancel request", onClick = {}, enabled = false)
    }
}

/** A destructive text button, which is the error tone named rather than passed as a colour. */
@Composable
fun TextButtonErrorSample() {
    ScreenshotTheme {
        BingeTextButton(
            label = "Cancel request",
            onClick = {},
            destructive = true,
        )
    }
}

/**
 * The text button's [leadingIcon][BingeTextButton] pair — icon-and-label beside the icon-only
 * collapse that [showLabel][BingeTextButton] switches to.
 */
@Composable
fun TextButtonIconSample() {
    ScreenshotTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeTextButton(
                label = "Block this title",
                onClick = {},
                leadingIcon = Icons.Filled.Block,
                contentColor = MaterialTheme.colorScheme.error,
            )
            BingeTextButton(
                label = "Block this title",
                onClick = {},
                leadingIcon = Icons.Filled.Block,
                showLabel = false,
                contentColor = MaterialTheme.colorScheme.error,
            )
        }
    }
}
