@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.component.HeroItem
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvExpandingMeta
import com.binge.designsystem.tv.template.TvExpandingHeroCard
import com.binge.designsystem.tv.template.TvExpandingHubLoadingSkeleton
import com.binge.designsystem.tv.template.TvExpandingPendingRow
import com.binge.designsystem.tv.template.TvExpandingRowBody
import com.binge.designsystem.tv.template.TvExpandingRowState
import com.binge.designsystem.tv.template.TvExpandingRowsHub
import com.binge.designsystem.tv.template.TvExpandingSlot
import com.binge.designsystem.tv.template.TvExpandingTitles
import com.binge.designsystem.tv.template.TvHubItem
import com.binge.designsystem.tv.template.TvHubRow
import com.binge.designsystem.tv.template.TvTitleExtras

private const val LongCount = 10
private const val ShortCount = 4
private const val LoopMin = 8

private val Extras = TvTitleExtras(
    logoUrl = "https://preview.invalid/logo.png",
    certification = "12A",
    overview = "A washed-up bounty hunter takes one last job, and the job takes the rest of what he had left to lose.",
    year = "2024",
)

private val Hero = HeroItem(
    id = 1,
    imageUrl = null,
    title = "The Long Way Home",
    rating = 8.1f,
    genres = listOf("Drama", "Thriller"),
    year = "2024",
    rank = 3,
)

private fun hubItem(index: Int) =
    TvHubItem(
        key = "movie_$index",
        posterUrl = null,
        title = "Title $index",
        rating = 7.4f,
        genres = listOf("Adventure", "Action"),
        year = "2024",
        overview = "A washed-up bounty hunter takes one last job, and the job takes the rest of what he had left to lose.",
    )

private val PopularThenPending = listOf(
    TvHubRow(key = "popular", title = "Popular movies", items = List(LongCount) { hubItem(it) }),
    TvHubRow(key = "services", title = "On your services", items = emptyList<TvHubItem>(), isLoading = true),
)

@Composable
private fun Row(
    position: Int,
    expanded: Boolean,
    isFocused: Boolean,
    extras: TvTitleExtras? = null,
    count: Int = LongCount,
    failure: String? = null,
) {
    val items = List(count) { hubItem(it) }
    val titles = TvExpandingTitles(count = count, at = { items.getOrNull(it) }, loops = count >= LoopMin)
    TvExpandingRowBody(
        title = "Popular movies",
        titles = titles,
        position = position,
        item = items[titles.index(position)].let { shown -> if (failure == null) shown else shown.copy(failure = failure) },
        extras = { extras },
        state = TvExpandingRowState(isFocused = isFocused, expanded = expanded, arrival = { 1f }),
        strip = rememberLazyListState(initialFirstVisibleItemIndex = if (expanded) position + 1 else position),
    )
}

@Composable
private fun SeeAllRow(position: Int) {
    val items = List(LongCount) { hubItem(it) }
    val titles = TvExpandingTitles(count = LongCount, at = { items.getOrNull(it) }, loops = false, trailing = true)
    TvExpandingRowBody(
        title = "",
        titles = titles,
        position = position,
        item = items[titles.index(position)],
        extras = { Extras },
        state = TvExpandingRowState(isFocused = true, expanded = true, arrival = { 1f }),
        strip = rememberLazyListState(initialFirstVisibleItemIndex = titles.shownPosition(position) + 1),
        seeAllLabel = "See all",
    )
}

@Composable
private fun SampleColumn(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize()) { content() }
}

/** At rest: bare posters under a large heading, the row's remembered title first. */
@Composable
fun TvExpandingRowRestingSample() {
    SampleColumn { Row(position = 0, expanded = false, isFocused = false) }
}

/** Focused: the slot holds the title the user stepped to, the posters after it, its details below, and the ring on the slot. */
@Composable
fun TvExpandingRowFocusedSample() {
    SampleColumn { Row(position = 2, expanded = true, isFocused = true, extras = Extras) }
}

/** A card whose action failed: the failure line takes the meta line's place, in the error colour, and the poster and ring are as before. */
@Composable
fun TvExpandingRowActionFailedSample() {
    SampleColumn { Row(position = 2, expanded = true, isFocused = true, extras = Extras, failure = "Couldn't approve") }
}

/** Back went to the top bar: the row stays open with no ring, as Netflix leaves it. */
@Composable
fun TvExpandingRowHeldOpenSample() {
    SampleColumn { Row(position = 2, expanded = true, isFocused = false, extras = Extras) }
}

/** The slot's name before the extras land: nothing, so a name never flashes ahead of the logo that replaces it. */
@Composable
fun TvExpandingRowExtrasLoadingSample() {
    SampleColumn { Row(position = 1, expanded = true, isFocused = true, extras = null) }
}

/** Extras with no logo: the name in text, the certification beside the rating. */
@Composable
fun TvExpandingRowWithoutLogoSample() {
    SampleColumn { Row(position = 1, expanded = true, isFocused = true, extras = Extras.copy(logoUrl = null)) }
}

/** A short row: the posters stop at its last title and the trailing spacer leaves room for them to slide to the slot. */
@Composable
fun TvExpandingShortRowAtLastTitleSample() {
    SampleColumn { Row(position = ShortCount - 1, expanded = true, isFocused = true, count = ShortCount, extras = Extras) }
}

