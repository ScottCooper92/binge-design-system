package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.tv.R as TvR

/**
 * The score, as a filled amber pill with a star: the one way every 10-foot surface writes a rating.
 *
 * @need show a rating as the filled amber pill every tv surface uses
 * @group tv
 * @sample TvRatingBadgeSample
 */
@Composable
fun TvRatingBadge(
    rating: Float?,
    modifier: Modifier = Modifier,
    size: TvRatingBadgeSize = TvRatingBadgeSize.Meta,
    emphasis: TvRowEmphasis = TvRowEmphasis.Resting,
) {
    val value = tvRatingValue(rating) ?: return
    // Crossed on purpose: on an amber row the badge swaps to that row's ink for its fill, so the pair stays
    // the row's own two colours whichever way round.
    val container = emphasis.contentColor(resting = BingeTheme.colors.ratingStar)
    val content = emphasis.containerColor(resting = BingeTheme.colors.onRatingStar)
    Row(
        modifier = modifier
            // Chip, not the phone RatingChip's Pill: every TV button is a Pill, so a stadium-shaped amber
            // badge is the one silhouette mistakable for a focused control.
            .clip(BingeShapes.Chip)
            .background(container)
            .padding(
                horizontal = badgePaddingHorizontal(size),
                vertical = badgePaddingVertical(size),
            ),
        horizontalArrangement = Arrangement.spacedBy(badgeGap(size)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            // The number beside it says what this is; announcing "star" before every score would triple a
            // row's announcement for no added meaning.
            contentDescription = null,
            tint = content,
            modifier = Modifier.size(badgeIcon(size)),
        )
        Text(
            text = value,
            style = badgeTextStyle(size),
            color = content,
            maxLines = 1,
        )
    }
}

/**
 * A metadata line that opens with the rating badge: `[★ 8.6]  2026 · 2h 14m · Action`. The one presentation
 * step shared by every surface that writes such a line, so the badge cannot drift a size or gap apart; the lines
 * themselves stay plain testable strings from `tvMediaMetaLine` and no longer carry the rating.
 *
 * Renders [text] alone when there is no rating, the badge alone when there is no text, nothing when it has
 * neither — so a caller can place it in a `spacedBy` column without guarding it first.
 *
 * @need lead a tv meta line with its rating badge
 * @group tv
 * @sample TvMetaLineSample
 */
@Composable
fun TvMetaLine(
    rating: Float?,
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    badgeSize: TvRatingBadgeSize = TvRatingBadgeSize.Meta,
    emphasis: TvRowEmphasis = TvRowEmphasis.Resting,
) {
    if (tvRatingValue(rating) == null && text.isBlank()) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_meta_line_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TvRatingBadge(rating = rating, size = badgeSize, emphasis = emphasis)
        if (text.isNotBlank()) {
            Text(
                text = text,
                style = style,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** How large a [TvRatingBadge] draws, named for the two places it goes rather than by t-shirt size. */
enum class TvRatingBadgeSize {
    /** In a metadata line, beside `titleMedium` copy — heroes, the detail band, the episode panes. */
    Meta,

    /** In a poster's corner, where the Meta size would cover a fifth of a 120dp cell's artwork. */
    Card,
}

@Composable
@ReadOnlyComposable
private fun badgePaddingHorizontal(size: TvRatingBadgeSize): Dp =
    when (size) {
        TvRatingBadgeSize.Meta -> dimensionResource(TvR.dimen.tv_rating_badge_meta_padding_horizontal)
        TvRatingBadgeSize.Card -> dimensionResource(TvR.dimen.tv_rating_badge_card_padding_horizontal)
    }

@Composable
@ReadOnlyComposable
private fun badgePaddingVertical(size: TvRatingBadgeSize): Dp =
    when (size) {
        TvRatingBadgeSize.Meta -> dimensionResource(TvR.dimen.tv_rating_badge_meta_padding_vertical)
        TvRatingBadgeSize.Card -> dimensionResource(TvR.dimen.tv_rating_badge_card_padding_vertical)
    }

@Composable
@ReadOnlyComposable
private fun badgeGap(size: TvRatingBadgeSize): Dp =
    when (size) {
        TvRatingBadgeSize.Meta -> dimensionResource(TvR.dimen.tv_rating_badge_meta_gap)
        TvRatingBadgeSize.Card -> dimensionResource(TvR.dimen.tv_rating_badge_card_gap)
    }

@Composable
@ReadOnlyComposable
private fun badgeIcon(size: TvRatingBadgeSize): Dp =
    when (size) {
        TvRatingBadgeSize.Meta -> dimensionResource(TvR.dimen.tv_rating_badge_meta_icon)
        TvRatingBadgeSize.Card -> dimensionResource(TvR.dimen.tv_rating_badge_card_icon)
    }

@Composable
@ReadOnlyComposable
private fun badgeTextStyle(size: TvRatingBadgeSize): TextStyle =
    when (size) {
        TvRatingBadgeSize.Meta -> MaterialTheme.typography.titleMedium
        TvRatingBadgeSize.Card -> MaterialTheme.typography.labelMedium
    }
