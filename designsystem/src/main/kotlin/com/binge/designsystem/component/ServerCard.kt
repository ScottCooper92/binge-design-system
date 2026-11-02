package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeSentiment

/** The font scale from which the health pill drops under the address instead of sitting beside the name. */
private const val SERVER_CARD_STACK_FONT_SCALE = 1.5f

/** A status pill on a [ServerCard]: [label] names the state and [sentiment] colours it. */
data class ServerCardChip(
    val label: String,
    val sentiment: BingeSentiment,
)

/**
 * One total on a [ServerCard]. A null [value] is a total that has not arrived yet and shows a placeholder. A total the
 * viewer may not see is left out of the list altogether, so nobody gets a stat that never fills in.
 */
data class ServerCardStat(
    val icon: ImageVector,
    val value: String?,
    val label: String,
)

/**
 * A dashboard block: a tonal surface with the pane's [sides] around it and the medium spacing inside. Phone and
 * tablet only: nothing in it takes focus, so a TV page builds its own.
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
 * The card a server-backed app opens its home with: the app's [logo], the server's [title], an optional [subtitle]
 * (edition and version) and [address], status pills, and a strip of [stats].
 *
 * @param chips pills in the top row, beside the logo, for what the viewer may need to act on: "Demo", "Update available".
 *   They flow onto further lines, end-aligned, when they do not fit one; the logo stays at the top, and no pill shrinks or
 *   wraps its label.
 * @param statusChip the one pill beside the title, for the server's health: "Connected". Kept apart from [chips] so
 *   the two read as a flag on the app and a state of the server. From a font scale of 1.5 it drops under the address, so the
 *   name and address keep the width; below that it sits beside the name, which a long name ellipsises against.
 * @param stats up to four totals, shown as the design system's [DetailStatRow]; empty hides the strip.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ServerCard(
    title: String,
    logo: Painter,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    address: String? = null,
    chips: List<ServerCardChip> = emptyList(),
    statusChip: ServerCardChip? = null,
    stats: List<ServerCardStat> = emptyList(),
    sides: PaddingValues = resolvedContentPadding(),
) {
    val stacked = LocalDensity.current.fontScale >= SERVER_CARD_STACK_FONT_SCALE
    HubCard(modifier, sides) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = logo,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.server_card_logo)).align(Alignment.Top),
            )
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s), Alignment.End),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                chips.forEach { StatusChip(label = it.label, sentiment = it.sentiment, singleLine = true) }
            }
        }
        if (stacked) {
            ServerCardText(title, subtitle, address)
            statusChip?.let { StatusChip(label = it.label, sentiment = it.sentiment, singleLine = true) }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ServerCardText(title, subtitle, address, Modifier.weight(1f))
                statusChip?.let {
                    Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
                    StatusChip(label = it.label, sentiment = it.sentiment, singleLine = true)
                }
            }
        }
        if (stats.isNotEmpty()) {
            val placeholder = stringResource(R.string.server_card_stat_placeholder)
            DetailStatRow(
                stats = stats.map { DetailStat(it.icon, it.value ?: placeholder, it.label) },
                contentPadding = PaddingValues(),
            )
        }
    }
}

/** The name, the edition and the address: one line each for the name and address, ellipsised, the edition free to wrap. */
@Composable
private fun ServerCardText(
    title: String,
    subtitle: String?,
    address: String?,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
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
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = if (singleLine) 1 else Int.MAX_VALUE,
        overflow = if (singleLine) TextOverflow.Ellipsis else TextOverflow.Clip,
    )
}
