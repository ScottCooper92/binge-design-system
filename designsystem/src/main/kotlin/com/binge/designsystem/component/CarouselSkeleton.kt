package com.binge.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.navOverlayEnd
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeExpressiveTheme

private const val SKELETON_CARD_COUNT = 6

@Composable
fun CarouselSkeleton(modifier: Modifier = Modifier, numCards: Int = SKELETON_CARD_COUNT) {
    val sides = carouselSkeletonSides()
    Column(modifier = modifier.fillMaxWidth()) {
        // Reserve the loaded SectionHeader's row height: its content is floored at the touch target
        // (SectionHeader), so this reserves the same token rather than a line height of its own.
        Box(
            modifier = Modifier
                .padding(sides)
                .padding(vertical = dimensionResource(R.dimen.section_header_padding_v))
                .height(dimensionResource(R.dimen.min_touch_target)),
            contentAlignment = Alignment.CenterStart,
        ) {
            SkeletonPlate(
                Modifier
                    .width(dimensionResource(R.dimen.skeleton_header_width))
                    .height(dimensionResource(R.dimen.skeleton_header_height)),
            )
        }
        LazyRow(
            contentPadding = sides,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
            userScrollEnabled = false,
        ) {
            items(numCards) {
                // MediaCardSkeleton, not a bare poster plate: the row resolves to MediaCards, whose two-line
                // title sits 46dp below the poster and moved every rail after this one when unreserved.
                MediaCardSkeleton(Modifier.width(dimensionResource(R.dimen.card_width)))
            }
        }
    }
}

/**
 * The skeleton's start and end, as the loaded [MediaCarousel] and [SectionHeader] pad theirs: the content inset, plus
 * whatever an overlaying rail covers at the start and whatever a shell publishes at the end (#512).
 */
@Composable
internal fun carouselSkeletonSides(): PaddingValues {
    val inset = resolvedContentInset()
    return PaddingValues(start = inset + navOverlayStart(), end = inset + navOverlayEnd())
}

@Preview(showBackground = true)
@Composable
private fun PreviewCarouselSkeleton() {
    BingeExpressiveTheme {
        CarouselSkeleton()
    }
}
