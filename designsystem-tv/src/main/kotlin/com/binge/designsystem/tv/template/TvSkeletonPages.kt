package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvSkeletonBlock
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val POSTER_RATIO = 2f / 3f
private const val LANDSCAPE_RATIO = 4f / 3f
private const val SYNOPSIS_LINES = 3

/**
 * The loading state of an immersive hub (`TvImmersiveHub`): the text band over where the backdrop will be, then rows of
 * poster plates under their headings, laid out where the loaded hub puts them so nothing moves when the content
 * arrives. [rows] and [cardsPerRow] say how much to suggest; the cards run off the edge as a loaded row does.
 *
 * It is a page, laid out like the hub: its edges follow the hub's own, the content gutter at the start and rows that
 * run to the end of the screen. Nothing in it is focusable, so while it shows the D-pad has nowhere to go, and focus
 * is placed by the loaded page when it replaces this one. [description] names the state for a screen reader.
 * [hosting] is the loaded hub's: an overlay or a pre-shell page lands focus on the first row at once, and the hub
 * anchors that row at the foot of the copy band rather than a row gap below it, so the skeleton's rows sit there too.
 * It stands in for a hub with no hero only: a hero hub lands focus on the hero, so a hero hub shows its own loading state.
 */
@Composable
fun TvImmersiveHubSkeleton(
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    rows: Int = 2,
    cardsPerRow: Int = 8,
    cardWidth: Dp = dimensionResource(TvR.dimen.tv_immersive_card_width),
    description: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(top = dimensionResource(TvR.dimen.tv_overscan_vertical))
                .describedAs(description),
    ) {
        Column(
            modifier = Modifier
                .height(
                    dimensionResource(TvR.dimen.tv_immersive_content_top) - dimensionResource(TvR.dimen.tv_overscan_vertical),
                ).padding(start = tvContentGutterStart(), bottom = dimensionResource(DesR.dimen.padding_l))
                .tvLayoutAnchor(TvLayoutAnchors.HUB_COPY),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s), Alignment.Bottom),
        ) {
            TvSkeletonBlock(
                Modifier
                    .width(
                        dimensionResource(TvR.dimen.tv_skeleton_overline_width),
                    ).height(dimensionResource(TvR.dimen.tv_skeleton_meta_height)),
            )
            TvSkeletonBlock(
                Modifier
                    .width(
                        dimensionResource(TvR.dimen.tv_skeleton_title_width),
                    ).height(dimensionResource(TvR.dimen.tv_skeleton_title_height)),
            )
            TvSkeletonBlock(
                Modifier
                    .width(
                        dimensionResource(TvR.dimen.tv_skeleton_meta_width),
                    ).height(dimensionResource(TvR.dimen.tv_skeleton_meta_height)),
            )
        }
        if (hosting == TvPageHosting.RailDestination) Spacer(Modifier.height(dimensionResource(TvR.dimen.tv_immersive_row_gap)))
        // Measured at their own height and clipped by the screen: a loaded hub's rows run off the bottom, and a row
        // squeezed into what is left would draw its heading over its cards.
        Column(
            modifier = Modifier.wrapContentHeight(align = Alignment.Top, unbounded = true),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_immersive_row_gap)),
        ) {
            repeat(rows) { index -> PosterRowSkeleton(cardsPerRow, cardWidth, Modifier.tvLayoutAnchor(TvLayoutAnchors.hubRow(index))) }
        }
    }
}

/**
 * The loading state of a detail page (`TvDetailPage`): the hero band at its loaded height, with the poster beside
 * the overline, title, facts and synopsis lines and the action pills at its foot, then one row of cards. Not
 * focusable. Every block sits where the loaded page puts it, so nothing moves when the content arrives.
 *
 * The detail page pads the same in every [hosting], so the skeleton does too; the parameter matches the page's.
 * [firstSectionKey] is the key of the page's first section, so the row stands in for it as a layout anchor.
 */
@Composable
fun TvDetailPageSkeleton(
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") hosting: TvPageHosting = currentTvPageHosting(),
    description: String? = null,
    firstSectionKey: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .describedAs(description),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_section_gap)),
    ) {
        DetailHeroSkeleton(Modifier.tvLayoutAnchor(TvLayoutAnchors.entry(TvLayoutAnchors.HERO_KEY)))
        DetailRowSkeleton(firstSectionKey?.let { Modifier.tvLayoutAnchor(TvLayoutAnchors.entry(it)) } ?: Modifier)
    }
}

