@file:CatalogGroup("Buttons")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeStatusButton
import com.binge.designsystem.component.BingeStatusButtonEmphasis
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

@Composable
private fun StatusButtons(
    request: String,
    requested: String,
    processing: String,
    available: String,
    partly: String,
) {
    Column(
        modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        val full = Modifier.fillMaxWidth()
        BingeStatusButton(
            label = request,
            onClick = {},
            sentiment = BingeSentiment.Neutral,
            modifier = full,
            icon = Icons.Filled.Add,
            emphasis = BingeStatusButtonEmphasis.Filled,
        )
        BingeStatusButton(requested, onClick = {}, sentiment = BingeSentiment.Caution, modifier = full)
        BingeStatusButton(processing, onClick = {}, sentiment = BingeSentiment.Info, modifier = full)
        BingeStatusButton(available, onClick = {}, sentiment = BingeSentiment.Positive, modifier = full)
        BingeStatusButton(partly, onClick = {}, sentiment = BingeSentiment.Info, modifier = full)
    }
}

/**
 * The states of a request, as a title page shows them: Request, solid and neutral, for the open action, then Requested,
 * Processing, Available and Partly available, tonal with their status dots. The mapping of states onto sentiments is the
 * consumer's; this one only illustrates it.
 */
@Composable
fun BingeStatusButtonSample() {
    ScreenshotTheme { StatusButtons("Request", "Requested", "Processing", "Available", "Partly available") }
}

/** The same states in Spanish, where "Parcialmente disponible" is the long label that wraps at a large font. */
@Composable
fun BingeStatusButtonSpanishSample() {
    ScreenshotTheme { StatusButtons("Solicitar", "Solicitada", "En proceso", "Disponible", "Parcialmente disponible") }
}
