@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.LabelledIconButton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for [LabelledIconButton]: a round tonal icon button with a label under it, as a detail page's action row shows them.
 * Each sample is a row of three buttons, the last with a label too long for one line, which grows the button to its cap and then wraps to
 * two lines, centred. A short label keeps the narrow floor width; a large font widens the button rather than cutting "Trailer" to "Trail…".
 * The words are the sample's own, since the caller owns them in the app.
 */
@Composable
fun LabelledIconButtonRowSample() {
    LabelledIconButtonRow(trailer = "Trailer", share = "Share", longLabel = "Add to a list with friends")
}

/** The same row in Spanish, whose words run longer than English: the buttons widen to read them in full, then wrap. */
@Composable
fun LabelledIconButtonSpanishRowSample() {
    LabelledIconButtonRow(trailer = "Tráiler", share = "Compartir", longLabel = "Añadir a una lista con amigos")
}

/**
 * A row whose last label is one long word. The button's cap grows with the font, so the word reads whole at the normal size, at 1.3x and at
 * 2x, on one line and never broken across two lines in the middle. A word wider than even the scaled cap, or than the row leaves it, stays
 * whole and ends in an ellipsis.
 */
@Composable
fun LabelledIconButtonLongWordSample() {
    LabelledIconButtonRow(trailer = "Trailer", share = "Share", longLabel = "Subscribe")
}

/** The same in Spanish, whose long word "Suscribirse" also reads whole at 2x as the cap scales with the font. */
@Composable
fun LabelledIconButtonSpanishLongWordSample() {
    LabelledIconButtonRow(trailer = "Tráiler", share = "Compartir", longLabel = "Suscribirse")
}

/**
 * A row whose first button leaves the app: a small open-in-new mark at the end of its label, announced with the button ("Trailer, Opens
 * outside the app"). The other two draw as they always have, so the row shows the marked button beside plain ones.
 */
@Composable
fun LabelledIconButtonTrailingMarkSample() {
    ScreenshotTheme {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                LabelledIconButton(
                    icon = Icons.Filled.PlayArrow,
                    label = "Trailer",
                    onClick = {},
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    trailingContentDescription = "Opens outside the app",
                )
                LabelledIconButton(icon = Icons.Filled.Share, label = "Share", onClick = {})
                LabelledIconButton(icon = Icons.Filled.Add, label = "Add to a list with friends", onClick = {})
            }
        }
    }
}

/** The same in Spanish, whose longer word still reads whole beside the mark. */
@Composable
fun LabelledIconButtonSpanishTrailingMarkSample() {
    ScreenshotTheme {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                LabelledIconButton(
                    icon = Icons.Filled.PlayArrow,
                    label = "Tráiler",
                    onClick = {},
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    trailingContentDescription = "Se abre fuera de la app",
                )
                LabelledIconButton(icon = Icons.Filled.Share, label = "Compartir", onClick = {})
                LabelledIconButton(icon = Icons.Filled.Add, label = "Añadir a una lista con amigos", onClick = {})
            }
        }
    }
}

/** The row with every button disabled: the circles and labels dim, and nothing takes a tap. */
@Composable
fun LabelledIconButtonDisabledSample() {
    LabelledIconButtonRow(trailer = "Trailer", share = "Share", longLabel = "Add to a list with friends", enabled = false)
}

@Composable
private fun LabelledIconButtonRow(
    trailer: String,
    share: String,
    longLabel: String,
    enabled: Boolean = true,
) {
    ScreenshotTheme {
        // A Column so the surface's tight constraints do not stretch the row to the frame's height.
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                LabelledIconButton(icon = Icons.Filled.PlayArrow, label = trailer, onClick = {}, enabled = enabled)
                LabelledIconButton(icon = Icons.Filled.Share, label = share, onClick = {}, enabled = enabled)
                LabelledIconButton(icon = Icons.Filled.Add, label = longLabel, onClick = {}, enabled = enabled)
            }
        }
    }
}
