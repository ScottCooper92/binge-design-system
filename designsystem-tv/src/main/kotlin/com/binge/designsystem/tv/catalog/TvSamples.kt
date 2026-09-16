package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvButtonSurface
import com.binge.designsystem.tv.component.TvCardRow
import com.binge.designsystem.tv.component.TvExcludedMark
import com.binge.designsystem.tv.component.TvIconButtonSurface
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.component.TvQrCode
import com.binge.designsystem.tv.component.TvRowEmphasis
import com.binge.designsystem.tv.component.TvSectionTitle
import com.binge.designsystem.tv.component.TvSelectedTick
import com.binge.designsystem.tv.component.TvSelectedTickBadge
import com.binge.designsystem.tv.component.TvVerticalDivider
import com.binge.designsystem.tv.component.containerColor
import com.binge.designsystem.tv.component.contentColor
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.nav.BingeTvNavRail
import com.binge.designsystem.tv.nav.TvNavRailItem
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/*
 * The one public fixture per TV component, rendered by the screenshot frames. Focus is a parameter on every
 * sample that has a focused appearance, so the lit state is captured without a focus request.
 */

/** Enough bands that the rail's ramp is legible against them, and an odd count so neither colour ends both edges. */
private const val SAMPLE_ARTWORK_BANDS = 9

/** The button across its roles and both focus states, through the stateless surface. */
@Composable
fun TvButtonSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButtonSurface("Sign in", TvButtonStyle.Primary, enabled = true, isFocused = false)
            TvButtonSurface("Sign in", TvButtonStyle.Primary, enabled = true, isFocused = true)
            TvButtonSurface("Settings", TvButtonStyle.Secondary, enabled = true, isFocused = false)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButtonSurface("Sign out", TvButtonStyle.Destructive, enabled = true, isFocused = false)
            TvButtonSurface("Sign out", TvButtonStyle.Destructive, enabled = true, isFocused = true)
            TvButtonSurface("Continue", TvButtonStyle.Primary, enabled = false, isFocused = false)
        }
    }
}

/** Disabled beside enabled on a surface panel, where a disabled control that composites to nothing shows as a failure. */
@Composable
fun TvButtonOnPanelSample() {
    Column(
        modifier =
            Modifier
                .padding(dimensionResource(TvR.dimen.tv_overscan_horizontal))
                .background(MaterialTheme.colorScheme.surface, BingeShapes.AccountCard)
                .padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButtonSurface("Checking…", TvButtonStyle.Primary, enabled = false, isFocused = false)
            TvButtonSurface("Checking…", TvButtonStyle.Secondary, enabled = false, isFocused = false)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButtonSurface("I've approved it", TvButtonStyle.Primary, enabled = true, isFocused = false)
            TvButtonSurface("Settings", TvButtonStyle.Secondary, enabled = true, isFocused = false)
        }
    }
}

/**
 * The icon button at rest, revealed on focus and disabled, across its roles — the same coverage
 * [TvButtonSample] gives [TvButton]. The middle column of each row is the revealed frame: the surface
 * growing from a circle into a labelled pill.
 */
@Composable
fun TvIconButtonSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvIconButtonSurface(Icons.Filled.Add, "Add to list", TvButtonStyle.Secondary, enabled = true, isFocused = false)
            TvIconButtonSurface(Icons.Filled.Add, "Add to list", TvButtonStyle.Secondary, enabled = true, isFocused = true)
            TvIconButtonSurface(Icons.Filled.PlayArrow, "Play", TvButtonStyle.Primary, enabled = true, isFocused = false)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvIconButtonSurface(Icons.Filled.Delete, "Remove", TvButtonStyle.Destructive, enabled = true, isFocused = false)
            TvIconButtonSurface(Icons.Filled.Delete, "Remove", TvButtonStyle.Destructive, enabled = true, isFocused = true)
            TvIconButtonSurface(Icons.Filled.Bookmark, "Watchlist", TvButtonStyle.Secondary, enabled = false, isFocused = false)
        }
    }
}

/** The in-content section title over the standard gutter inset. */
@Composable
fun TvSectionTitleSample() {
    TvSectionTitle(text = "Cast & crew")
}

/** A heading over fixed-width plates with a trailing tile: the row's layout, not any one card's chrome. */
@Composable
fun TvCardRowSample() {
    val cellWidth = dimensionResource(DesR.dimen.card_width)
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TvCardRow(
            heading = "Seasons",
            items = (1..4).toList(),
            key = { it },
            cellWidth = cellWidth,
            trailing = { _, _, cellModifier -> SamplePlate(label = "More", modifier = cellModifier.width(cellWidth)) },
        ) { index, _, _, cellModifier ->
            SamplePlate(label = "Season $index", modifier = cellModifier)
        }
    }
}

/** A message with a headline, art and an action, centred as a full-screen state is. */
@Composable
fun TvMessagePlateSample() {
    TvMessagePlate(
        headline = "No results for \"kurosawa\"",
        body = "Check the spelling, or try a shorter search.",
        icon = Icons.Filled.SearchOff,
        alignment = Alignment.Center,
        actions = { TvButton(label = "Clear search", onClick = {}, style = TvButtonStyle.Primary) },
    )
}

/** The in-pane placement: top-start, staying with the band above it. */
@Composable
fun TvMessagePlateTopStartSample() {
    TvMessagePlate(
        headline = "No results for \"kurosawa\"",
        body = "Check the spelling, or try a shorter search.",
    )
}

