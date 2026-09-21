package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.component.ListRow
import com.binge.designsystem.component.ListRowHeader
import com.binge.designsystem.component.ListRowPoster
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public samples for the **Rows** group — the shared [ListRow] primitive (a tonal surface with an
 * optional leading visual, content column, and optional trailing element). Follows the catalog
 * convention on [MediaCardRatedSample]: the screenshot test renders these, the primitive's one
 * public fixture. A row stretches
 * to the canvas, so the samples let it fill the preview width rather than bounding it.
 */
@Composable
fun ListRowPosterSample() {
    ScreenshotTheme {
        ListRow(
            onClick = {},
            leading = { ListRowPoster(imageUrl = null, contentDescription = null) },
            trailing = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
        ) { contentModifier ->
            Column(modifier = contentModifier) {
                Text(
                    text = "Severance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
                Text(
                    text = "TV show · 2022",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * The bottom-anchoring pattern from [ListRow]'s doc: a short title leaves the trailing
 * `Spacer(weight(1f))` room to push the meta line down to the poster's bottom edge.
 */
@Composable
fun ListRowAnchoredShortTitleSample() {
    ScreenshotTheme {
        ListRow(
            onClick = {},
            leading = { ListRowPoster(imageUrl = null, contentDescription = null) },
        ) { contentModifier ->
            Column(modifier = contentModifier) {
                Text(
                    text = "Severance",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Requested by Alex · Today",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * The same anchoring pattern with a title long enough to already reach the poster's matched
 * height: the spacer has nothing left to give, so the title and the meta line simply stack with
 * no overlap and no clipping.
 */
@Composable
fun ListRowAnchoredLongTitleSample() {
    ScreenshotTheme {
        ListRow(
            onClick = {},
            leading = { ListRowPoster(imageUrl = null, contentDescription = null) },
        ) { contentModifier ->
            Column(modifier = contentModifier) {
                Text(
                    text = "A title so long it wraps across every single line the poster's " +
                        "height allows, filling the whole column before the ellipsis ever " +
                        "gets a chance to trim it down to size",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Requested by Alex · Today",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** A list row whose [ListRowHeader] title wraps to its two-line cap while the trailing action stays put. */
@Composable
fun ListRowHeaderSample() {
    ScreenshotTheme {
        ListRow(
            onClick = {},
            leading = { ListRowPoster(imageUrl = null, contentDescription = null) },
        ) { contentModifier ->
            Column(modifier = contentModifier) {
                ListRowHeader(
                    title = "A long episode title that wraps onto a second line so the badge stays put",
                    trailing = {
                        Text(
                            text = "Open",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
                Text(
                    text = "TV show · 2022",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
