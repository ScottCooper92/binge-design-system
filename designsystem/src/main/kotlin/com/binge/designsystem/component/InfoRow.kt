package com.binge.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * The value side of an [InfoRow]: plain text, a single tappable link, or a list of tappable
 * tokens (e.g. multiple studios). [Link]/[Links] carry the navigation [onClick]; the plain
 * path stays the default for the common non-linkable rows.
 */
sealed interface InfoValue {
    data class Plain(
        val text: String,
    ) : InfoValue

    data class Link(
        val text: String,
        val onClick: () -> Unit,
    ) : InfoValue

    data class Links(
        val links: List<InfoLink>,
    ) : InfoValue
}

/** One tappable token within an [InfoValue.Links] row. */
data class InfoLink(
    val text: String,
    val onClick: () -> Unit,
)

/** A label + [InfoValue] pair for [InfoRowList]. */
data class InfoRowEntry(
    val label: String,
    val value: InfoValue,
)

/** Plain-text [InfoRowEntry] convenience — the common non-linkable case. */
fun InfoRowEntry(label: String, value: String?): InfoRowEntry = InfoRowEntry(label, InfoValue.Plain(value.orEmpty()))

private fun InfoValue.isBlank(): Boolean =
    when (this) {
        is InfoValue.Plain -> text.isBlank()
        is InfoValue.Link -> text.isBlank()
        is InfoValue.Links -> links.all { it.text.isBlank() }
    }

/**
 * Key-value row for detail screens: label (fixed-width, SemiBold, muted) on the left,
 * value (Medium) on the right. The value is plain text, a single link, or a row of link
 * tokens. Skips rendering when the value is blank.
 */
@Composable
fun InfoRow(
    label: String,
    value: InfoValue,
    modifier: Modifier = Modifier,
) {
    if (value.isBlank()) return
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.info_row_gap)),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.defaultMinSize(minWidth = dimensionResource(R.dimen.info_row_label_min_width)),
        )
        when (value) {
            is InfoValue.Plain -> InfoRowText(value.text)
            is InfoValue.Link -> InfoRowLink(value.text, value.onClick)
            is InfoValue.Links -> InfoRowLinks(value.links)
        }
    }
}

/** Plain-text convenience overload — skips rendering when [value] is null/blank. */
@Composable
fun InfoRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
) {
    if (value.isNullOrBlank()) return
    InfoRow(label = label, value = InfoValue.Plain(value), modifier = modifier)
}

@Composable
private fun InfoRowText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun InfoRowLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoRowLinks(links: List<InfoLink>) {
    val tokens = links.filter { it.text.isNotBlank() }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_xs)),
    ) {
        tokens.forEachIndexed { index, link ->
            InfoRowLink(
                text = if (index < tokens.lastIndex) "${link.text}," else link.text,
                onClick = link.onClick,
            )
        }
    }
}

/** Stacked [InfoRow]s with consistent horizontal padding. Blank values are filtered. */
@Composable
fun InfoRowList(entries: List<InfoRowEntry>, modifier: Modifier = Modifier) {
    val visible = entries.filterNot { it.value.isBlank() }
    if (visible.isEmpty()) return
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.screen_content_inset)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.info_row_spacing_v)),
    ) {
        visible.forEach { InfoRow(label = it.label, value = it.value) }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewInfoRowList() {
    BingeExpressiveTheme {
        InfoRowList(
            entries =
                listOf(
                    InfoRowEntry("Director", InfoValue.Link("Christopher Nolan") {}),
                    InfoRowEntry(
                        "Studios",
                        InfoValue.Links(
                            listOf(
                                InfoLink("Warner Bros.") {},
                                InfoLink("Legendary") {},
                            ),
                        ),
                    ),
                    InfoRowEntry("Released", "2008"),
                    InfoRowEntry("Original language", "English"),
                ),
        )
    }
}