/** `TvDetailHero`'s band: its height, its content padding, the poster and copy on the weighted row, the pills at the foot. */
@Composable
private fun DetailHeroSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(dimensionResource(TvR.dimen.tv_detail_page_hero_height))
                .padding(
                    start = tvContentGutterStart(),
                    end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                    top = dimensionResource(TvR.dimen.tv_detail_page_hero_content_top),
                ),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_hero_gap)),
        ) {
            TvSkeletonBlock(
                Modifier.width(dimensionResource(TvR.dimen.tv_detail_page_poster_width)).aspectRatio(POSTER_RATIO),
                shape = BingeShapes.MediaCard,
            )
            Column(
                modifier = Modifier.width(dimensionResource(TvR.dimen.tv_detail_page_copy_width)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_copy_gap)),
            ) {
                TvSkeletonBlock(
                    Modifier
                        .width(dimensionResource(TvR.dimen.tv_skeleton_overline_width))
                        .height(dimensionResource(TvR.dimen.tv_skeleton_meta_height)),
                )
                TvSkeletonBlock(
                    Modifier
                        .width(dimensionResource(TvR.dimen.tv_skeleton_title_width))
                        .height(dimensionResource(TvR.dimen.tv_skeleton_title_height)),
                )
                TvSkeletonBlock(
                    Modifier
                        .width(dimensionResource(TvR.dimen.tv_skeleton_meta_width))
                        .height(dimensionResource(TvR.dimen.tv_skeleton_meta_height)),
                )
                repeat(SYNOPSIS_LINES) { line ->
                    TvSkeletonBlock(
                        Modifier
                            .fillMaxWidth(if (line == SYNOPSIS_LINES - 1) LAST_LINE_FRACTION else 1f)
                            .height(dimensionResource(TvR.dimen.tv_skeleton_meta_height)),
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s))) {
            TvSkeletonBlock(
                Modifier
                    .width(dimensionResource(TvR.dimen.tv_detail_page_action_primary_min_width))
                    .height(dimensionResource(TvR.dimen.tv_skeleton_action_height)),
                shape = BingeShapes.Pill,
            )
            TvSkeletonBlock(
                Modifier
                    .width(dimensionResource(TvR.dimen.tv_skeleton_action_height))
                    .height(dimensionResource(TvR.dimen.tv_skeleton_action_height)),
                shape = BingeShapes.Pill,
            )
        }
    }
}

/**
 * A detail section's card row, at the start gutter `TvCardRow` uses. Unbounded below, as the loaded page's row runs
 * off the panel's foot, so a short panel does not squeeze the cards up over the heading.
 */
@Composable
private fun DetailRowSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().wrapContentHeight(align = Alignment.Top, unbounded = true),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        TvSkeletonBlock(
            Modifier
                .padding(start = tvContentGutterStart())
                .width(dimensionResource(TvR.dimen.tv_skeleton_heading_width))
                .height(dimensionResource(TvR.dimen.tv_skeleton_heading_height)),
        )
        LazyRow(
            contentPadding =
                PaddingValues(
                    start = tvContentGutterStart(),
                    end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
            userScrollEnabled = false,
        ) {
            items(count = DETAIL_CARDS) {
                TvSkeletonBlock(
                    Modifier.width(dimensionResource(TvR.dimen.tv_skeleton_card_width)).aspectRatio(LANDSCAPE_RATIO),
                    shape = BingeShapes.MediaCard,
                )
            }
        }
    }
}

private const val LAST_LINE_FRACTION = 0.6f
private const val DETAIL_CARDS = 6

@Composable
private fun PosterRowSkeleton(
    cards: Int,
    cardWidth: Dp,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        TvSkeletonBlock(
            Modifier
                .padding(start = tvContentGutterStart())
                .width(
                    dimensionResource(TvR.dimen.tv_skeleton_heading_width),
                ).height(dimensionResource(TvR.dimen.tv_skeleton_heading_height)),
        )
        LazyRow(
            contentPadding = PaddingValues(
                start = tvContentGutterStart(),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_card_gap)),
            userScrollEnabled = false,
        ) {
            items(count = cards) {
                TvSkeletonBlock(Modifier.width(cardWidth).aspectRatio(POSTER_RATIO), shape = BingeShapes.MediaCard)
            }
        }
    }
}

/** Names the loading state for a screen reader when [description] is given; otherwise leaves the page silent. */
private fun Modifier.describedAs(description: String?): Modifier =
    if (description == null) this else semantics { contentDescription = description }
