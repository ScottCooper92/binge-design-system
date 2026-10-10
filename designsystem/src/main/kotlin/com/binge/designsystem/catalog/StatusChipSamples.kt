@file:CatalogGroup("Tags and badges")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.StatusChip
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

/** A status chip in every sentiment, each labelled with a state it suits: waiting, in motion, done, refused, unknown. */
@Composable
fun StatusChipSample() {
    ScreenshotTheme {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            StatusChip(label = "Pending", sentiment = BingeSentiment.Caution)
            StatusChip(label = "Processing", sentiment = BingeSentiment.Info)
            StatusChip(label = "Available", sentiment = BingeSentiment.Positive)
            StatusChip(label = "Declined", sentiment = BingeSentiment.Negative)
            StatusChip(label = "Unknown", sentiment = BingeSentiment.Neutral)
        }
    }
}

/** A status chip without its dot, for a row that already shows a state glyph of its own. */
@Composable
fun StatusChipNoDotSample() {
    ScreenshotTheme {
        // In a Column, so the theme's Surface does not stretch the chip to its own minimum width.
        Column { StatusChip(label = "Pending", sentiment = BingeSentiment.Caution, showDot = false) }
    }
}
