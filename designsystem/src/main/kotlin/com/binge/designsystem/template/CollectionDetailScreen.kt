package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailCinematicHeader
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailHeroSkeleton
import com.binge.designsystem.component.DetailOverviewSection
import com.binge.designsystem.component.DetailOverviewSkeleton
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.component.MediaResultRow
import com.binge.designsystem.component.MediaResultRowSkeleton
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.component.SkeletonPlate
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.navOverlayEnd
import com.binge.designsystem.resolvedContentInset

/**
 * What a collection's page shows, as plain values: a series of titles that belong together, such as a film franchise. Every word is already in
 * the user's language. A part the app has nothing for is left out.
 *
 * @property backdropUrl the image behind the hero
 * @property posterUrl the collection's poster. At an expanded width the hero draws it beside the title, as the movie page does; and where there is no
 * [backdropUrl] it is the picture behind the hero at any width, since a hand-made box set often has only a poster. With neither, the hero is the plain wash.
 * @property metaText the line under the title: a runtime or a genre line, or a count the app words for the plural
 * @property overviewHeading the heading over [overview]; null uses this library's word for it
 * @property partsHeading the heading over the parts; null or blank draws no heading, and the loading state's [CollectionDetailState.Loading.hasPartsHeading] is
 * `!partsHeading.isNullOrBlank()`: the skeleton reserves a heading only when this page will draw one
 * @property parts the titles, in the order they are drawn. With none the page is [emptyTitle] and [emptyMessage] over the back control.
 */
@Immutable
class CollectionDetail(
    val title: String,
    val backdropUrl: String? = null,
    val posterUrl: String? = null,
    val metaText: String = "",
    val overview: String? = null,
    val overviewHeading: String? = null,
    val partsHeading: String? = null,
    val parts: List<MediaResult> = emptyList(),
    val emptyTitle: String? = null,
    val emptyMessage: String = "",
)

/** The three states of a collection's page. [Failed] keeps the back control, so the user is never stranded. */
sealed interface CollectionDetailState {
    /**
     * A block of prose when [hasOverview], a heading row when [hasPartsHeading], and [parts] rows of placeholders, in the loaded page's own room. [hasPoster] says the
     * loaded hero will have a poster beside the title at an expanded width, so the placeholder is that taller header. [hasPartsHeading] says the loaded page will draw
     * a heading over its parts, which is when [CollectionDetail.partsHeading] has words; the heading is a row of its own, so a skeleton that reserves one for a page that draws none
     * starts the parts that much too low, and one that does not for a page that does starts them that much too high. Left false, as by default, the skeleton draws none.
     * [hasOverviewToggle] says the loaded overview will run past its three lines and so carry the expand toggle's row; false for an overview that is short, which has none, so
     * the parts are not left that much too low. It is true by default, as a long overview is the usual one, and applies only with [hasOverview].
     */
    @Immutable
    class Loading(
        val hasOverview: Boolean = true,
        val parts: Int = 4,
        val hasPoster: Boolean = false,
        val hasPartsHeading: Boolean = false,
        val hasOverviewToggle: Boolean = true,
    ) : CollectionDetailState

    @Immutable
    class Content(
        val collection: CollectionDetail,
    ) : CollectionDetailState

    /** [title] and [message] default to the words for [kind]; [retryable] shows Try again; [secondary] is a second action beside it. */
    @Immutable
    class Failed(
        val kind: ErrorKind = ErrorKind.Network,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
        val secondary: ScreenAction? = null,
    ) : CollectionDetailState
}

/** What the page does with what the user does. [onOpenPart] is handed the tapped part's [MediaResult.key]. A non-null [onRefresh] lets the page be pulled down. */
@Immutable
class CollectionDetailCallbacks(
    val onBack: (() -> Unit)?,
    val onRetry: () -> Unit,
    val onOpenPart: (key: String) -> Unit,
    val onRefresh: (() -> Unit)? = null,
)

/**
 * A collection's page: a hero with the title and a count, an expandable overview, and the parts as rows with their year and a short blurb. It holds
 * no view model and no media-database type. While loading it draws placeholders in the loaded page's room; when it fails, or has no parts, it keeps the
 * back control over the message.
 *
 * Slots: [partTrailing] gives a part's status (an availability chip), drawn first on its status line, and may return null to draw none; [footer] is
 * pinned under the scroll (a "request all" button); [topBarActions] sit in the bar and are handed its glass alpha (an overflow menu); [overlays]
 * holds anything drawn over the page (a sheet, a confirm).
 *
 * @need build a collection's page with a hero, an overview and its parts as rows
 * @group screens
 * @replaces a hand-built hero page of a collection and a list of its titles
 * @sample CollectionDetailReadySample
 */
