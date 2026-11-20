package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** What a [TvPosterCell] says about the title beyond its art; draws nothing for a cell with no marks. */
@Composable
internal fun TvPosterMarks(poster: TvPosterCell) {
    val hasMarks = poster.watched || poster.unwatchedCount > 0 || poster.favourite || poster.progress != null
    if (!hasMarks) return
    val inset = dimensionResource(DesR.dimen.padding_xs)
    Box(Modifier.fillMaxSize()) {
        if (poster.watched) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = stringResource(TvR.string.tv_poster_watched),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(inset)
                    .size(dimensionResource(TvR.dimen.tv_poster_mark_size)),
            )
        } else if (poster.unwatchedCount > 0) {
            val label = stringResource(TvR.string.tv_poster_unwatched_count, poster.unwatchedCount)
            Text(
                text = poster.unwatchedCount.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(inset)
                    .clip(BingeShapes.MediaCard)
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = dimensionResource(DesR.dimen.padding_s), vertical = dimensionResource(DesR.dimen.padding_xxs))
                    .semantics { contentDescription = label },
            )
        }
        if (poster.favourite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(TvR.string.tv_poster_favourite),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(inset)
                    .size(dimensionResource(TvR.dimen.tv_poster_mark_size)),
            )
        }
        poster.progress?.let { fraction ->
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(dimensionResource(TvR.dimen.tv_poster_progress_height))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(fraction.coerceIn(0f, 1f))
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}
