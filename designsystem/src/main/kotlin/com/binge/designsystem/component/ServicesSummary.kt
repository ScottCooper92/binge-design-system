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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import coil3.compose.SubcomposeAsyncImage
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * Logos shown before the row folds the remainder into a [ServicesSummaryOverflowBadge]. Fixed
 * rather than measured against the available width: this renders in a settings-style row (Account's
 * watch-provider settings) and half-width onboarding panes alike, and a count this component picks
 * once is safe in the narrowest of those rather than depending on a caller-supplied max width.
 */
private const val MAX_VISIBLE_LOGOS = 5

/**
 * Fixed-height summary of the user's selected streaming services. Swaps between an [emptyText] hint
 * (nothing picked) and [leadText] + the selected providers' logos, holding a constant minimum height
 * so the grid above it never shifts as the selection changes. Copy is passed in so the component
 * stays free of any feature's string resources.
 *
 * A selection past [MAX_VISIBLE_LOGOS] stops rendering every logo — unbounded, that row overflows
 * its container — and folds the rest into a trailing "+N" badge instead, mirroring the count TV's
 * provider board shows once a region's full catalogue is selected.
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
    val visibleProviders = selectedProviders.take(MAX_VISIBLE_LOGOS)
    val overflowCount = selectedProviders.size - visibleProviders.size
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
            visibleProviders.forEach { provider ->
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
            if (overflowCount > 0) {
                ServicesSummaryOverflowBadge(count = overflowCount)
            }
        }
    }
}

/**
 * Trailing "+N" pill for the providers [SelectedServices] didn't have room to draw as logos. Sized
 * off [R.dimen.services_summary_logo] so it sits in the row at the same height as the tiles beside
 * it, but width follows its own text rather than the fixed logo size — "+12" needs more room than
 * "+2", and clipping the count back down to fit a square would defeat the point of showing it.
 */
@Composable
private fun ServicesSummaryOverflowBadge(count: Int) {
    val description = pluralStringResource(R.plurals.services_summary_overflow_services, count, count)
    Box(
        modifier = Modifier
            .heightIn(min = dimensionResource(R.dimen.services_summary_logo))
            .clip(BingeShapes.ElementSmall)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = dimensionResource(R.dimen.padding_s))
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.services_summary_overflow_count, count),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
