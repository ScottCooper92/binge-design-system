package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme
import com.binge.designsystem.theme.contrastRatio

/** How opaque the scrim disc behind the heart is: enough that the red holds against a bright poster, still a disc and not a plate. */
internal const val FAVOURITE_BACKDROP_ALPHA = 0.8f

/** Contrast a mark over artwork holds against the worst poster, a graphic's 3:1. */
private const val FAVOURITE_MIN_CONTRAST = 3.0

/**
 * The tint of the favourite heart over artwork: the theme's favourite red, which is the red of the tappable favourite, unless that fails
 * [FAVOURITE_MIN_CONTRAST] against the scrim disc on the brightest poster, when it is `onScrim`. The disc is the same on every poster, so
 * this is a property of the theme and not of the artwork, and it is read once for each.
 */
@Composable
internal fun favouriteOverArtworkTint(): Color {
    val colors = BingeTheme.colors
    return favouriteTintFor(colors.favourite, colors.scrim, colors.onScrim)
}

/** The rule behind [favouriteOverArtworkTint], on the colours alone so it can be checked for each theme. */
internal fun favouriteTintFor(
    favourite: Color,
    scrim: Color,
    onScrim: Color,
): Color {
    val worstBackdrop = scrim.copy(alpha = FAVOURITE_BACKDROP_ALPHA).compositeOver(Color.White)
    return if (favourite.contrastRatio(worstBackdrop) >= FAVOURITE_MIN_CONTRAST) favourite else onScrim
}

/**
 * The favourite mark on a [MediaCard] poster: a small filled heart on a scrim disc, drawn at the top-end corner beside the status dot. It has
 * no click, no ripple and no semantics of its own; the card announces "Favourite" in its merged state description.
 */
@Composable
internal fun FavouriteMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.media_card_favourite_size))
            .background(BingeTheme.colors.scrim.copy(alpha = FAVOURITE_BACKDROP_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Favorite,
            contentDescription = null,
            tint = favouriteOverArtworkTint(),
            modifier = Modifier.size(dimensionResource(R.dimen.media_card_favourite_icon_size)),
        )
    }
}
