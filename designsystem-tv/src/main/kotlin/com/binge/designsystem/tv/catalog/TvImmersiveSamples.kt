@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvSeeAllTile
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.nav.LocalTvContentInset
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.template.TvImmersiveHub
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val ROW_ITEM_COUNT = 8
private const val SEE_ALL_AFTER = 7

private data class SampleTitle(
    val id: Int,
    val title: String,
    val meta: String,
    val synopsis: String,
)

private fun sampleRow(
    key: String,
    title: String,
    firstId: Int,
    seeAll: Boolean = false,
) = TvHubRow(
    key = key,
    title = title,
    items =
        List(ROW_ITEM_COUNT) { index ->
            SampleTitle(
                id = firstId + index,
                title = "A title in $title ${index + 1}",
                meta = "Movie  ·  2026  ·  12A",
                synopsis = "A synopsis long enough to fill two lines of the copy band over the rows, so the sample shows how it clamps.",
            )
        },
    onSeeAll = if (seeAll) ({}) else null,
)

private val SampleRows =
    listOf(
        sampleRow("pending", "Pending (8)", firstId = 100),
        sampleRow("approved", "Approved (24)", firstId = 200, seeAll = true),
        sampleRow("available", "Available (31)", firstId = 300),
    )

/** Stands a page in for a rail destination: the rail's collapsed width is cleared, as the shell provides it. */
@Composable
private fun RailHosted(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTvContentInset provides dimensionResource(TvR.dimen.tv_nav_rail_collapsed_width),
        content = content,
    )
}

/** The hub's cards, in a spread of container tones so a frame reads as a run of different posters. */
@Composable
private fun sampleTone(id: Int): Color =
    when (id % SEE_ALL_AFTER) {
        0 -> MaterialTheme.colorScheme.primaryContainer
        1 -> MaterialTheme.colorScheme.secondaryContainer
        2 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

@Composable
private fun SampleHub(
    hero: (@Composable () -> Unit)? = null,
    initialFocused: Pair<String, Int>? = null,
    rows: List<TvHubRow<SampleTitle>> = SampleRows,
) {
    var focusedId by remember { mutableStateOf(initialFocused?.second) }
    RailHosted {
        TvImmersiveHub(
            rows = rows,
            itemId = { it.id },
            cardWidth = dimensionResource(TvR.dimen.tv_immersive_card_width),
            onItemClick = {},
            artwork = { title ->
                // A gradient in the card's own tone, since a sample has no network to fetch art from.
                val tone = sampleTone(title.id)
                Box(
                    modifier =
                        Modifier.fillMaxSize().background(Brush.linearGradient(listOf(tone, MaterialTheme.colorScheme.background))),
                )
            },
            copy = { title ->
                Text(
                    text = title.meta,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
                Text(
                    text = title.title,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = title.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            hero = hero,
            initialFocused = initialFocused,
            seeAllLabel = "See all",
        ) { title, isFocused, onFocusChanged, onClick, cellModifier ->
            Box(
                modifier =
                    cellModifier
                        .aspectRatio(POSTER_RATIO)
                        .tvFocusIndicator(isFocused = isFocused, shape = BingeShapes.MediaCard)
                        .clip(BingeShapes.MediaCard)
                        .background(sampleTone(title.id))
                        .tvClickable(
                            onFocusChanged = {
                                if (it) focusedId = title.id
                                onFocusChanged(it)
                            },
                            onClick = onClick,
                        ),
            )
        }
    }
}

private const val POSTER_RATIO = 2f / 3f

/** A row of posters per filter over a backdrop that follows focus, with no hero: at rest it shows the first title. */
@Composable
fun TvImmersiveHubSample() {
    SampleHub()
}

/** The same hub with a hero above the rows, which gives way to the backdrop once a card takes focus. */
@Composable
fun TvImmersiveHubHeroSample() {
    SampleHub(
        hero = {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(start = tvContentGutterStart(), top = dimensionResource(TvR.dimen.tv_overscan_vertical)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
            ) {
                Text(text = "Server status", style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    text = "Connected  ·  version 2.7",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

/** Focus on the second row: the row is pulled to the top of the inset viewport and the rows above it are clipped away. */
@Composable
fun TvImmersiveHubFocusedSample() {
    SampleHub(initialFocused = "approved" to 202)
}

/** The tile that closes a row, at rest and focused, beside each other. */
@Composable
fun TvSeeAllTileSample() {
    Row(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
    ) {
        TvSeeAllTile(
            label = "See all",
            isFocused = false,
            onFocusChanged = {},
            onClick = {},
            modifier = Modifier.width(dimensionResource(TvR.dimen.tv_immersive_card_width)),
        )
        TvSeeAllTile(
            label = "See all",
            isFocused = true,
            onFocusChanged = {},
            onClick = {},
            modifier = Modifier.width(dimensionResource(TvR.dimen.tv_immersive_card_width)),
        )
    }
}
