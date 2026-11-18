@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
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
 * Each sample is a row of three buttons, the last with a label too long for its column, which ellipsizes on one line rather than
 * growing the button. The words are the sample's own, since the caller owns them in the app.
 */
@Composable
fun LabelledIconButtonRowSample() {
    LabelledIconButtonRow(trailer = "Trailer", share = "Share", longLabel = "Add to a list with friends")
}

/** The same row in Spanish, whose words run longer than English: the labels ellipsize rather than push the circles. */
@Composable
fun LabelledIconButtonSpanishRowSample() {
    LabelledIconButtonRow(trailer = "Tráiler", share = "Compartir", longLabel = "Añadir a una lista con amigos")
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
