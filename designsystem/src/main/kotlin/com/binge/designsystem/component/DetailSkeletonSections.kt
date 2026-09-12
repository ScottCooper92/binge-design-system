package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.lineHeightOf
import com.binge.designsystem.theme.BingeShapes

private const val SKELETON_STAT_COUNT = 3

/** The value bar fills less of its cell than the label below it, so the pair reads as a stat, not a block. */
private const val SKELETON_STAT_VALUE_FRACTION = 0.45f
private const val SKELETON_OVERVIEW_LINE_COUNT = 3
private const val SKELETON_CAST_COUNT = 4
private const val SKELETON_LAST_OVERVIEW_LINE_FRACTION = 0.6f
private const val SKELETON_DETAILS_ROW_COUNT = 3

/** Ragged on purpose: three equal bars read as a table, which the info list is not. */
private val SKELETON_DETAILS_VALUE_FRACTIONS = listOf(0.8f, 0.5f, 0.65f)
private const val SKELETON_CAST_LABEL_FRACTION = 0.8f
private const val SKELETON_CAST_SUBLABEL_FRACTION = 0.6f

// The body bands [DetailScreenSkeleton] stacks below the header, one per section a detail page can lead with (the
// header bands are in DetailSkeletonHeaders.kt). They sit beside the entry point because three page shapes draw from
// the same set; each band's KDoc records what it stands in for — what a reader checks when a geometry test fails.

/**
 * Stands in for [DetailStatRow], cell for cell.
 *
 * It mirrors the real cell's structure — the same weight, the same vertical inset, the same icon size, the
 * same gaps, and a bar per text line at that line's own height ([lineHeightOf]) — rather than approximating
 * it with two short bars. Two bars left the row 54dp shorter than the row it replaced, so the action row and
 * everything below it jumped down the moment a page resolved (#1598).
 */

@Composable
internal fun SkeletonStatRow(modifier: Modifier = Modifier) {
    val valueHeight = lineHeightOf(MaterialTheme.typography.titleSmall)
    val labelHeight = lineHeightOf(MaterialTheme.typography.labelSmall)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.screen_content_inset)),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        repeat(SKELETON_STAT_COUNT) {
            Column(
                // weight(1f), as StatCell takes, so the three cells divide the row rather than each sizing
                // to its own widest bar — which left the label bars overlapping their neighbours.
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = dimensionResource(R.dimen.detail_meta_spacing)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.spacedBy(dimensionResource(R.dimen.detail_cast_avatar_label_spacing)),
            ) {
                Box(
                    Modifier
                        .size(dimensionResource(R.dimen.detail_stat_icon_size))
                        .clip(BingeShapes.ElementSmall)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                SkeletonPlate(Modifier.fillMaxWidth(SKELETON_STAT_VALUE_FRACTION).height(valueHeight))
                SkeletonPlate(Modifier.fillMaxWidth(SKELETON_CAST_SUBLABEL_FRACTION).height(labelHeight))
            }
        }
    }
}

/**
 * One plate the height of the episode page's rating card, on the card's own inset and corner.
 *
 * The episode page has no play/request controls, so the media pages' [SkeletonActionRow] promised buttons
 * that never arrive and left the overview below it reserved at the wrong height — the phone counterpart of
 * the television's `actionCount = 0` correction (#1598).
 */
@Composable
internal fun SkeletonRatingCard(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.screen_content_inset))
            .height(dimensionResource(R.dimen.detail_skeleton_rating_card_height))
            .clip(BingeShapes.MediaCard)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
