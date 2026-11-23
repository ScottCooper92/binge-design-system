@file:CatalogGroup("Chips")
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Movie
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLinkChip
import com.binge.designsystem.preview.ScreenshotTheme

/** Two chips that open outside the app, a film database and a website: each ends in the open-in-new arrow. */
@Composable
fun BingeLinkChipSample() {
    ScreenshotTheme {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeLinkChip(label = "IMDb", leadingIcon = Icons.Filled.Movie, onClick = {})
            BingeLinkChip(label = "Website", leadingIcon = Icons.Filled.Language, onClick = {})
        }
    }
}
