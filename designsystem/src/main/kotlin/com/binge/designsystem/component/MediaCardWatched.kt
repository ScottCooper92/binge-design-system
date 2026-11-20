package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeTheme

/**
 * The watched mark on a [MediaCard] poster: a small tick on the same scrim disc as the favourite heart, at the top-end corner. It has no click,
 * no ripple and no semantics of its own; the card announces "Watched" in its merged state description.
 */
@Composable
internal fun WatchedMark(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(dimensionResource(R.dimen.media_card_favourite_size))
            .background(BingeTheme.colors.scrim.copy(alpha = FAVOURITE_BACKDROP_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(dimensionResource(R.dimen.media_card_favourite_icon_size)),
        )
    }
}

/**
 * How many episodes or titles of a card are still unwatched, as a count badge at the top-end corner. The card announces it ("3 unwatched") in
 * its merged state description, so the badge says nothing of its own.
 */
@Composable
internal fun UnwatchedCountMark(
    count: Int,
    modifier: Modifier = Modifier,
) {
    CountBadge(
        count = count,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier,
    )
}