fun SkeletonActionRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.screen_content_inset)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_action_row_spacing)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The primary plate is the height of the filled button it replaces, not the icon buttons beside it:
        // that button is the tallest thing in the row, so it is what sets the row's height (#1598).
        SkeletonPlate(
            Modifier
                .weight(1f)
                .height(dimensionResource(R.dimen.button_filled_height)),
        )
        Box(
            Modifier
                .size(dimensionResource(R.dimen.detail_action_button_size))
                .clip(BingeShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Box(
            Modifier
                .size(dimensionResource(R.dimen.detail_action_button_size))
                .clip(BingeShapes.Pill)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

@Composable
internal fun SkeletonOverview(horizontalPadding: Dp, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        SkeletonPlate(
            Modifier
                .width(dimensionResource(R.dimen.skeleton_header_width))
                .height(dimensionResource(R.dimen.skeleton_header_height)),
        )
        repeat(SKELETON_OVERVIEW_LINE_COUNT) { index ->
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(
                        if (index == SKELETON_OVERVIEW_LINE_COUNT - 1) SKELETON_LAST_OVERVIEW_LINE_FRACTION else 1f,
                    ).height(dimensionResource(R.dimen.detail_skeleton_text_height)),
            )
        }
    }
}

/**
 * Three info rows standing in for the details block — director, release date, studios, status.
 *
 * Three is an average, not a measurement, and this band is the one place the skeleton cannot keep the #1598
 * promise exactly: [InfoRow] renders nothing when its value is blank, so a real page shows anywhere from two
 * rows to six and no fixed height matches them all. Reserving an average beats reserving nothing, which is
 * what stood here before and left the cast rail arriving 124dp adrift. What it costs the geometry check is
 * recorded on [com.binge.designsystem.layout.LayoutAnchors.Detail.CAST].
 *
 * The label bar takes `info_row_label_min_width` and the gap `info_row_gap`, so the two columns line up with
 * the real rows rather than merely occupying the same band.
 */
@Composable
internal fun SkeletonDetailsSection(horizontalPadding: Dp, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.info_row_spacing_v)),
    ) {
        repeat(SKELETON_DETAILS_ROW_COUNT) { index ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.info_row_gap)),
            ) {
                SkeletonPlate(
                    Modifier
                        .width(dimensionResource(R.dimen.info_row_label_min_width))
                        .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
                )
                // The value bar measures against the row's remaining width, so the fraction rides a
                // weighted Box rather than a fillMaxWidth that would size against the whole row.
                Box(modifier = Modifier.weight(1f)) {
                    SkeletonPlate(
                        Modifier
                            .fillMaxWidth(SKELETON_DETAILS_VALUE_FRACTIONS[index])
                            .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
                    )
                }
            }
        }
    }
}

@Composable
internal fun SkeletonCastRow(horizontalPadding: Dp, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // The heading reserves SectionHeader's whole row — line height plus the vertical padding either side — not
        // a bare plate with a gap beneath it: that sat the title hard against the rail and left the band 26dp short of
        // the header it stands in for. CarouselSkeleton already gives the hub headings this treatment, for the same reason.
        Box(
            modifier = Modifier
                .padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = dimensionResource(R.dimen.section_header_padding_v),
                    bottom = dimensionResource(R.dimen.section_header_padding_v),
                ).height(dimensionResource(R.dimen.min_touch_target)),
            contentAlignment = Alignment.CenterStart,
        ) {
            SkeletonPlate(
                Modifier
                    .width(dimensionResource(R.dimen.skeleton_header_width))
                    .height(dimensionResource(R.dimen.skeleton_header_height)),
            )
        }
        // The inset rides the row's contentPadding rather than an outer padding, so the plates bleed
        // past the margin exactly as CastSection's cards do.
        LazyRow(
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
            userScrollEnabled = false,
        ) {
            items(SKELETON_CAST_COUNT, key = { it }) {
                Column(
                    modifier = Modifier.width(dimensionResource(R.dimen.cast_card_width)),
                    verticalArrangement = Arrangement.spacedBy(
                        dimensionResource(R.dimen.detail_cast_avatar_label_spacing),
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // A circle at the avatar's own size, not a poster plate filling the card width:
                    // CastSection draws round faces, and a portrait plate resolved into one on load.
                    Box(
                        Modifier
                            .size(dimensionResource(R.dimen.cast_avatar_size))
                            .clip(BingeShapes.Pill)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                    SkeletonPlate(
                        Modifier
                            .fillMaxWidth(SKELETON_CAST_LABEL_FRACTION)
                            .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
                    )
                    SkeletonPlate(
                        Modifier
                            .fillMaxWidth(SKELETON_CAST_SUBLABEL_FRACTION)
                            .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
                    )
                }
            }
        }
    }
}

/**
 * The plate every detail band is built from — one rounded, filled rectangle, sized by its caller.
 *
 * Named a plate rather than a bar because `CarouselSkeleton` already has a private `SkeletonBar` of its own
 * that bakes in a fixed height; the two are not the same primitive, and one internal and one private under
 * the same name is an overload ambiguity across the package.
 */
@Composable
internal fun SkeletonPlate(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(BingeShapes.ElementSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
