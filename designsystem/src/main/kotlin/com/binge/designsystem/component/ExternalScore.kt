package com.binge.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
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

    /** An IMDb-style user rating: a small text badge. */
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
private val ImdbYellow = Color(0xFFF5C518)
private val ImdbInk = Color(0xFF000000)

/**
 * A score's glyph and value as one item, read by a screen reader as [ExternalScore.contentDescription].
 * The glyphs are our own drawings and look the same in light and dark. [color] is the value's ink.
 */
@Composable
internal fun ExternalScoreItem(
    score: ExternalScore,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = score.contentDescription },
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.external_score_gap)),
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
            fontWeight = FontWeight.ExtraBold,
            color = ImdbInk,
            maxLines = 1,
            modifier = Modifier
                .clip(BingeShapes.ElementExtraSmall)
                .background(ImdbYellow)
                .padding(
                    horizontal = dimensionResource(R.dimen.external_score_badge_padding_h),
                    vertical = dimensionResource(R.dimen.external_score_badge_padding_v),
                ),
        )
    }
}

/**
 * A meta line: [entries] side by side, each followed by the meta dot except the last, so a line that wraps at a large
 * font ends on a dot rather than starting on one.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MetaFlow(entries: List<@Composable () -> Unit>, modifier: Modifier = Modifier) {
    val gap = dimensionResource(R.dimen.hero_meta_gap)
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(gap),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.meta_flow_line_gap)),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        entries.forEachIndexed { index, entry ->
            Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                entry()
                if (index < entries.lastIndex) HeroMetaDot()
            }
        }
    }
}

/**
 * A row of [scores], each a glyph and its value, separated by the meta dot, for a screen's own meta line. It wraps
 * between scores when they do not fit, and a screen reader reads each as its [ExternalScore.contentDescription].
 * [color] is the values' ink.
 *
 * @need Show review scores from other sources (critics, audience, IMDb) next to a title, each with its icon.
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
    MetaFlow(entries = scores.map { score -> { ExternalScoreItem(score, color) } }, modifier = modifier)
}

/**
 * The genre line of a detail header with [scores] after it: the genres in the accent, then each score, separated by
 * the meta dot. Stays on one line when it fits and wraps between items when it does not.
 */
@Composable
internal fun GenreScoreLine(
    genres: String?,
    scores: List<ExternalScore>,
    scoreColor: Color,
    modifier: Modifier = Modifier,
) {
    val entries = buildList<@Composable () -> Unit> {
        if (!genres.isNullOrBlank()) {
            add {
                Text(
                    text = genres,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        scores.forEach { score -> add { ExternalScoreItem(score, scoreColor) } }
    }
    MetaFlow(entries = entries, modifier = modifier)
}
