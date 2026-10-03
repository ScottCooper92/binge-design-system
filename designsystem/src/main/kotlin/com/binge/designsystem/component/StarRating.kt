package com.binge.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.formatRating
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeTheme
import kotlin.math.ceil

private const val MAX_STARS = 5

/**
 * A five-star rating on a 0–10 scale. [rating] is shown to the nearest half star, which is
 * `rating / 2` rounded to the nearest 0.5, so 7.4 is three and a half stars and 7.6 is four. A value
 * exactly between two half stars rounds **down**: 7.5 reads as three and a half stars, agreeing with
 * the "7.5" a card prints beside it rather than the four stars a round-half-up would draw. See
 * [ratingToHalves] for the edge cases.
 *
 * When [interactive], tapping star N sets N full stars (a second tap on a full star toggles it to a
 * half), and long-pressing star N sets N − ½. New values report via [onRatingChange] on 1–10.
 *
 * Each interactive star is a button naming the value it sets, with its half a long-press away, so every
 * value on the scale is reachable without the gesture — the whole rating control published a label and
 * no action at all until this, leaving a primary action of the app impossible with a screen reader.
 */
@Composable
fun StarRating(
    rating: Float,
    modifier: Modifier = Modifier,
    starSize: Dp = dimensionResource(R.dimen.star_rating_size_display),
    interactive: Boolean = false,
    onRatingChange: ((Float) -> Unit)? = null,
) {
    val halves = ratingToHalves(rating)
    val ratingDescription = stringResource(R.string.cd_rating_out_of_ten, announcedRating(rating).formatRating())

    Row(
        modifier = modifier.semantics { contentDescription = ratingDescription },
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.star_rating_spacing)),
    ) {
        for (index in 0 until MAX_STARS) {
            val starHalves = (halves - index * 2).coerceIn(0, 2)
            val fullValue = (index + 1) * 2
            val halfValue = fullValue - 1
            // A tap on an already-full star drops it to its half, so the label has to name the value
            // this tap would actually set rather than the star's own.
            val tapValue = if (halves == fullValue) halfValue else fullValue
            val handler = onRatingChange.takeIf { interactive }
            Star(
                starHalves = starHalves,
                starSize = starSize,
                onTap = handler?.let { { it(tapValue.toFloat()) } },
                tapLabel = handler?.let { stringResource(R.string.cd_rate_out_of_ten, tapValue.toFloat().formatRating()) },
                onLongPress = handler?.let { { it(halfValue.toFloat()) } },
                longPressLabel =
                    handler?.let { stringResource(R.string.cd_rate_out_of_ten, halfValue.toFloat().formatRating()) },
            )
        }
    }
}

/**
 * The number of half stars (0–10) that [rating] on the 0–10 scale draws: `rating / 2` rounded to the
 * nearest 0.5 star, with ties rounding down.
 *
 * Every tie falls on an x.5 rating (7.5 sits exactly between 3.5 and 4 stars), so ties go down to make
 * the star row never read higher than the number beside it. Out-of-range values clamp to 0 and 10, and
 * NaN draws no stars.
 */
internal fun ratingToHalves(rating: Float): Int {
    if (rating.isNaN()) return 0
    return ceil(rating.coerceIn(0f, MAX_RATING) - HALF_POINT).toInt().coerceAtLeast(0)
}

/** The rating a screen reader announces: the true value, clamped to the scale, not the drawn half star. */
internal fun announcedRating(rating: Float): Float = if (rating.isNaN()) 0f else rating.coerceIn(0f, MAX_RATING)

private const val MAX_RATING = 10f
private const val HALF_POINT = 0.5f

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Star(
    starHalves: Int,
    starSize: Dp,
    onTap: (() -> Unit)?,
    tapLabel: String?,
    onLongPress: (() -> Unit)?,
    longPressLabel: String?,
) {
    val icon =
        when (starHalves) {
            2 -> Icons.Filled.Star
            1 -> Icons.AutoMirrored.Filled.StarHalf
            else -> Icons.Filled.StarBorder
        }
    val tint = if (starHalves > 0) BingeTheme.colors.ratingStar else MaterialTheme.colorScheme.outline

    Icon(
        imageVector = icon,
        // The label is the star's name as well as its action: a Role.Button with nothing to say is
        // announced as an unnamed button, which is the half of 4.1.2 an onClickLabel does not cover.
        contentDescription = tapLabel,
        tint = tint,
        modifier =
            Modifier
                .size(starSize)
                .then(
                    if (onTap != null) {
                        // `detectTapGestures` published nothing: no name, no role, no action, no focus
                        // target, and none of `clickable`'s touch-target expansion on a 28dp star.
                        Modifier.combinedClickable(
                            role = Role.Button,
                            onLongClickLabel = longPressLabel,
                            onLongClick = onLongPress,
                            onClick = onTap,
                        )
                    } else {
                        Modifier
                    },
                ),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewStarRating() {
    BingeExpressiveTheme {
        StarRating(rating = 7f)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewStarRatingInteractive() {
    BingeExpressiveTheme {
        StarRating(
            rating = 0f,
            starSize = dimensionResource(R.dimen.star_rating_size_interactive),
            interactive = true,
            onRatingChange = {},
        )
    }
}
