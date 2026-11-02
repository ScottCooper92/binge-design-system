package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.theme.BingeSentiment

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
 * @param statusChip the one pill beside the title, for the server's health: "Connected". Kept apart from [chips] so
 *   the two read as a flag on the app and a state of the server.
 * @param stats up to four totals, shown as the design system's [DetailStatRow]; empty hides the strip.
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
    stats: List<ServerCardStat> = emptyList(),
    sides: PaddingValues = resolvedContentPadding(),
) {
    HubCard(modifier, sides) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = logo,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.server_card_logo)),
            )
            Spacer(Modifier.weight(1f))
            chips.forEach { StatusChip(label = it.label, sentiment = it.sentiment) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
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
            statusChip?.let {
                Spacer(Modifier.width(dimensionResource(R.dimen.padding_s)))
                StatusChip(label = it.label, sentiment = it.sentiment)
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
