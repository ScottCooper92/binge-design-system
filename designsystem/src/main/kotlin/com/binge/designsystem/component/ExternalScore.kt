package com.binge.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

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
    /** An item that draws itself and stays whole, such as a score or a status. Its dot follows it on its own line. */
    class Content(
        val content: @Composable () -> Unit,
    ) : MetaEntry

    /**
     * Text that may wrap over several lines, such as a list of genres. Its dot is drawn inline after its last word, so
     * it sits on the line the text ends on rather than at the far edge beside the middle of a wrapped block.
     */
    class WrappingText(
        val text: String,
        val color: Color,
    ) : MetaEntry
}

/**
 * A meta line: [entries] side by side, each followed by the meta dot except the last, so a line that wraps at a large
 * font ends on a dot rather than starting on one.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MetaFlow(entries: List<MetaEntry>, modifier: Modifier = Modifier) {
    val gap = dimensionResource(R.dimen.hero_meta_gap)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.meta_flow_line_gap)),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        entries.forEachIndexed { index, entry ->
            val last = index == entries.lastIndex
            when (entry) {
                is MetaEntry.Content ->
                    Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                        // The entry takes what the dot leaves, so an item as wide as the line still ends on its dot.
                        Box(Modifier.weight(1f, fill = false)) { entry.content() }
                        if (!last) HeroMetaDot()
                    }
                is MetaEntry.WrappingText -> WrappingMetaText(entry, withDot = !last)
            }
        }
    }
}

private const val META_DOT_ID = "meta-dot"

/** The word joiner keeps the inline dot on the line of the word before it. */
private const val WORD_JOINER = "\u2060"

@Composable
private fun WrappingMetaText(entry: MetaEntry.WrappingText, withDot: Boolean) {
    val density = LocalDensity.current
    // The gap before the dot and the dot itself; the flow's own gap follows it, as it follows any entry.
    val dotSlot = with(density) { (dimensionResource(R.dimen.hero_meta_gap) + dimensionResource(R.dimen.hero_meta_dot_size)).toSp() }
    val text = buildAnnotatedString {
        append(entry.text)
        if (withDot) {
            append(WORD_JOINER)
            appendInlineContent(META_DOT_ID)
        }
    }
    val inline = if (withDot) {
        mapOf(
            META_DOT_ID to InlineTextContent(
                Placeholder(width = dotSlot, height = 1.em, placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) { HeroMetaDot() }
            },
        )
    } else {
        emptyMap()
    }
    Text(
        text = text,
        inlineContent = inline,
        // The inline dot's alternate text must not reach a screen reader: it reads the words alone.
        modifier = if (withDot) Modifier.clearAndSetSemantics { this.text = AnnotatedString(entry.text) } else Modifier,
        style = MaterialTheme.typography.labelLarge,
        color = entry.color,
    )
}

/**
 * A row of [scores], each a glyph and its value, separated by the meta dot, for a screen's own meta line. It wraps
 * between scores when they do not fit, and a screen reader reads each as its [ExternalScore.contentDescription].
 * [color] is the values' ink.
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
