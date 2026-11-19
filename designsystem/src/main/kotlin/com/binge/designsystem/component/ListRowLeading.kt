package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/**
 * The [ListRow] leading visual for a person: their photo cropped into a circle [width] across, centred in a box as
 * tall as a [ListRowPoster] of the same width. A person row is then as tall as a title row beside it in a list or
 * grid. A tonal surface shows behind the photo, and an [ImagePlaceholder] stands in while it loads and when it fails.
 * The photo is decorative: the row's own text names the person.
 *
 * @need lead a list row with a person's photo
 * @group rows
 * @replaces a hand-built round image in a poster-sized box beside row text
 * @sample ListRowAvatarSample
 */
@Composable
fun ListRowAvatar(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(R.dimen.list_row_poster_width),
) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(CARD_ASPECT_RATIO),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width)
                .clip(BingeShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
                error = { ImagePlaceholder(Modifier.fillMaxSize()) },
            )
        }
    }
}

/**
 * The [ListRow] leading visual for a company: its logo fitted, never cropped, inside a rounded tile as wide and as
 * tall as a [ListRowPoster] of the same [width]. Logos are wide, transparent marks, so the tile pads the image and
 * sits it on a tonal surface. An [ImagePlaceholder] fills the tile while the logo loads and when it fails. The logo
 * is decorative: the row's own text names the company.
 *
 * @need lead a list row with a company's logo
 * @group rows
 * @replaces a hand-built padded logo tile beside row text
 * @sample ListRowLogoSample
 */
@Composable
fun ListRowLogo(
    logoUrl: String?,
    modifier: Modifier = Modifier,
    width: Dp = dimensionResource(R.dimen.list_row_poster_width),
) {
    Box(
        modifier = modifier
            .width(width)
            .aspectRatio(CARD_ASPECT_RATIO)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        SubcomposeAsyncImage(
            model = logoUrl,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            loading = { ImagePlaceholder(Modifier.fillMaxSize()) },
            error = { ImagePlaceholder(Modifier.fillMaxSize()) },
            success = {
                SubcomposeAsyncImageContent(
                    modifier = Modifier.padding(dimensionResource(R.dimen.list_row_logo_padding)),
                    contentScale = ContentScale.Fit,
                )
            },
        )
    }
}
