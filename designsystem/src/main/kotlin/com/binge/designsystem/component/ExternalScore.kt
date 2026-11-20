package com.binge.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes
import kotlin.math.floor

/** Which review source an [ExternalScore] comes from, and so which glyph it wears. */
enum class ExternalScoreKind {
    /** Critics' score, at or above the consumer's fresh threshold: a tomato. */
    CriticsFresh,

    /** Critics' score below the consumer's fresh threshold: a splat. */
    CriticsRotten,

    /** Audience score: popcorn. */
    Audience,

    /** An IMDb-style user rating: a small outlined text tag in the theme's own ink, not IMDb's colours. */
    Imdb,
}

/**
 * One review score for a meta line: a glyph for [kind] and the already-formatted [value] (such as `97%` or `9.0`).
 * [contentDescription] is what a screen reader reads for the pair, such as "Critics 97 percent", so the consumer
 * owns the words and the language.
 */
data class ExternalScore(
    val kind: ExternalScoreKind,
    val value: String,
    val contentDescription: String,
)

private const val IMDB_BADGE_TEXT = "IMDb"

/**
 * A score's glyph and value as one item, read by a screen reader as [ExternalScore.contentDescription].
 * The glyphs are our own drawings and look the same in light and dark; the IMDb tag takes the theme's ink. [color] is the value's ink.
 */
@Composable
internal fun ExternalScoreItem(
    score: ExternalScore,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = score.contentDescription },
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.meta_item_icon_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExternalScoreGlyph(score.kind)
        Text(
            text = score.value,
            style = MaterialTheme.typography.labelLarge,
            color = color,
            maxLines = 1,
        )
    }
}

@Composable
private fun ExternalScoreGlyph(kind: ExternalScoreKind) {
    val drawable = when (kind) {
        ExternalScoreKind.CriticsFresh -> R.drawable.ic_score_tomato
        ExternalScoreKind.CriticsRotten -> R.drawable.ic_score_splat
        ExternalScoreKind.Audience -> R.drawable.ic_score_popcorn
        ExternalScoreKind.Imdb -> null
    }
    if (drawable != null) {
        Image(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.external_score_icon_size)),
        )
    } else {
        Text(
            text = IMDB_BADGE_TEXT,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier
                .border(
                    width = dimensionResource(R.dimen.external_score_badge_border),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = BingeShapes.ElementExtraSmall,
                ).padding(
                    horizontal = dimensionResource(R.dimen.external_score_badge_padding_h),
                    vertical = dimensionResource(R.dimen.external_score_badge_padding_v),
                ),
        )
    }
}

/** One item of a [MetaFlow] line. */
internal sealed interface MetaEntry {
    /** An item that draws itself and stays whole, such as a score or a status. */
    class Content(
        val content: @Composable () -> Unit,
    ) : MetaEntry

    /** Text that may wrap over several lines, such as a list of genres, set in the meta line's style and [color]. */
    class WrappingText(
        val text: String,
        val color: Color,
    ) : MetaEntry
}

/**
 * A meta line: [entries] side by side with the meta dot between neighbours on the same line, wrapping when they do not fit. A dot is drawn only
 * between two entries on one line, never at a break, so no line ends or starts on a separator. Every meta line, the hero's, the detail header's
 * scores and its genres, is this one layout ([BreakableMetaLine]), so they all wrap the same way.
 */
@Composable
internal fun MetaFlow(entries: List<MetaEntry>, modifier: Modifier = Modifier) {
    BreakableMetaLine(
        entries = entries.map { entry ->
            when (entry) {
                is MetaEntry.Content -> entry.content
                is MetaEntry.WrappingText -> {
                    {
                        Text(text = entry.text, style = MaterialTheme.typography.labelLarge, color = entry.color)
                    }
                }
            }
        },
        modifier = modifier,
    )
}

/**
 * A meta line that wraps without ever ending a line on a separator: [entries] side by side with the meta dot between neighbours on the same
 * line, and none at a line break, so a wrapped line neither ends nor starts on a dot. An entry that does not fit what is left of a line starts
 * the next one, whole.
 */
