package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.LocalPaneWidth
import com.binge.designsystem.R
import com.binge.designsystem.resolvedContentPadding
import com.binge.designsystem.template.LocalHeroReadingMargin

/**
 * One fact in a [DetailInfoGrid]: its [icon], a [label] and the [value], which is plain text, one link or a list of
 * links (tappable people or studios). The icon is decoration; the label and value carry the meaning.
 */
data class DetailInfoEntry(
    val icon: ImageVector,
    val label: String,
    val value: InfoValue,
)

/** Plain-text [DetailInfoEntry] convenience, the common non-linkable fact. */
fun DetailInfoEntry(
    icon: ImageVector,
    label: String,
    value: String?,
): DetailInfoEntry = DetailInfoEntry(icon, label, InfoValue.Plain(value.orEmpty()))

/**
 * How many columns a [DetailInfoGrid] has: 1 (a list) on a phone, 2 from 600dp and 3 from 840dp. Measured against
 * [LocalPaneWidth] when the page shares the window with another pane, and against the window otherwise, the way
 * [com.binge.designsystem.mediaGridColumns] does.
 */
@Composable
fun detailInfoColumns(): Int {
    val paneWidth = LocalPaneWidth.current ?: return integerResource(R.integer.detail_info_columns)
    val mediumBreakpoint = dimensionResource(R.dimen.content_inset_medium_breakpoint)
    val expandedBreakpoint = dimensionResource(R.dimen.content_inset_expanded_breakpoint)
    return when {
        paneWidth >= expandedBreakpoint -> integerResource(R.integer.detail_info_columns_expanded)
        paneWidth >= mediumBreakpoint -> integerResource(R.integer.detail_info_columns_medium)
        else -> integerResource(R.integer.detail_info_columns_compact)
    }
}

/**
 * The facts at the end of a detail page (director, dates, languages, studios, status, budget), each an icon, a muted
 * label and its value. They sit in columns across the width, 3 at expanded width and 2 at medium, and as one list on a
 * phone. A long value wraps inside its own column, and a blank one is left out. The grid insets itself by
 * [LocalHeroReadingMargin] and the page's own content padding, so it lines up with the sections around it on a wide window.
 *
 * Laid out as the TV detail page's info columns are, so phone, tablet and TV read the same. [columns] overrides the
 * width-driven count, for a grid in a space the window does not describe.
 *
 * @need show a title's facts (director, dates, languages, studios) in columns at the end of a detail page
 * @group rows
 * @sample DetailInfoGridSample
 * @replaces a hand-built Column of label and value rows
 */
@Composable
fun DetailInfoGrid(
    entries: List<DetailInfoEntry>,
    modifier: Modifier = Modifier,
    columns: Int = detailInfoColumns(),
) {
    val visible = entries.filterNot { it.value.isBlank() }
    if (visible.isEmpty()) return
    val count = columns.coerceAtLeast(1)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LocalHeroReadingMargin.current)
            .padding(resolvedContentPadding()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_info_row_gap)),
    ) {
        visible.chunked(count).forEach { line ->
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_info_column_gap))) {
                line.forEach { entry -> DetailInfoCell(entry, Modifier.weight(1f)) }
                repeat(count - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DetailInfoCell(entry: DetailInfoEntry, modifier: Modifier = Modifier) {
    Row(
        // A plain fact reads as one item, "Director Christopher Nolan"; a link must stay its own tappable item.
        modifier = modifier.semantics(mergeDescendants = entry.value is InfoValue.Plain) {},
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_info_icon_gap)),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = entry.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(dimensionResource(R.dimen.detail_info_icon_size)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            when (val value = entry.value) {
                is InfoValue.Plain -> InfoRowText(value.text)
                is InfoValue.Link -> InfoRowLink(value.text, value.onClick)
                is InfoValue.Links -> InfoRowLinks(value.links)
            }
        }
    }
}
