package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.component.SeeAllTile
import com.binge.designsystem.navOverlayEnd
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * A titled horizontal row of media items, with an optional trailing "see all" tile.
 *
 * [itemContent] receives each item's position in [items] as well as the item, so a caller reporting
 * which position was tapped reads it from the slot rather than searching the list for it.
 */
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
    endPadding: Dp = horizontalPadding + navOverlayEnd(),
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(
            title = title,
            onMoreClick = onMoreClick,
            horizontalPadding = horizontalPadding,
            startPadding = startPadding,
            endPadding = endPadding,
        )
        LazyRow(
            contentPadding = PaddingValues(start = startPadding, end = endPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
            itemsIndexed(items, key = { _, item -> itemKey(item) }) { index, item -> itemContent(index, item) }
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
        ) { _, title ->
            MediaCard(posterUrl = null, title = title, rating = 8.5f, onClick = {})
        }
    }
}