/** The focused row mirrored: the slot, the strip's start and the previous poster's offset all swap sides. */
@Composable
fun TvExpandingRowRtlSample() {
    SampleColumn {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Row(position = 2, expanded = true, isFocused = true, extras = Extras)
        }
    }
}

/** The slot alone, focused: the ring is drawn round the backdrop, the name in text because the extras carry no logo. */
@Composable
fun TvExpandingSlotFocusedSample() {
    SampleColumn {
        TvExpandingSlot(
            item = hubItem(0),
            extras = {
                Extras.copy(logoUrl = null)
            },
            isFocused = true,
            width = dimensionResource(
                R.dimen.tv_expanding_sample_slot_width,
            ),
            height = dimensionResource(R.dimen.tv_expanding_row_height_focused),
        )
    }
}

/** The skeleton for a page of rows with no hero: the first row drawn open, over two resting ones. */
@Composable
fun TvExpandingHubLoadingSkeletonSample() {
    SampleColumn { TvExpandingHubLoadingSkeleton() }
}

/** The hub while it loads: the hero plate over placeholder rows, the same list the loaded hub fills in place. */
@Composable
fun TvExpandingHubLoadingSample() {
    SampleColumn {
        TvExpandingRowsHub(
            rows = emptyList<TvHubRow<TvHubItem>>(),
            item = { it },
            onItemClick = {},
            onRowItemClick = { _, _ -> },
            loading = true,
        )
    }
}

/** The hero plate with focus on it: the ring the loaded hero draws, round a card with no title yet. */
@Composable
fun TvExpandingHeroPlateFocusedSample() {
    SampleColumn { TvExpandingHeroCard(hero = null, extras = { null }, isFocused = true) }
}

/** A row whose titles are still loading holds its heading and its resting height, as placeholder posters. */
@Composable
fun TvExpandingPendingRowSample() {
    SampleColumn { TvExpandingPendingRow(title = "On your services") }
}

/** The meta line held to a width: the rating and certification always show, and genres drop from the end until the line fits. */
@Composable
fun TvExpandingMetaLineWidthsSample() {
    SampleColumn {
        listOf(
            dimensionResource(R.dimen.tv_sample_meta_wide),
            dimensionResource(R.dimen.tv_sample_meta_medium),
            dimensionResource(R.dimen.tv_sample_meta_narrow),
        ).forEach { width ->
            TvExpandingMeta(
                genres = listOf("Science Fiction", "Adventure"),
                trailingFacts = listOf("2024"),
                rating = 8.1f,
                certification = "12A",
                modifier = Modifier.width(width),
            )
        }
    }
}

/** A rated and an unrated title's meta lines, each on a tinted band: the bands match in height, so nothing below moves. */
@Composable
fun TvExpandingMetaLineRatedSample() {
    SampleColumn {
        listOf(
            8.1f,
            null,
        ).forEach { rating ->
            TvExpandingMeta(
                genres = listOf("Science Fiction", "Adventure"),
                trailingFacts = listOf("2024"),
                rating = rating,
                certification = null,
                modifier = Modifier
                    .width(
                        dimensionResource(R.dimen.tv_sample_meta_wide),
                    ).background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

/** The hero at rest: logo, meta, three-line synopsis bottom-left, the trending pill bottom-right, no ring. */
@Composable
fun TvExpandingHeroRestingSample() {
    SampleColumn { TvExpandingHeroCard(hero = Hero, extras = { Extras.copy(runtimeMinutes = 131) }, isFocused = false) }
}

/** The hero focused: the same slide with the ring drawn round the card. */
@Composable
fun TvExpandingHeroFocusedSample() {
    SampleColumn { TvExpandingHeroCard(hero = Hero, extras = { Extras.copy(runtimeMinutes = 131) }, isFocused = true) }
}

/** A series slide: the season count stands in for the runtime, and no logo means the name in text. */
@Composable
fun TvExpandingHeroSeriesSample() {
    SampleColumn {
        TvExpandingHeroCard(hero = Hero, extras = { Extras.copy(logoUrl = null, runtimeMinutes = null, seasons = 3) }, isFocused = false)
    }
}

/** The hero before its extras land: no logo, no name, no synopsis, only the meta the list entry already carries. */
@Composable
fun TvExpandingHeroExtrasLoadingSample() {
    SampleColumn { TvExpandingHeroCard(hero = Hero.copy(rank = null), extras = { null }, isFocused = false) }
}

/** The whole hub: the hero over a populated row, then a row still loading that holds its slot. */
@Composable
fun TvExpandingHubWithHeroSample() {
    SampleColumn {
        TvExpandingRowsHub(
            rows = PopularThenPending,
            item = { it },
            onItemClick = {},
            onRowItemClick = { _, _ -> },
            heroItems = listOf(Hero),
        )
    }
}

/** Without a hero the rows lead: a populated row, then one still loading that holds its slot. */
@Composable
fun TvExpandingHubRowsOnlySample() {
    SampleColumn { TvExpandingRowsHub(rows = PopularThenPending, item = { it }, onItemClick = {}, onRowItemClick = { _, _ -> }) }
}

/** A row that ends in a See all card, on its last title: the card waits beside the slot, poster-sized, where the next title would be. */
@Composable
fun TvExpandingSeeAllLastTitleSample() {
    SampleColumn { SeeAllRow(position = LongCount - 1) }
}

/** One step right: the slot keeps the last title, unringed, and the ring and scale go to the card beside it. */
@Composable
fun TvExpandingSeeAllFocusedSample() {
    SampleColumn { SeeAllRow(position = LongCount) }
}