@Composable
fun CollectionDetailScreen(
    state: CollectionDetailState,
    callbacks: CollectionDetailCallbacks,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    partTrailing: ((MediaResult) -> (@Composable () -> Unit)?)? = null,
    footer: (@Composable () -> Unit)? = null,
    topBarActions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    overlays: @Composable () -> Unit = {},
) {
    val collection = (state as? CollectionDetailState.Content)?.collection
    when {
        state is CollectionDetailState.Failed ->
            HeroDetailStateScreen(onBack = callbacks.onBack, modifier = modifier, snackbarHostState = snackbarHostState) {
                ErrorScreen(
                    kind = state.kind,
                    onRetry = callbacks.onRetry.takeIf { state.retryable },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                    title = state.title,
                    message = state.message,
                    secondary = state.secondary,
                )
            }
        collection != null && collection.parts.isEmpty() ->
            HeroDetailStateScreen(onBack = callbacks.onBack, modifier = modifier, snackbarHostState = snackbarHostState) {
                val emptyTitle = collection.emptyTitle
                EmptyScreen(
                    message = collection.emptyMessage,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                    title = emptyTitle ?: stringResource(R.string.empty_screen_title),
                )
            }
        else ->
            HeroDetailScreen(
                title = collection?.title.orEmpty(),
                onBack = callbacks.onBack,
                modifier = modifier,
                snackbarHostState = snackbarHostState,
                contentMaxWidth = Dp.Infinity,
                onRefresh = callbacks.onRefresh.takeIf { collection != null },
                actions = topBarActions,
                footer = footer,
                hero = {
                    // The expanded header is the one with a poster beside the title; a collection with no poster keeps the plain hero.
                    val wide = isExpandedLayout()
                    if (collection == null) {
                        val hasPoster = (state as? CollectionDetailState.Loading)?.hasPoster == true
                        DetailHeroSkeleton(cinematic = wide && hasPoster, tagline = null, genres = emptyList(), metaText = " ")
                    } else if (wide && collection.posterUrl != null) {
                        DetailCinematicHeader(
                            title = collection.title,
                            genres = listOfNotNull(collection.metaText.takeIf { it.isNotBlank() }),
                            synopsis = null,
                            stats = emptyList(),
                            backdropUrl = collection.backdropUrl,
                            posterUrl = collection.posterUrl,
                        )
                    } else {
                        DetailHero(
                            title = collection.title,
                            backdropUrl = collection.backdropUrl ?: collection.posterUrl,
                            tagline = null,
                            metaText = collection.metaText,
                            onBack = {},
                            showChrome = false,
                            richBackdrop = true,
                        )
                    }
                },
            ) {
                if (collection != null) {
                    CollectionContent(collection, callbacks.onOpenPart, partTrailing)
                } else {
                    CollectionPlaceholder(state as CollectionDetailState.Loading)
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_l)))
            }
    }
    overlays()
}

@Composable
private fun ColumnScope.CollectionContent(
    collection: CollectionDetail,
    onOpen: (String) -> Unit,
    partTrailing: ((MediaResult) -> (@Composable () -> Unit)?)?,
) {
    DetailOverviewSection(
        heading = collection.overviewHeading ?: stringResource(R.string.detail_overview_heading),
        text = collection.overview,
    )
    // A blank heading is no heading, as null is: a row of nothing would only push the parts down, and the skeleton ([CollectionDetailState.Loading.hasPartsHeading])
    // reserves a heading only for words.
    val partsHeading = collection.partsHeading?.takeIf { it.isNotBlank() }
    partsHeading?.let { SectionHeader(title = it, horizontalPadding = readingInset(), startPadding = readingInset()) }
    // A heading brings its own padding; without one the first row would sit hard under the overview.
    if (partsHeading == null && collection.overview != null) Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
    Column(
        Modifier.fillMaxWidth().readingMargin().padding(horizontal = resolvedContentInset()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        collection.parts.forEachIndexed { index, part ->
            MediaResultRow(
                result = part,
                onClick = { onOpen(part.key) },
                modifier = rowAnchor(index),
                trailing = partTrailing?.invoke(part),
            )
        }
    }
}

@Composable
private fun ColumnScope.CollectionPlaceholder(state: CollectionDetailState.Loading) {
    if (state.hasOverview) {
        DetailOverviewSkeleton(
            Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW)),
            withToggle = state.hasOverviewToggle,
        )
    }
    if (state.hasPartsHeading) {
        PartsHeadingPlate()
    } else if (state.hasOverview) {
        // As the loaded page's own: without a heading, the first row sits one gap under the overview.
        Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
    }
    Column(
        Modifier.fillMaxWidth().readingMargin().padding(horizontal = resolvedContentInset()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        repeat(state.parts) { index ->
            MediaResultRowSkeleton(
                modifier = rowAnchor(index),
                kind = MediaResultKind.Poster,
                blurbLines = PART_BLURB_LINES,
            )
        }
    }
}

/** The loading stand-in for the parts' [SectionHeader]: its row, at its padding and its touch-target height, with a plate where the heading's words go. */
@Composable
private fun PartsHeadingPlate() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = readingInset(), end = readingInset() + navOverlayEnd())
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
}

/** A part row's blurb runs up to this many lines, so its placeholder holds as many. */
private const val PART_BLURB_LINES = 3

/** The anchors on the first two rows, loaded or loading, so a test can compare where they stand and how tall they are. */
private fun rowAnchor(index: Int): Modifier =
    when (index) {
        0 -> Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM))
        1 -> Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Collection.SECOND_ITEM))
        else -> Modifier
    }