/** The focus ring, the lift and the overscan margin at TV scale, with focus as a flag. */
@Composable
fun TvFocusIndicatorSample() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                    vertical = dimensionResource(TvR.dimen.tv_overscan_vertical),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l))) {
            FocusSampleTile(label = "rest", isFocused = false)
            FocusSampleTile(label = "focused", isFocused = true)
            FocusSampleTile(label = "rest", isFocused = false)
        }
    }
}

/**
 * The rail shell with a header, destinations and a footer; [expanded] pins which resting shape renders.
 *
 * [artworkBehind] pins the other half of the rail's fill, and puts something behind it worth seeing through
 * to — without both, a frame cannot tell a scrim from a solid panel.
 */
@Composable
fun TvNavRailSample(
    expanded: Boolean,
    selectedKey: String = "movies",
    artworkBehind: Boolean = false,
) {
    BingeTvNavRail(
        header = NavRailSampleHeader,
        items = NavRailSampleItems,
        footer = NavRailSampleFooter,
        selectedKey = selectedKey,
        onSelect = {},
        expanded = expanded,
        artworkBehind = artworkBehind,
        content = { NavRailSampleContent(artworkBehind = artworkBehind) },
    )
}

/** The initials avatar at the rail's size, for a name with no artwork. */
@Composable
fun TvInitialsAvatarSample() {
    BingeTvInitialsAvatar(
        name = "Alexander Hamilton-Montgomery",
        size = dimensionResource(TvR.dimen.tv_nav_rail_avatar_size),
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
    )
}

val NavRailSampleHeader: TvNavRailItem =
    TvNavRailItem(
        key = "account",
        label = "Alexander Hamilton-Montgomery",
        icon = Icons.Filled.Person,
        displayName = "Alexander Hamilton-Montgomery",
    )

val NavRailSampleItems: List<TvNavRailItem> =
    listOf(
        TvNavRailItem(key = "search", label = "Search", icon = Icons.Filled.Search),
        TvNavRailItem(key = "movies", label = "Movies", icon = Icons.Filled.Movie),
        TvNavRailItem(key = "tv", label = "TV shows", icon = Icons.Filled.Tv),
        TvNavRailItem(key = "watchlist", label = "Watchlist", icon = Icons.Filled.Bookmark),
        TvNavRailItem(key = "lists", label = "Lists", icon = Icons.AutoMirrored.Filled.FormatListBulleted),
    )

val NavRailSampleFooter: TvNavRailItem = TvNavRailItem(key = "settings", label = "Settings", icon = Icons.Filled.Settings)

@Composable
private fun NavRailSampleContent(artworkBehind: Boolean = false) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        if (artworkBehind) NavRailSampleArtwork()
        Box(
            modifier = Modifier.fillMaxSize().padding(dimensionResource(TvR.dimen.tv_overscan_horizontal)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Content",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * A stand-in for a backdrop: full-bleed bands that run under the rail, so a frame can read how much of them
 * the fill lets through. Bands rather than a picture — this repository's frames render with no network, and a
 * flat colour behind the rail cannot tell a third of a fill from all of it.
 */
@Composable
private fun NavRailSampleArtwork() {
    Column(modifier = Modifier.fillMaxSize()) {
        repeat(SAMPLE_ARTWORK_BANDS) { index ->
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        ),
            )
        }
    }
}

@Composable
private fun SamplePlate(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .aspectRatio(2f / 3f)
                .clip(BingeShapes.MediaCard)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun FocusSampleTile(label: String, isFocused: Boolean) {
    Surface(
        modifier =
            Modifier
                .size(width = dimensionResource(DesR.dimen.card_width), height = dimensionResource(DesR.dimen.card_height))
                .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard),
        shape = BingeShapes.MediaCard,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/** The divider between two side-by-side panes, at the height its caller gives it. */
@Composable
fun TvVerticalDividerSample() {
    Row(
        modifier = Modifier.height(dimensionResource(DesR.dimen.card_height)).padding(dimensionResource(DesR.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        PaneSampleColumn("List")
        TvVerticalDivider()
        PaneSampleColumn("Detail")
    }
}

/** All three emphases side by side: the pair is only judgeable against each other and the resting row. */
@Composable
fun TvRowEmphasisSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        EmphasisSampleRow("Resting", TvRowEmphasis.Resting)
        EmphasisSampleRow("Current, focus in the other pane", TvRowEmphasis.Current)
        EmphasisSampleRow("Focused", TvRowEmphasis.Focused)
    }
}

/** The three selection marks, including the badge that carries its own disc for use over artwork. */
@Composable
fun TvSelectedTickSample() {
    Row(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvSelectedTick(tint = MaterialTheme.colorScheme.primary)
        TvExcludedMark(tint = MaterialTheme.colorScheme.onSurfaceVariant)
        TvSelectedTickBadge()
    }
}

/** A symbol at the size a panel shows it. The plate is white by specification, not by theme. */
@Composable
fun TvQrCodeSample() {
    Box(modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l))) {
        TvQrCode(
            content = "https://example.com/link?code=BINGE-1234",
            contentDescription = "Scan to finish signing in",
            modifier = Modifier.size(dimensionResource(DesR.dimen.card_height)),
        )
    }
}

@Composable
private fun PaneSampleColumn(label: String) {
    Box(
        modifier = Modifier.width(dimensionResource(DesR.dimen.card_width)).fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun EmphasisSampleRow(label: String, emphasis: TvRowEmphasis) {
    Box(
        modifier =
            Modifier
                .width(dimensionResource(TvR.dimen.tv_message_plate_max_width))
                .background(
                    emphasis.containerColor(resting = MaterialTheme.colorScheme.surface),
                    BingeShapes.TvListItem,
                ).padding(dimensionResource(DesR.dimen.padding_m)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = emphasis.contentColor(resting = MaterialTheme.colorScheme.onSurface),
        )
    }
}
