package com.binge.designsystem.component

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.tonalContainer

/** How strongly the edition and address read against the name in the header: the header's ink, a step quieter. */
private const val SERVER_CARD_CAPTION_ALPHA = 0.8f

/**
 * A status pill on a [ServerCard]: [label] names the state and [sentiment] colours it.
 *
 * In [ServerCard]'s `chips`, an [onClick] makes the pill a button: it takes the button role, shows a ring in its accent when
 * focused, and has a touch target of at least `min_touch_target`. It is drawn at its own size, so only its place in the card
 * grows, and a row that holds one is taller than a row of plain pills. [contentDescription] is what a screen reader says for
 * such a pill ("Release notes"); null reads the [label]. The `statusChip` stays a label whatever it carries: only `chips` take
 * a click. Without an [onClick] the pill is drawn, and read, as it always was, and [contentDescription] is not used.
 */
data class ServerCardChip(
    val label: String,
    val sentiment: BingeSentiment,
    val onClick: (() -> Unit)? = null,
    val contentDescription: String? = null,
)

/**
 * One total on a [ServerCard]. A null [value] is a total that has not arrived yet and shows a placeholder. A total the
 * viewer may not see is left out of the list altogether, so nobody gets a stat that never fills in.
 *
 * A [sentiment] colours the total's icon, value and label in that sentiment's accent: a count that is waiting on someone,
 * such as pending requests, as [BingeSentiment.Caution]. Null, the default, is a plain total. The caller decides when a
 * total deserves one, usually only when it is above zero.
 */
data class ServerCardStat(
    val icon: ImageVector,
    val value: String?,
    val label: String,
    val sentiment: BingeSentiment? = null,
)

/**
 * The full-width row a [ServerCard] shows under its header when the server has an update: [label] says so ("Update
 * available: 2.8.0") and [actionLabel] names what a tap opens ("Release notes"). The whole row is one button that runs
 * [onClick], with a touch target of at least `min_touch_target`.
 */
data class ServerCardUpdate(
    val label: String,
    val actionLabel: String,
    val onClick: () -> Unit,
)

/**
 * A dashboard block: a tonal surface with the pane's [sides] around it and the medium spacing inside. Phone and
 * tablet only: nothing in it takes focus, so a TV page builds its own.
 *
 * @need group dashboard content on a tonal card
 * @group cards
 * @replaces a hand-built Surface with a padded Column
 */
@Composable
fun HubCard(
    modifier: Modifier = Modifier,
    sides: PaddingValues = resolvedContentPadding(),
    contentPadding: PaddingValues = PaddingValues(dimensionResource(R.dimen.padding_m)),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(sides)
                .padding(vertical = dimensionResource(R.dimen.padding_s)),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.server_card_section_spacing)),
            content = content,
        )
    }
}

/**
 * The card a server-backed app opens its home with. A header in the brand's `primaryContainer` holds the app's [logo], the
 * pills, the server's [title], and an optional [subtitle] (edition and version) and [address], each on a line of its own.
 * Under it, on the card's surface, come the [update] row and a strip of [stats].
 *
 * @param chips pills in the header's top row, after the logo, for what the viewer may need to know: "Demo". They flow onto
 *   further lines, end-aligned, when they do not fit one; the logo stays at the top, and no pill shrinks or wraps its label.
 *   An update has its own [update] row rather than a pill.
 * @param statusChip the server's health, "Connected", drawn as the last pill of that row. It stays a label: it takes no
 *   click whatever it carries. The name and address below it keep the header's whole width at every font scale.
 * @param update the full-width row under the header when the server has an update; null hides it.
 * @param stats up to four totals, shown as the design system's [DetailStatRow]; empty hides the strip.
 *
 * @need show a connected server status and stats on a home card
 * @group cards
 * @replaces a hand-built header card with status pills and stats
 */
