package com.binge.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.formatRating
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.labelLargeEmphasis

/**
 * Condensed rate-this-title card for the movie/TV detail screens. Shows an interactive
 * star picker when unrated, the user's score with an inline editor when rated, and — when
 * the title has community reviews — a footer that links through to the reviews screen.
 *
 * When [isSignedIn] is `false` the rate body is hidden entirely (TMDB requires a session
 * to submit ratings); the card collapses to just the reviews footer, or renders nothing
 * if there are also no reviews to link to.
 *
 * [rateable] is for an already-rateable-in-principle title TMDB will still refuse — most
 * commonly one that hasn't released yet. It only affects the unrated body: the stars render
 * non-interactive and the prompt swaps to [notYetRateableQuestion]/[notYetRateableHint]. A
 * caller decides rateability from its own data (a release date, an air date); this component
 * has no opinion on what makes a title unrateable, only on how to show it.
 */
@Composable
fun RatingCard(
    userRating: Float?,
    isTv: Boolean,
    isSignedIn: Boolean,
    reviewCount: Int,
    averageReviewRating: Float?,
    onRate: (Float) -> Unit,
    onRemoveRating: () -> Unit,
    onReviewsClick: () -> Unit,
    modifier: Modifier = Modifier,
    rateable: Boolean = true,
    // The "Rate this …" eyebrow; defaults to the movie/show wording from [isTv]. Callers on other
    // surfaces (e.g. an episode screen) can pass a more specific label.
    @StringRes promptLabel: Int = if (isTv) R.string.rating_card_prompt_label_tv else R.string.rating_card_prompt_label_movie,
    @StringRes notYetRateableQuestion: Int = R.string.rating_card_prompt_question_pending,
    @StringRes notYetRateableHint: Int = R.string.rating_card_prompt_hint_pending,
    // The reviews-footer subtitle, with and without a community average; both name the app by
    // default, so a caller whose brand isn't Binge must override them.
    @StringRes reviewsSubtitle: Int = R.string.rating_card_reviews_subtitle,
    @StringRes reviewsSubtitleNoAverage: Int = R.string.rating_card_reviews_subtitle_no_average,
) {
    val showFooter = reviewCount > 0
    if (!isSignedIn && !showFooter) return

    val rated = isSignedIn && userRating != null
    val shape = BingeShapes.MediaCard
    val containerColor =
        if (rated) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val contentColor =
        if (rated) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

    Column(
        modifier =
            modifier
                .padding(horizontal = resolvedContentInset())
                .clip(shape)
                .background(containerColor)
                .then(
                    if (rated) {
                        Modifier
                    } else {
                        Modifier.border(dimensionResource(R.dimen.hairline_thickness), MaterialTheme.colorScheme.outline, shape)
                    },
                ),
    ) {
        if (isSignedIn) {
            if (userRating != null) {
                RatedBody(
                    userRating = userRating,
                    contentColor = contentColor,
                    onRate = onRate,
                    onRemoveRating = onRemoveRating,
                )
            } else {
                UnratedBody(
                    promptLabel = promptLabel,
                    rateable = rateable,
                    notYetRateableQuestion = notYetRateableQuestion,
                    notYetRateableHint = notYetRateableHint,
                    onRate = onRate,
                )
            }
        }

        if (showFooter) {
            if (isSignedIn) {
                HorizontalDivider(color = contentColor.copy(alpha = 0.12f))
            }
            ReviewsFooter(
                reviewCount = reviewCount,
                averageReviewRating = averageReviewRating,
                subtitle = reviewsSubtitle,
                subtitleNoAverage = reviewsSubtitleNoAverage,
                contentColor = contentColor,
                onClick = onReviewsClick,
            )
        }
    }
}

