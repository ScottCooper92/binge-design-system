package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

/** The font scale from which a compact [DetailInfoGrid] falls back to one column, so a value keeps a readable width. */
private const val SINGLE_COLUMN_FONT_SCALE = 1.5f

/**
 * How many columns a [DetailInfoGrid] has: 2 on a phone, 2 from 600dp and 3 from 840dp. On a phone at a font scale of
 * 1.5 or more it is 1 (a list), because two columns leave a value too little width to read. Measured against
 * [LocalPaneWidth] when the page shares the window with another pane, and against the window otherwise, the way
 * [com.binge.designsystem.mediaGridColumns] does.
 */
@Composable
fun detailInfoColumns(): Int {
    val mediumBreakpoint = dimensionResource(R.dimen.content_inset_medium_breakpoint)
    val expandedBreakpoint = dimensionResource(R.dimen.content_inset_expanded_breakpoint)
    val paneWidth = LocalPaneWidth.current
    val columns = when {
        paneWidth == null -> integerResource(R.integer.detail_info_columns)
        paneWidth >= expandedBreakpoint -> integerResource(R.integer.detail_info_columns_expanded)
        paneWidth >= mediumBreakpoint -> integerResource(R.integer.detail_info_columns_medium)
        else -> integerResource(R.integer.detail_info_columns_compact)
    }
    val width = paneWidth ?: LocalConfiguration.current.screenWidthDp.dp
    val largeFont = LocalDensity.current.fontScale >= SINGLE_COLUMN_FONT_SCALE
    return if (largeFont && width < mediumBreakpoint) 1 else columns
}

/**
 * The facts at the end of a detail page (director, dates, languages, studios, status, budget), each an icon, a muted
 * label and its value. They sit in columns across the width, 3 at expanded width and 2 at medium and compact, falling
 * back to one list on a phone at a large font. A long value wraps inside its own column at word boundaries. A line with a
 * word wider than its column puts its facts one above the other at the full width instead, so a word is never broken
 * mid-word, and a blank value is left out. The grid insets itself by [LocalHeroReadingMargin] and the page's own content
 * padding, so it lines up with the sections around it on a wide window.
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
            DetailInfoLine(
                columns = count,
                columnGap = dimensionResource(R.dimen.detail_info_column_gap),
                rowGap = dimensionResource(R.dimen.detail_info_row_gap),
                cellMinWidths = line.map { detailInfoCellMinWidth(it) },
            ) {
                line.forEach { entry -> DetailInfoCell(entry) }
            }
        }
    }
}

/**
 * One line of the grid: its cells side by side in [columns] equal columns, or, when a cell's [cellMinWidths] entry (the
 * width of its longest word and its icon) is wider than the column it would get, one above the other at the full width.
 * A word then has the whole line instead of being broken mid-word in a narrow column, and a line whose words all fit
 * keeps its columns. A line with fewer cells than [columns] keeps the column width, so its cells line up with the line
 * above.
 */
@Composable
private fun DetailInfoLine(
    columns: Int,
    columnGap: Dp,
    rowGap: Dp,
    cellMinWidths: List<Dp>,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = Modifier.fillMaxWidth()) { measurables, constraints ->
        val gap = columnGap.roundToPx()
        val width = constraints.maxWidth
        val columnWidth = ((width - gap * (columns - 1)) / columns).coerceAtLeast(0)
        val fits = cellMinWidths.all { it.roundToPx() <= columnWidth }
        if (fits) {
            val placeables = measurables.map { it.measure(Constraints.fixedWidth(columnWidth)) }
            layout(width, placeables.maxOfOrNull { it.height } ?: 0) {
                placeables.forEachIndexed { index, placeable -> placeable.place(index * (columnWidth + gap), 0) }
            }
        } else {
            val placeables = measurables.map { it.measure(Constraints.fixedWidth(width)) }
            val stackGap = rowGap.roundToPx()
            var y = 0
            layout(width, placeables.sumOf { it.height } + stackGap * (placeables.size - 1).coerceAtLeast(0)) {
                placeables.forEach { placeable ->
                    placeable.place(0, y)
                    y += placeable.height + stackGap
                }
            }
        }
    }
}

/**
 * The width a [DetailInfoCell] needs so that none of its words breaks mid-word: its icon and gap, then the widest word
 * of its label or of its value, measured at the current font scale. Measured from the text, because a row of links wraps
 * in a flow layout whose own minimum width is the whole row, not its longest word.
 */
@Composable
private fun detailInfoCellMinWidth(entry: DetailInfoEntry): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelStyle = MaterialTheme.typography.labelMedium
    val valueStyle = MaterialTheme.typography.bodyMedium
    val valueText = when (val value = entry.value) {
        is InfoValue.Plain -> value.text
        is InfoValue.Link -> value.text
        is InfoValue.Links -> value.links.joinToString(" ") { it.text }
    }
    val widestWord = with(density) {
        fun widest(text: String, style: TextStyle): Int =
            text.split(' ', '\n').filter { it.isNotEmpty() }.maxOfOrNull { measurer.measure(it, style).size.width } ?: 0
        maxOf(widest(entry.label, labelStyle), widest(valueText, valueStyle)).toDp()
    }
    return dimensionResource(R.dimen.detail_info_icon_size) + dimensionResource(R.dimen.detail_info_icon_gap) + widestWord
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
