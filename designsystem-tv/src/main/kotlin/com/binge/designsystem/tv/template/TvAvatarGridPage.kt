package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvCardGrid
import com.binge.designsystem.tv.component.TvCastCard
import com.binge.designsystem.tv.component.TvErrorPlate
import com.binge.designsystem.tv.component.TvMediaGridSkeleton
import com.binge.designsystem.tv.focus.rememberTvRowEntry
import com.binge.designsystem.tv.R as TvR

/** One face in a [TvAvatarGridPage]: [key] names it stably, and [role] is the caption line under the [name]. */
data class TvAvatarGridItem<out K : Any>(
    val key: K,
    val name: String,
    val role: String?,
    val imageUrl: String?,
)

/** What a [TvAvatarGridPage] shows. A caller translates its source into one of these at the screen's edge. */
sealed interface TvAvatarGridState<out K : Any> {
    data object Loading : TvAvatarGridState<Nothing>

    /** A failure: the [kind]'s glyph and copy, [title] and [message] overriding them. A null [onRetry] drops the button. */
    data class Error(
        val kind: ErrorKind,
        val onRetry: (() -> Unit)? = null,
        val title: String? = null,
        val message: String? = null,
    ) : TvAvatarGridState<Nothing>

    /** Nothing to show, said in the caller's own words. */
    data class Empty(
        val message: String,
    ) : TvAvatarGridState<Nothing>

    data class Ready<out K : Any>(
        val items: List<TvAvatarGridItem<K>>,
    ) : TvAvatarGridState<K>
}

/**
 * A heading over a grid of circular faces, each with a name and a role beneath: a cast list or a people list, with no backdrop to
 * paint. A lean page rather than [TvGridPage], which is built around poster cells that drive an immersive backdrop, but with the
 * same loading, failure and empty vocabulary. Entry routes to the cell last focused (the first on a fresh grid).
 *
 * [gridEntryFocusRequester] is the screen's arrival request: an overlay entry has no rail to enter the grid from, so the screen
 * aims a requester at the grid's entry group. [initialFocusedKey] seeds a focused cell for a frame, and [errorRetryFocused] the
 * failure's one focus stop.
 *
 * @need show a heading over a grid of circular faces with names and roles on a TV screen
 * @group tv
 * @replaces a hand-built tv cast or people grid
 */
@Composable
fun <K : Any> TvAvatarGridPage(
    heading: String,
    state: TvAvatarGridState<K>,
    onItemClick: (K) -> Unit,
    modifier: Modifier = Modifier,
    initialFocusedKey: K? = null,
    gridEntryFocusRequester: FocusRequester? = null,
    errorRetryFocused: Boolean = false,
    onItemTapped: (index: Int) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // With no app bar the heading is the only thing naming what the grid shows.
        Text(
            text = heading,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                start = dimensionResource(TvR.dimen.tv_content_gutter_start),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                top = dimensionResource(TvR.dimen.tv_overscan_vertical),
            ),
        )
        when (state) {
            TvAvatarGridState.Loading -> AvatarGridSkeleton()
            is TvAvatarGridState.Error -> TvErrorPlate(
                kind = state.kind,
                onRetry = state.onRetry,
                title = state.title,
                message = state.message,
                alignment = Alignment.Center,
                retryInitiallyFocused = errorRetryFocused,
            )
            is TvAvatarGridState.Empty -> AvatarGridMessage(state.message)
            is TvAvatarGridState.Ready ->
                if (state.items.isNotEmpty()) {
                    AvatarGrid(state.items, onItemClick, initialFocusedKey, gridEntryFocusRequester, onItemTapped)
                }
        }
    }
}

@Composable
private fun <K : Any> AvatarGrid(
    items: List<TvAvatarGridItem<K>>,
    onItemClick: (K) -> Unit,
    initialFocusedKey: K?,
    entryFocusRequester: FocusRequester?,
    onItemTapped: (index: Int) -> Unit,
) {
    // The shared remembered-cell contract: entry routes to the cell last focused (the first on a fresh grid) instead of a
    // geometric pick. The grid tracks the ring; this outlives it, since the ring clears on the blur that starts the round trip.
    val entry = rememberTvRowEntry(items.size)
    TvCardGrid(
        itemCount = items.size,
        itemKey = { items[it].key },
        columns = AVATAR_GRID_COLUMNS,
        state = rememberLazyGridState(initialFirstVisibleItemIndex = entry.entryIndex),
        // Seeded so a frame can render a focused cell inside the grid, the only way to prove the content padding protects the ring.
        initialFocusedIndex = initialFocusedKey?.let { key -> items.indexOfFirst { it.key == key } }?.takeIf { it >= 0 },
        contentPadding = avatarGridPadding(),
        onItemFocused = { index -> entry.rememberFocused(index) },
        entryFocusRequester = entryFocusRequester,
        rememberedIndex = entry.entryIndex,
    ) { index, isFocused, onFocusChanged, cellModifier ->
        val item = items[index]
        TvCastCard(
            name = item.name,
            role = item.role?.takeIf { it.isNotBlank() },
            profileUrl = item.imageUrl,
            isFocused = isFocused,
            onFocusChanged = onFocusChanged,
            onClick = {
                onItemTapped(index)
                onItemClick(item.key)
            },
            // Fills the cell, so the column count decides the circle size and the gutters stay even.
            modifier = cellModifier,
        )
    }
}

/**
 * A circle plate, and beneath it the caption block the cast card draws: the same gap, then a line in each of the card's two text
 * styles. The plates take their height from those lines rather than from a number of their own, so a row of plates and the row of
 * cards that replaces it have the same height (a bare circle left every row after the first too high, and fixed plates too low).
 */
@Composable
private fun AvatarGridSkeleton() {
    TvMediaGridSkeleton(columns = AVATAR_GRID_COLUMNS, contentPadding = avatarGridPadding()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            SkeletonCaptionLine(
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = dimensionResource(TvR.dimen.tv_card_caption_gap)),
            )
            SkeletonCaptionLine(style = MaterialTheme.typography.labelMedium)
        }
    }
}

/** One caption line's slot: an invisible line of [style] fixes the height, and a plate sits inside it, inset so two lines do not run together. */
@Composable
private fun SkeletonCaptionLine(style: TextStyle, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth(SKELETON_CAPTION_WIDTH_FRACTION)) {
        Text(text = " ", style = style, maxLines = 1, color = Color.Transparent)
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(vertical = dimensionResource(TvR.dimen.tv_skeleton_caption_plate_inset))
                .clip(BingeShapes.Tag)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

/** A centred message filling the space the grid would have taken. */
@Composable
private fun AvatarGridMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
@ReadOnlyComposable
private fun avatarGridPadding() =
    PaddingValues(
        start = dimensionResource(TvR.dimen.tv_content_gutter_start),
        end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
        // The top inset clears the focus ring's bleed so the first row's ring is not shaved by the grid's viewport clip.
        top = dimensionResource(TvR.dimen.tv_media_grid_row_gap),
        bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
    )

/** Six columns, not a poster grid's five: a face is a ~120dp circle plus two caption lines; at five they read outsized, at seven the captions truncate. */
private const val AVATAR_GRID_COLUMNS = 6

/** Narrower than the cell, so the caption plates read as text rather than as a second tile. */
private const val SKELETON_CAPTION_WIDTH_FRACTION = 0.7f