@Composable
private fun UnratedBody(
    @StringRes promptLabel: Int,
    rateable: Boolean,
    @StringRes notYetRateableQuestion: Int,
    @StringRes notYetRateableHint: Int,
    onRate: (Float) -> Unit,
) {
    Row(
        modifier = Modifier.padding(dimensionResource(R.dimen.rating_card_padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.rating_card_content_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(promptLabel),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(if (rateable) R.string.rating_card_prompt_question else notYetRateableQuestion),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(if (rateable) R.string.rating_card_prompt_hint else notYetRateableHint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        StarRating(
            rating = 0f,
            starSize = dimensionResource(R.dimen.star_rating_size_interactive),
            interactive = rateable,
            onRatingChange = if (rateable) onRate else null,
        )
    }
}

@Composable
private fun RatedBody(
    userRating: Float,
    contentColor: Color,
    onRate: (Float) -> Unit,
    onRemoveRating: () -> Unit,
) {
    var editing by rememberSaveable { mutableStateOf(false) }

    Column(modifier = Modifier.padding(dimensionResource(R.dimen.rating_card_padding))) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.rating_card_content_spacing)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.rating_card_your_rating),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = contentColor.copy(alpha = 0.7f),
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.detail_meta_spacing)))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.rating_card_content_spacing)),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = userRating.formatRating(),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = contentColor,
                        )
                        Text(
                            text = stringResource(R.string.rating_card_score_suffix),
                            style = MaterialTheme.typography.titleSmall,
                            color = contentColor.copy(alpha = 0.6f),
                        )
                    }
                    StarRating(rating = userRating)
                }
            }
            Box(
                modifier =
                    Modifier
                        .size(dimensionResource(R.dimen.rating_card_edit_button))
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f))
                        .clickable { editing = !editing },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.rating_card_change),
                    tint = contentColor,
                    modifier = Modifier.size(dimensionResource(R.dimen.detail_meta_icon_size)),
                )
            }
        }

        if (editing) {
            Spacer(Modifier.height(dimensionResource(R.dimen.rating_card_content_spacing)))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_meta_spacing)),
            ) {
                Text(
                    text = stringResource(R.string.rating_card_change_title),
                    style = MaterialTheme.typography.labelMedium,
                    color = contentColor.copy(alpha = 0.7f),
                )
                StarRating(
                    rating = userRating,
                    starSize = dimensionResource(R.dimen.star_rating_size_interactive),
                    interactive = true,
                    onRatingChange = onRate,
                )
                TextButton(
                    onClick = {
                        onRemoveRating()
                        editing = false
                    },
                ) {
                    Text(stringResource(R.string.rating_card_remove))
                }
            }
        }
    }
}

@Composable
private fun ReviewsFooter(
    reviewCount: Int,
    averageReviewRating: Float?,
    @StringRes subtitle: Int,
    @StringRes subtitleNoAverage: Int,
    contentColor: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(dimensionResource(R.dimen.rating_card_footer_padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.rating_card_content_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(dimensionResource(R.dimen.rating_card_footer_icon))
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(dimensionResource(R.dimen.detail_meta_icon_size)),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pluralStringResource(R.plurals.rating_card_reviews_title, reviewCount, reviewCount),
                style = MaterialTheme.typography.labelLargeEmphasis,
                color = contentColor,
            )
            Text(
                text =
                    if (averageReviewRating != null) {
                        stringResource(subtitle, averageReviewRating.formatRating())
                    } else {
                        stringResource(subtitleNoAverage)
                    },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.66f),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingCardUnrated() {
    BingeExpressiveTheme {
        RatingCard(
            userRating = null,
            isTv = false,
            isSignedIn = true,
            reviewCount = 7,
            averageReviewRating = 7.5f,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingCardRated() {
    BingeExpressiveTheme {
        RatingCard(
            userRating = 9f,
            isTv = false,
            isSignedIn = true,
            reviewCount = 0,
            averageReviewRating = null,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingCardSignedOutWithReviews() {
    BingeExpressiveTheme {
        RatingCard(
            userRating = null,
            isTv = false,
            isSignedIn = false,
            reviewCount = 12,
            averageReviewRating = 7.2f,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingCardNotYetRateable() {
    BingeExpressiveTheme {
        RatingCard(
            userRating = null,
            isTv = false,
            isSignedIn = true,
            rateable = false,
            reviewCount = 0,
            averageReviewRating = null,
            onRate = {},
            onRemoveRating = {},
            onReviewsClick = {},
        )
    }
}
