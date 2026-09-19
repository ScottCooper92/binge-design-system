package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.BingeShapes

private const val SKELETON_CARD_COUNT = 6

@Composable
fun CarouselSkeleton(modifier: Modifier = Modifier, numCards: Int = SKELETON_CARD_COUNT) {
    val inset = dimensionResource(R.dimen.screen_content_inset)
    // Matches the loaded MediaCarousel: full-bleed row, first column clear of an overlaying rail.
    val startPadding = inset + navOverlayStart()
    Column(modifier = modifier.fillMaxWidth()) {
        // Reserve the loaded SectionHeader's row height: its content is floored at the touch target
        // (SectionHeader), so this reserves the same token rather than a line height of its own.
        Box(
            modifier = Modifier
                .padding(
                    start = startPadding,
                    end = inset,
                    top = dimensionResource(R.dimen.section_header_padding_v),
                    bottom = dimensionResource(R.dimen.section_header_padding_v),
                ).height(dimensionResource(R.dimen.min_touch_target)),
            contentAlignment = Alignment.CenterStart,
        ) {
            SkeletonBar(Modifier.width(dimensionResource(R.dimen.skeleton_header_width)))
        }
        LazyRow(
            contentPadding = PaddingValues(start = startPadding, end = inset),
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

@Composable
private fun SkeletonBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(dimensionResource(R.dimen.skeleton_header_height))
            .clip(BingeShapes.ElementSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCarouselSkeleton() {
    BingeExpressiveTheme {
        CarouselSkeleton()
    }
}