@Composable
fun ServerCard(
    title: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    address: String? = null,
    chips: List<ServerCardChip> = emptyList(),
    statusChip: ServerCardChip? = null,
    update: ServerCardUpdate? = null,
    stats: List<ServerCardStat> = emptyList(),
    sides: PaddingValues = resolvedContentPadding(),
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(sides)
                .padding(vertical = dimensionResource(R.dimen.padding_s)),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column {
            ServerCardHeader(title, logo, subtitle, address, chips, statusChip)
            if (update != null || stats.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.server_card_section_spacing)),
                ) {
                    update?.let { ServerCardUpdateRow(it) }
                    if (stats.isNotEmpty()) {
                        val placeholder = stringResource(R.string.server_card_stat_placeholder)
                        DetailStatRow(
                            stats =
                                stats.map {
                                    DetailStat(
                                        icon = it.icon,
                                        value = it.value ?: placeholder,
                                        label = it.label,
                                        accent = it.sentiment?.accent() ?: Color.Unspecified,
                                    )
                                },
                            contentPadding = PaddingValues(),
                        )
                    }
                }
            }
        }
    }
}

/** The brand block: the logo and every pill in one row, then the name, the edition and the address, in `onPrimaryContainer`. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ServerCardHeader(
    title: String,
    logo: Painter,
    subtitle: String?,
    address: String?,
    chips: List<ServerCardChip>,
    statusChip: ServerCardChip?,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.server_card_section_spacing)),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = logo,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(dimensionResource(R.dimen.server_card_logo)).align(Alignment.Top),
                )
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s), Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
                    itemVerticalAlignment = Alignment.CenterVertically,
                ) {
                    chips.forEach { ServerCardChipView(it) }
                    statusChip?.let { StatusChip(label = it.label, sentiment = it.sentiment, singleLine = true) }
                }
            }
            ServerCardText(title, subtitle, address)
        }
    }
}

/** The update row: a tonal caution wash, an arrow, the label and, at the end, the action. One button, the whole width. */
@Composable
private fun ServerCardUpdateRow(update: ServerCardUpdate) {
    val accent = BingeSentiment.Caution.accent()
    Surface(
        onClick = update.onClick,
        // A clickable `Surface` is not announced as a button on its own; the row is one, so it says so.
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = dimensionResource(R.dimen.min_touch_target))
            .semantics { role = Role.Button },
        shape = MaterialTheme.shapes.medium,
        color = accent.tonalContainer(),
        contentColor = accent,
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = dimensionResource(R.dimen.padding_sm),
                    vertical = dimensionResource(R.dimen.padding_s),
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowUpward,
                contentDescription = null,
                modifier = Modifier.size(dimensionResource(R.dimen.server_card_update_icon)),
            )
            Text(text = update.label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            Text(text = update.actionLabel, style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** A chip as the card draws it: the pill alone, or, with an [ServerCardChip.onClick], the pill as a button. */
@Composable
private fun ServerCardChipView(chip: ServerCardChip) {
    val onClick = chip.onClick
    if (onClick == null) {
        StatusChip(label = chip.label, sentiment = chip.sentiment, singleLine = true)
        return
    }
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val description = chip.contentDescription
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(BingeShapes.Pill)
            .border(
                width = dimensionResource(R.dimen.server_card_chip_focus_ring),
                color = if (focused) chip.sentiment.accent() else Color.Transparent,
                shape = BingeShapes.Pill,
            ).clickable(interactionSource = interactions, indication = LocalIndication.current, role = Role.Button, onClick = onClick)
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        // With a description of its own, the pill's text is not read a second time beside it.
        StatusChip(
            label = chip.label,
            sentiment = chip.sentiment,
            singleLine = true,
            modifier = if (description != null) Modifier.clearAndSetSemantics {} else Modifier,
        )
    }
}

/** The name, the edition and the address, one line each: the name and address ellipsised, the edition free to wrap. */
@Composable
private fun ServerCardText(
    title: String,
    subtitle: String?,
    address: String?,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        subtitle?.let { CardCaption(it, singleLine = false) }
        address?.let { CardCaption(it, singleLine = true) }
    }
}

@Composable
private fun CardCaption(text: String, singleLine: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = SERVER_CARD_CAPTION_ALPHA),
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
    )
}
