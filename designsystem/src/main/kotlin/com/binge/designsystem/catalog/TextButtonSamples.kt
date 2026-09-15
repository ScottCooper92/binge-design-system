package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
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
