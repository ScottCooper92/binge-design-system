package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
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
 * The trailing tile is [endTileHeight] tall, which defaults to a poster's height ([R.dimen.card_height]): the tile lines up with the posters,
 * not with the captions under them, as it does in Binge. That holds at every font scale, because a poster is an image of fixed size and only its
 * caption grows with the font, so a [MediaCard] row at a large scale has a tile shorter than its cards by design. A row of shorter items, such
 * as the category tiles ([categoryTileHeight]), passes their height so the tile does not stand taller than the row.
 *
 * A row that wants the tile to span its whole items, or whose items change height with the font scale, such as [PersonCard]s, passes
 * [Dp.Unspecified]: the tile is then as tall as the tallest item the row has composed now. Each item reports its own height, so the tile follows the items down as well as up, and never sets the
 * row's height itself. The row is measured once before the tile has a height, so the tile fills in a frame after the items.
 *
 * [itemContent] receives each item's position in [items] as well as the item, so a caller reporting
 * which position was tapped reads it from the slot rather than searching the list for it.
 *
 * @need show a titled row of posters with a see-all tile
 * @group media
 * @replaces a hand-built LazyRow with a section title
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
    endTileHeight: Dp = dimensionResource(R.dimen.card_height),
    horizontalPadding: Dp = dimensionResource(R.dimen.padding_m),
    // The row is full-bleed under an overlaying rail; its first column clears it instead (navOverlayStart).
    // A caller whose own parent already applied the overlay inset (e.g. a grid's navOverlayPadding)
    // passes its own value to avoid adding it twice.
    startPadding: Dp = horizontalPadding + navOverlayStart(),
    endPadding: Dp = horizontalPadding + navOverlayEnd(),
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    val matchRow = endTileHeight == Dp.Unspecified
    // Each composed item's own height, in px, by key, when the tile follows the row. The tile is left out of it: it is sized from the items, so it
    // cannot hold the row open at a height the items have since shrunk below.
    val itemHeights = remember { mutableStateMapOf<Any, Int>() }
    val tallest by remember { derivedStateOf { itemHeights.values.maxOrNull() ?: 0 } }
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
            itemsIndexed(items, key = { _, item -> itemKey(item) }) { index, item ->
                if (matchRow) {
                    val key = itemKey(item)
                    Box(Modifier.onSizeChanged { itemHeights[key] = it.height }) { itemContent(index, item) }
                    DisposableEffect(key) { onDispose { itemHeights.remove(key) } }
                } else {
                    itemContent(index, item)
                }
            }
            if (onEndTileClick != null) {
                item(key = "media_carousel_end_tile") {
                    SeeAllTile(
                        label = endTileLabel,
                        onClick = onEndTileClick,
                        modifier = Modifier
                            .width(dimensionResource(R.dimen.see_all_tile_width))
                            .height(if (matchRow) with(LocalDensity.current) { tallest.toDp() } else endTileHeight),
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