@Composable
internal fun BreakableMetaLine(entries: List<@Composable () -> Unit>, modifier: Modifier = Modifier) {
    val gap = dimensionResource(R.dimen.hero_meta_gap)
    val lineGap = dimensionResource(R.dimen.meta_flow_line_gap)
    Layout(
        content = {
            entries.forEachIndexed { index, entry ->
                if (index > 0) HeroMetaDot()
                Box { entry() }
            }
        },
        modifier = modifier,
    ) { measurables, constraints ->
        if (entries.isEmpty()) return@Layout layout(0, 0) {}
        val gapPx = gap.roundToPx()
        val lineGapPx = lineGap.roundToPx()
        val maxWidth = constraints.maxWidth
        val loose = Constraints(maxWidth = maxWidth)
        // The children alternate: the first entry, then a dot and an entry for each one after it.
        val entryPlaceables = entries.indices.map { measurables[if (it == 0) 0 else 2 * it].measure(loose) }
        val dotPlaceables = (1 until entries.size).map { measurables[2 * it - 1].measure(Constraints()) }

        // An item is placed at its x on a line, and centred on the line the way a flow of rows centres them: a dot is centred in the row it ends,
        // which is as tall as the entry before it, and that row is centred on the line. Two steps, not one, so the pixels land where they did.
        class Placed(
            val placeable: androidx.compose.ui.layout.Placeable,
            val x: Int,
            val line: Int,
            val rowHeight: Int,
            val dot: Boolean,
        )
        val placed = mutableListOf<Placed>()
        var line = 0
        var x = 0
        entryPlaceables.forEachIndexed { index, entry ->
            val dotWidth = if (index == 0) 0 else dotPlaceables[index - 1].width
            val withDot = if (index == 0) 0 else gapPx + dotWidth + gapPx
            if (index > 0 && x + withDot + entry.width > maxWidth) {
                // The previous entry ends its line with no dot after it, so its row is only the entry.
                line++
                placed += Placed(entry, 0, line, entry.height, dot = false)
                x = entry.width
            } else {
                if (index > 0) {
                    val before = entryPlaceables[index - 1]
                    placed +=
                        Placed(dotPlaceables[index - 1], x + gapPx, line, maxOf(before.height, dotPlaceables[index - 1].height), dot = true)
                }
                placed += Placed(entry, x + withDot, line, entry.height, dot = false)
                x += withDot + entry.width
            }
        }
        val lineHeights = (0..line).map { l -> placed.filter { it.line == l }.maxOf { it.rowHeight } }
        val tops = lineHeights.runningFold(0) { top, height -> top + height + lineGapPx }
        // As the flow it replaces: the whole width on offer, so a parent that centres or aligns it sees the same box as before.
        val width = if (constraints.hasBoundedWidth) maxWidth else placed.maxOf { it.x + it.placeable.width }

        fun centred(space: Int, size: Int) = floor((space - size) / 2f + 0.5f).toInt()
        layout(width, lineHeights.sum() + lineGapPx * line) {
            placed.forEachIndexed { index, item ->
                val y = if (item.dot) {
                    // The dot is centred in the row of the entry before it, and that row on the line.
                    val before = placed[index - 1]
                    tops[item.line] + centred(lineHeights[item.line], before.rowHeight) + centred(item.rowHeight, item.placeable.height)
                } else {
                    tops[item.line] + centred(lineHeights[item.line], item.rowHeight) + centred(item.rowHeight, item.placeable.height)
                }
                item.placeable.placeRelative(item.x, y)
            }
        }
    }
}

/**
 * A row of [scores], each a glyph and its value, separated by the meta dot, for a screen's own meta line. It wraps
 * between scores when they do not fit, and the dot is drawn only between two scores on one line, so no line ends or starts on a separator.
 * A screen reader reads each score as its [ExternalScore.contentDescription]. [color] is the values' ink.
 *
 * @need show review scores from other sources (critics, audience, IMDb) next to a title, each with its icon
 * @group rows
 * @sample ExternalScoreLineSample
 */
@Composable
fun ExternalScoreLine(
    scores: List<ExternalScore>,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onBackground,
) {
    if (scores.isEmpty()) return
    MetaFlow(
        entries = scores.map { score -> MetaEntry.Content { ExternalScoreItem(score, color) } },
        modifier = modifier,
    )
}

/** The genre line of a detail header: the genres in the accent, wrapping between words when they do not fit. */
@Composable
internal fun GenreLine(genres: String, modifier: Modifier = Modifier) {
    MetaFlow(
        entries = listOf(MetaEntry.WrappingText(genres, MaterialTheme.colorScheme.primary)),
        modifier = modifier,
    )
}
