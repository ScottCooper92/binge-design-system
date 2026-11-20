@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvInfoGroup
import com.binge.designsystem.tv.template.TV_DETAIL_WIDE_ASPECT
import com.binge.designsystem.tv.template.TvCastCell
import com.binge.designsystem.tv.template.TvDetailCaption
import com.binge.designsystem.tv.template.TvDetailCast
import com.binge.designsystem.tv.template.TvDetailGalleryGrid
import com.binge.designsystem.tv.template.TvDetailGalleryImage
import com.binge.designsystem.tv.template.TvDetailOverview
import com.binge.designsystem.tv.template.TvDetailSlotCarousel
import com.binge.designsystem.tv.template.TvDetailTab
import com.binge.designsystem.tv.template.TvTitleDetailPage
import com.binge.designsystem.tv.template.mixedForGrid

private val Info = listOf(
    TvInfoGroup("Status", "Released"),
    TvInfoGroup("Original language", "English"),
    TvInfoGroup("Budget", "$40,000,000"),
    TvInfoGroup("Revenue", "$126,000,000"),
    TvInfoGroup("Production", "Long Way Pictures, Northern Lights"),
    TvInfoGroup("Collection", "The Long Way series"),
)

private val Cast = listOf(TvCastCell("director", "Dee Director", "Director", "https://preview.invalid/dee.jpg")) +
    List(5) { TvCastCell(key = "cast-$it", name = "Actor $it", role = "Role $it", profileUrl = null) }

private val Gallery = List(10) { TvDetailGalleryImage("https://preview.invalid/$it.jpg", portrait = it % 5 == 4, index = it) }

private const val SEE_ALL_POSITION = 99

/** The Details tab alone: the logo, meta and synopsis, then the info fields in columns. It is one scrollable stop, so it has no ring. */
@Composable
fun TvDetailOverviewSample() {
    TvDetailOverview(title = DetailSampleTitle, infoGroups = Info)
}

/** The Cast tab at rest: the first person large in the slot, the rest along the strip, and no ring until the tab is focused. */
@Composable
fun TvDetailCastSample() {
    TvDetailCast(people = Cast, onCastClick = {})
}

/** The Cast tab focused on its second person: the slot and caption follow the step, and the card is ringed. */
@Composable
fun TvDetailCastFocusedSample() {
    TvDetailCast(people = Cast, onCastClick = {}, initialPosition = 1, initialFocused = true)
}

/** The Cast tab stepped past the last person onto the See all card, which takes the ring while the slot keeps the last person. */
@Composable
fun TvDetailCastSeeAllFocusedSample() {
    TvDetailCast(people = Cast, onCastClick = {}, onSeeAll = {}, initialPosition = SEE_ALL_POSITION, initialFocused = true)
}

/** A cast with no one to show: the tab is the See all card alone, focused, so the full list stays reachable. */
@Composable
fun TvDetailCastSeeAllOnlySample() {
    TvDetailCast(people = emptyList(), onCastClick = {}, onSeeAll = {}, initialFocused = true)
}

/** A 16:9 carousel with a title over a quieter line, as a Videos tab draws it. */
@Composable
fun TvDetailSlotCarouselSample() {
    TvDetailSlotCarousel(
        items = listOf("Official Trailer", "Teaser", "Behind the scenes"),
        key = { it },
        aspect = TV_DETAIL_WIDE_ASPECT,
        slotHeight = dimensionResource(R.dimen.tv_detail_wide_slot_height),
        stripHeight = dimensionResource(R.dimen.tv_detail_wide_strip_height),
        onClick = {},
        image = {},
        caption = { TvDetailCaption(title = it, subtitle = "Trailer · 2025") },
        initialFocused = true,
    )
}

/** The gallery: landscape images paired in two rows, a portrait one spanning both, and the ring on one image. */
@Composable
fun TvDetailGalleryGridSample() {
    TvDetailGalleryGrid(images = Gallery.mixedForGrid(), onOpen = {}, initialFocusedIndex = 2)
}

/** The page expanded onto its Cast tab, with the Details and Cast tabs drawing their real content. */
@Composable
fun TvTitleDetailPageTabsSample() {
    val actionsFocus = remember { FocusRequester() }
    TvTitleDetailPage(
        title = DetailSampleTitle,
        tabs = listOf(
            TvDetailTab("details", "Details") { TvDetailOverview(DetailSampleTitle, Info) },
            TvDetailTab("cast", "Cast") { TvDetailCast(people = Cast, onCastClick = {}) },
        ),
        actionsFocus = actionsFocus,
        hasActions = false,
        initialExpanded = true,
        initialTabKey = "cast",
    ) {}
}
