package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import coil3.compose.AsyncImage
import com.binge.designsystem.tv.focus.rememberTvRowEntry
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.R as TvR

private const val WIDE = 16f / 9f
private const val TALL = 2f / 3f
private const val ROWS = 2
private const val LANDSCAPE_PER_PORTRAIT = 4

/**
 * One image in the gallery grid: where it loads from, whether it is portrait, and its [index] in the title's whole gallery.
 * [description] is what a screen reader says for the image; left null, the grid says "Image 3 of 12".
 */
class TvDetailGalleryImage(
    val url: String,
    val portrait: Boolean,
    val index: Int,
    val description: String? = null,
)

/**
 * Orders [this] for the grid with portrait images mixed among the landscape ones: [LANDSCAPE_PER_PORTRAIT] landscape (whole
 * columns, so an even count) then one portrait, repeating, with whatever is left of either type at the end. Keeping each portrait
 * at a column boundary is what stops it landing beside a single landscape image and leaving a hole.
 */
fun List<TvDetailGalleryImage>.mixedForGrid(): List<TvDetailGalleryImage> {
    val landscape = filterNot { it.portrait }.toMutableList()
    val portrait = filter { it.portrait }.toMutableList()
    return buildList {
        while (landscape.size >= LANDSCAPE_PER_PORTRAIT && portrait.isNotEmpty()) {
            repeat(LANDSCAPE_PER_PORTRAIT) { add(landscape.removeAt(0)) }
            add(portrait.removeAt(0))
        }
        // An odd landscape image would leave the next portrait half a column out, so the portraits go first.
        addAll(portrait)
        addAll(landscape)
    }
}

/**
 * The Gallery tab as a horizontal strip two rows high. Landscape images take one row each, so they pair up; a portrait one spans
 * both rows as a single tall card. Focus is a ring and nothing else; OK calls [onOpen] with the image's index in the title's whole
 * gallery. Entry lands on the first image, then on the last one focused. [initialFocusedIndex] seeds a frame. [totalCount] is the
 * size of the whole gallery, for the default "Image 3 of 12" label of an image with no description of its own.
 *
 * @need show a title's images as a two-row strip with tall images spanning both rows
 * @group tv
 * @replaces a hand-built horizontal image grid with focus entry
 */
@Composable
fun TvDetailGalleryGrid(
    images: List<TvDetailGalleryImage>,
    onOpen: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
    initialFocusedIndex: Int? = null,
    totalCount: Int = images.size,
) {
    val rowHeight = dimensionResource(TvR.dimen.tv_detail_gallery_row_height)
    val gap = dimensionResource(TvR.dimen.tv_media_row_card_gap)
    var focusedIndex by remember { mutableStateOf(initialFocusedIndex) }
    val entry = rememberTvRowEntry(images.size)
    // Centred in the space under the tabs, like the carousels.
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(ROWS),
            modifier = Modifier.height(rowHeight * ROWS + gap).tvEntryFocusGroup(entry.entryFocus),
            contentPadding = PaddingValues(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap),
        ) {
            itemsIndexed(
                images,
                key = { _, image -> image.index },
                span = { _, image -> GridItemSpan(if (image.portrait) ROWS else 1) },
            ) { position, image ->
                val height = if (image.portrait) rowHeight * ROWS + gap else rowHeight
                TvGalleryGridCell(
                    image = image,
                    label = image.description ?: stringResource(TvR.string.tv_detail_gallery_image, image.index + 1, totalCount),
                    width = height * (if (image.portrait) TALL else WIDE),
                    height = height,
                    isFocused = focusedIndex == image.index,
                    onFocusChanged = { focused ->
                        if (focused) {
                            focusedIndex = image.index
                            entry.rememberFocused(position)
                        } else if (focusedIndex == image.index) {
                            focusedIndex = null
                        }
                    },
                    modifier = entry.entryModifier(position),
                    onClick = { onOpen(image.index) },
                )
            }
        }
    }
}

@Composable
private fun TvGalleryGridCell(
    image: TvDetailGalleryImage,
    label: String,
    width: Dp,
    height: Dp,
    isFocused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = MaterialTheme.shapes.medium
    Box(
        modifier = modifier
            .size(width, height)
            .clip(shape)
            .semantics { contentDescription = label }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                dimensionResource(TvR.dimen.tv_focus_ring_width),
                if (isFocused) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                shape,
            ).tvClickable(role = Role.Button, onFocusChanged = onFocusChanged, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = image.url,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().clip(shape),
        )
    }
}
