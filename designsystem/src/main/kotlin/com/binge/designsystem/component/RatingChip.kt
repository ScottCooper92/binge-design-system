package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.formatRating
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.BingeTheme

enum class RatingChipSize {
    Sm,
    Md,
    Lg,
}

enum class RatingChipTone {
    Glass,
    Surface,
    Accent,
}

@Composable
fun RatingChip(
    rating: Float,
    modifier: Modifier = Modifier,
    size: RatingChipSize = RatingChipSize.Sm,
    tone: RatingChipTone = RatingChipTone.Glass,
) {
    val paddingH = when (size) {
        RatingChipSize.Sm -> dimensionResource(R.dimen.rating_chip_padding_h)
        RatingChipSize.Md -> dimensionResource(R.dimen.rating_chip_padding_h_md)
        RatingChipSize.Lg -> dimensionResource(R.dimen.rating_chip_padding_h_lg)
    }
    val paddingV = when (size) {
        RatingChipSize.Sm -> dimensionResource(R.dimen.rating_chip_padding_v)
        RatingChipSize.Md -> dimensionResource(R.dimen.rating_chip_padding_v_md)
        RatingChipSize.Lg -> dimensionResource(R.dimen.rating_chip_padding_v_lg)
    }
    val starSize = when (size) {
        RatingChipSize.Sm -> dimensionResource(R.dimen.rating_chip_star_size)
        RatingChipSize.Md -> dimensionResource(R.dimen.rating_chip_star_size_md)
        RatingChipSize.Lg -> dimensionResource(R.dimen.rating_chip_star_size_lg)
    }
    val textStyle = when (size) {
        RatingChipSize.Sm -> MaterialTheme.typography.labelSmall
        RatingChipSize.Md -> MaterialTheme.typography.labelMedium
        RatingChipSize.Lg -> MaterialTheme.typography.labelLarge
    }
    val background = when (tone) {
        RatingChipTone.Glass -> BingeTheme.colors.scrim.copy(alpha = 0.65f)
        RatingChipTone.Surface -> MaterialTheme.colorScheme.surfaceContainer
        RatingChipTone.Accent -> MaterialTheme.colorScheme.secondaryContainer
    }
    val foreground = when (tone) {
        RatingChipTone.Glass -> BingeTheme.colors.onScrim
        RatingChipTone.Surface -> MaterialTheme.colorScheme.onSurface
        RatingChipTone.Accent -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Row(
        modifier = modifier
            .clip(BingeShapes.Pill)
            .background(background)
            .padding(horizontal = paddingH, vertical = paddingV),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = BingeTheme.colors.ratingStar,
            modifier = Modifier.size(starSize),
        )
        Spacer(Modifier.width(dimensionResource(R.dimen.rating_chip_star_spacing)))
        Text(
            text = rating.formatRating(),
            style = textStyle,
            color = foreground,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingChip() {
    BingeExpressiveTheme {
        RatingChip(rating = 8.5f)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingChipLarge() {
    BingeExpressiveTheme {
        RatingChip(rating = 9.0f, size = RatingChipSize.Lg)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingChipMedium() {
    BingeExpressiveTheme {
        RatingChip(rating = 7.4f, size = RatingChipSize.Md)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingChipSurfaceTone() {
    BingeExpressiveTheme {
        RatingChip(rating = 8.5f, tone = RatingChipTone.Surface)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewRatingChipAccentTone() {
    BingeExpressiveTheme {
        RatingChip(rating = 8.5f, tone = RatingChipTone.Accent)
    }
}
