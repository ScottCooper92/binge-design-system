package com.binge.designsystem.tv.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val TILE_ASPECT_RATIO = 2f / 3f

/**
 * The tile that closes a row: press it to open the whole set behind the row.
 *
 * Poster-shaped, not a button: at the tail of a poster run a differently sized element would break the row's
 * rhythm. A fill rather than an accent, so it reads as a place to go, not as another title. The caller's
 * [modifier] carries the row's width and entry requester.
 */
@Composable
fun TvSeeAllTile(
    label: String,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = TILE_ASPECT_RATIO,
) {
    Column(
        modifier =
            modifier
                .aspectRatio(aspectRatio)
                // Ring before clip: it draws outside the bounds, so an earlier clip cuts it off.
                .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                .clip(BingeShapes.MediaCard)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .tvClickable(onFocusChanged = onFocusChanged, onClick = onClick)
                .semantics { contentDescription = label }
                .padding(dimensionResource(DesR.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s), Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(dimensionResource(TvR.dimen.tv_see_all_icon)),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
    }
}
