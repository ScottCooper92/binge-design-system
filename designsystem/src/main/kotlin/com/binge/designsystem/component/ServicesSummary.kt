package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * Fixed-height summary of the user's selected streaming services. Swaps between an [emptyText] hint
 * (nothing picked) and [leadText] + the selected providers' logos, holding a constant minimum height
 * so the grid above it never shifts as the selection changes. Copy is passed in so the component
 * stays free of any feature's string resources.
 */
@Composable
fun ServicesSummary(
    selectedProviders: List<WatchProviderUi>,
    leadText: String,
    emptyText: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(R.dimen.services_summary_min_height)),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (selectedProviders.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            SelectedServices(selectedProviders = selectedProviders, leadText = leadText)
        }
    }
}

@Composable
private fun SelectedServices(selectedProviders: List<WatchProviderUi>, leadText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = leadText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_s)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            selectedProviders.forEach { provider ->
                SubcomposeAsyncImage(
                    model = provider.logoUrl,
                    contentDescription = provider.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.services_summary_logo))
                        .clip(BingeShapes.ElementSmall)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                )
            }
        }
    }
}
