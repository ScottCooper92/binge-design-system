@file:CatalogGroup("Tags and badges")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeIconPlate
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.accent

/**
 * The plate a row leads with, in the three forms a caller gives it: a glyph on the primary wash, a glyph tinted
 * with a sentiment, and text (a flag, say) in the content slot.
 */
@Composable
fun BingeIconPlateSample() {
    ScreenshotTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s))) {
            BingeIconPlate(icon = Icons.Filled.Folder)
            BingeIconPlate(icon = Icons.Filled.Subtitles, tint = BingeSentiment.Info.accent())
            BingeIconPlate { Text("EN", style = MaterialTheme.typography.titleMedium) }
        }
    }
}
