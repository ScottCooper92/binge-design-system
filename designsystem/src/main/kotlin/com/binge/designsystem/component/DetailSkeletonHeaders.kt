package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.CARD_ASPECT_RATIO
import com.binge.designsystem.R
import com.binge.designsystem.component.ImagePlaceholder
import com.binge.designsystem.theme.BingeShapes

private const val SKELETON_HERO_TITLE_FRACTION = 0.7f
private const val SKELETON_HERO_TAGLINE_FRACTION = 0.5f
private const val SKELETON_HERO_EYEBROW_FRACTION = 0.35f
private const val SKELETON_CINEMATIC_COPY_FRACTION = 0.9f

// The bands standing in for a detail page's *header* — the one band that swaps by form factor
// ([SkeletonHero] against [SkeletonCinematicHeader]) and by page shape ([SkeletonProfileHeader]). Split from
// the stacked body bands in DetailSkeletonSections.kt, which the header sits above on every shape.

@Composable
internal fun SkeletonHero() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.detail_hero_height)),
    ) {
        ImagePlaceholder(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = dimensionResource(R.dimen.screen_content_inset),
                    end = dimensionResource(R.dimen.screen_content_inset),
                    bottom = dimensionResource(R.dimen.detail_hero_text_bottom_padding),
                ),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_meta_spacing)),
        ) {
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(SKELETON_HERO_EYEBROW_FRACTION)
                    .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
            )
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(SKELETON_HERO_TITLE_FRACTION)
                    .height(dimensionResource(R.dimen.skeleton_header_height)),
            )
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(SKELETON_HERO_TAGLINE_FRACTION)
                    .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
            )
        }
    }
}

@Composable
internal fun SkeletonCinematicHeader() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.detail_cinematic_header_height)),
    ) {
        ImagePlaceholder(Modifier.fillMaxSize())
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = dimensionResource(R.dimen.detail_cinematic_header_padding),
                    end = dimensionResource(R.dimen.detail_cinematic_header_padding),
                    bottom = dimensionResource(R.dimen.detail_cinematic_header_bottom_padding),
                ),
            horizontalArrangement =
                Arrangement.spacedBy(dimensionResource(R.dimen.detail_cinematic_poster_copy_spacing)),
            verticalAlignment = Alignment.Bottom,
        ) {
            Box(
                Modifier
                    .width(dimensionResource(R.dimen.detail_cinematic_poster_width))
                    .aspectRatio(CARD_ASPECT_RATIO)
                    .clip(BingeShapes.MediaCard)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement =
                    Arrangement.spacedBy(dimensionResource(R.dimen.detail_cinematic_copy_spacing)),
            ) {
                SkeletonPlate(
                    Modifier
                        .fillMaxWidth(SKELETON_HERO_EYEBROW_FRACTION)
                        .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
                )
                SkeletonPlate(
                    Modifier
                        .fillMaxWidth(SKELETON_CINEMATIC_COPY_FRACTION)
                        .height(dimensionResource(R.dimen.skeleton_header_height)),
                )
                SkeletonPlate(
                    Modifier
                        .fillMaxWidth(SKELETON_HERO_TAGLINE_FRACTION)
                        .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
                )
                SkeletonPlate(
                    Modifier
                        .width(dimensionResource(R.dimen.detail_skeleton_value_width))
                        .height(dimensionResource(R.dimen.button_filled_height)),
                )
            }
        }
    }
}

/**
 * The person page's header: a bounded poster beside a copy column, on the [horizontalPadding]
 * `PersonProfileSection` is given and behind the same status-bar padding and top-bar offset the resolved
 * page's scroll column carries. It
 * is a separate shape from [SkeletonHero] rather than a narrower one because the person page has no
 * full-bleed backdrop to stand in for — drawing one reserved a band the page never fills.
 */
@Composable
internal fun SkeletonProfileHeader(horizontalPadding: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = horizontalPadding,
                end = horizontalPadding,
                top = dimensionResource(R.dimen.person_profile_padding_top),
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.person_profile_gap)),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            Modifier
                .width(dimensionResource(R.dimen.person_profile_poster_width))
                .aspectRatio(CARD_ASPECT_RATIO)
                .clip(BingeShapes.ProfilePoster)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.detail_meta_spacing)),
        ) {
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(SKELETON_HERO_EYEBROW_FRACTION)
                    .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
            )
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(SKELETON_HERO_TITLE_FRACTION)
                    .height(dimensionResource(R.dimen.skeleton_header_height)),
            )
            SkeletonPlate(
                Modifier
                    .fillMaxWidth(SKELETON_HERO_TAGLINE_FRACTION)
                    .height(dimensionResource(R.dimen.detail_skeleton_text_height)),
            )
        }
    }
}
