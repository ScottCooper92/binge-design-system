package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.component.SeeAllTile
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.theme.BingeExpressiveTheme

@Composable
fun <T> MediaCarousel(
    title: String,
    items: List<T>,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
    onMoreClick: (() -> Unit)? = null,
    onEndTileClick: (() -> Unit)? = onMoreClick,
    endTileLabel: String = stringResource(R.string.gallery_see_all),
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_m),
    // The row is full-bleed under an overlaying rail; its first column clears it instead (navOverlayStart).
    // A caller whose own parent already applied the overlay inset (e.g. a grid's navOverlayPadding)
    // passes its own value to avoid adding it twice.
    startPadding: Dp = horizontalPadding + navOverlayStart(),
    itemContent: @Composable (T) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = title, onMoreClick = onMoreClick, horizontalPadding = horizontalPadding, startPadding = startPadding)
        LazyRow(
            contentPadding = PaddingValues(start = startPadding, end = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
            items(items, key = itemKey) { item -> itemContent(item) }
            if (onEndTileClick != null) {
                item(key = "media_carousel_end_tile") {
                    SeeAllTile(
                        label = endTileLabel,
                        onClick = onEndTileClick,
                        modifier = Modifier
                            .width(dimensionResource(R.dimen.see_all_tile_width))
                            .height(dimensionResource(R.dimen.card_height)),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewMediaCarousel() {
    BingeExpressiveTheme {
        MediaCarousel(
            title = "Trending movies",
            items = listOf("The Dark Knight", "Inception", "Interstellar", "Tenet"),
            itemKey = { it },
            onMoreClick = {},
        ) { title ->
            MediaCard(posterUrl = null, title = title, rating = 8.5f, onClick = {})
        }
    }
}
