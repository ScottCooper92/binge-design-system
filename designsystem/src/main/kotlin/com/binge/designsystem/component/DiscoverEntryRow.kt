package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.BingeIcons
import com.binge.designsystem.R
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.titleMediumEmphasis

/**
 * The "browse everything" call-to-action that closes a hub or a search result list — a [ListRow]
 * carrying a tinted discover glyph, a title/subtitle pair and a chevron.
 *
 * It is a Row, not a Card or a Tile (CLAUDE.md › Component taxonomy), and takes its chrome from
 * [ListRow] rather than re-rolling it (#1224). The gradient is the one thing the primitive's flat
 * `containerColor` cannot express, so it arrives through `containerBrush`; the corner is the shape
 * scale's `large`, not a bespoke radius.
 *
 * [startPadding] is the nav-overlay inset and sits **outside** the row's clip, so it stays a caller
 * concern — a caller whose own parent already applied it (e.g. a grid's `navOverlayPadding`) passes
 * its own value to avoid adding it twice. See `SectionHeader`'s `startPadding`.
 */
@Composable
fun DiscoverEntryRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    startPadding: Dp = dimensionResource(R.dimen.screen_content_inset) + navOverlayStart(),
) {
    ListRow(
        modifier = modifier.padding(
            start = startPadding,
            end = dimensionResource(R.dimen.screen_content_inset),
        ),
        onClick = onClick,
        verticalAlignment = Alignment.CenterVertically,
        containerBrush = Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.secondaryContainer,
                MaterialTheme.colorScheme.surfaceContainer,
            ),
        ),
        leading = { DiscoverEntryGlyph() },
        trailing = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_l)),
            )
        },
    ) { contentModifier ->
        Column(
            modifier = contentModifier,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xxs)),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasis,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The row's leading visual: the discover glyph on a filled primary plate. */
@Composable
private fun DiscoverEntryGlyph() {
    Box(
        modifier = Modifier
            .size(dimensionResource(R.dimen.discover_entry_icon_size))
            .clip(BingeShapes.Large)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = BingeIcons.Discover,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(dimensionResource(R.dimen.discover_entry_icon_glyph)),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDiscoverEntryRow() {
    BingeExpressiveTheme {
        DiscoverEntryRow(
            title = "Browse all movies",
            subtitle = "Filter by genre, decade, provider and more",
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
