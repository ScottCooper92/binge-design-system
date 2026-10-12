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
import com.binge.designsystem.modifier.describedAs
import com.binge.designsystem.navOverlayEnd
import com.binge.designsystem.navOverlayStart
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.theme.BingeExpressiveTheme

private const val SKELETON_CARD_COUNT = 6

/**
 * Loading placeholder for a [MediaCarousel] under its [SectionHeader]: the heading plate, then [count] card
 * plates on the loaded row's insets.
 *
 * [description] names the loading state for a screen reader; without one it stays silent.
 */
@Composable
fun CarouselSkeleton(
    modifier: Modifier = Modifier,
    count: Int = SKELETON_CARD_COUNT,
    description: String? = null,
) {
    val sides = carouselSkeletonSidePadding()
    Column(modifier = modifier.fillMaxWidth().describedAs(description)) {
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
            items(count) {
                // MediaCardSkeleton, not a bare poster plate: the row resolves to MediaCards, whose two-line
                // title sits 46dp below the poster and moved every rail after this one when unreserved.
                MediaCardSkeleton(Modifier.width(dimensionResource(R.dimen.card_width)))
            }
        }
    }
}

/**
 * [CarouselSkeleton] under its old name for the card count, `numCards`; it is `count` now, as on the other skeletons.
 * Kotlin cannot hold two overloads that differ only in parameter names, so this one has no default for `numCards`: a
 * call that passes none resolves to the new function, and a call that passes `numCards` lands here. `numCards` and `description` swap places so the two overloads have different
 * signatures on the JVM; call it by name, as the old signature was.
 */
@Deprecated(
    message = "Renamed: numCards is count.",
    replaceWith = ReplaceWith("CarouselSkeleton(modifier, count = numCards, description = description)"),
)
@Suppress("ModifierParameter")
@Composable
fun CarouselSkeleton(
    modifier: Modifier = Modifier,
    description: String? = null,
    numCards: Int,
) = CarouselSkeleton(modifier = modifier, count = numCards, description = description)

/**
 * The skeleton's side padding, as the loaded MediaCarousel and SectionHeader take theirs: the content inset, plus an
 * overlay a shell or hub publishes on either side, so the first and last columns clear it as the loaded row's do.
 */
@Composable
internal fun carouselSkeletonSidePadding(): PaddingValues {
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
