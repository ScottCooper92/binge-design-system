package com.binge.designsystem.template

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.CarouselSkeleton
import com.binge.designsystem.component.DetailActions
import com.binge.designsystem.component.DetailActionsSkeleton
import com.binge.designsystem.component.DetailHeroSkeleton
import com.binge.designsystem.component.DetailInfoSection
import com.binge.designsystem.component.DetailOverviewSection
import com.binge.designsystem.component.DetailOverviewSkeleton
import com.binge.designsystem.component.DetailPeopleRail
import com.binge.designsystem.component.DetailPosterRail
import com.binge.designsystem.component.DetailRoundAction
import com.binge.designsystem.component.DetailStatsRows
import com.binge.designsystem.component.DetailStatsRowsSkeleton
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.component.TagRow
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor

/**
 * A movie's page, assembled once for every app: the hero (the cinematic header at the expanded width), the stats, the actions, the
 * overview, the rails of people and of posters, the tags and the facts, in that order, with the spacing, the reading margin and the
 * expanded-width rule held here. At the expanded width the header already carries the stats and the synopsis, so the body leaves both
 * out. The page takes plain values ([DetailState], [MediaDetail]) and reports every tap through [callbacks], so it holds no navigation,
 * and the app maps its own state to the model.
 *
 * [DetailState.Loading] draws the page's own skeleton, parameterised by how many stats, round actions and rails the loaded page will have and whether it has a primary action, so
 * the hero, the stats, the actions and the overview land where the loaded page puts them. [DetailState.Failed] replaces the page on the
 * loading page's own frame, so the Back control does not move, with Try again when the failure is retryable.
 *
 * What differs between apps comes in through slots, each optional and taking no space when empty: [primary] is the wide action (a
 * request in one app, Play in another) and [roundActions] the round buttons beside or under it (with no [primary] they sit alone, centred in one row); [topBarActions] are the bar's actions;
 * [afterPeople], [afterRails] and [atEnd] are an app's own sections at three named places (where to watch after the people, links
 * after the rails and tags, media information at the end); [overlays] hosts the app's sheets, in the loaded
 * page only: none is hosted while it loads or after it fails. [snackbarHostState] shows the app's messages.
 *
 * @need assemble a movie's detail page from plain values, with its loading and failed states
 * @group detail
 * @replaces a hand-built movie page that lists the detail sections, its skeleton and its error layout
 * @sample MovieDetailScreenSample
 */
@Composable
fun MovieDetailScreen(
    state: DetailState,
    callbacks: MediaDetailCallbacks,
    modifier: Modifier = Modifier,
    primary: (@Composable (Modifier) -> Unit)? = null,
    roundActions: List<DetailRoundAction> = emptyList(),
    topBarActions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    afterPeople: (@Composable () -> Unit)? = null,
    afterRails: (@Composable () -> Unit)? = null,
    atEnd: (@Composable () -> Unit)? = null,
    overlays: (@Composable () -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
) {
    MediaDetailScreen(
        state,
        callbacks,
        modifier,
        primary,
        roundActions,
        topBarActions,
        afterPeople,
        afterRails,
        atEnd,
        overlays,
        snackbarHostState,
        scrollState,
    )
}

/** The page every title kind shares: a movie passes no [episodes], a series passes its episodes section, drawn after the people and the first named slot. */
@Composable
internal fun MediaDetailScreen(
    state: DetailState,
    callbacks: MediaDetailCallbacks,
    modifier: Modifier = Modifier,
    primary: (@Composable (Modifier) -> Unit)? = null,
    roundActions: List<DetailRoundAction> = emptyList(),
    topBarActions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    afterPeople: (@Composable () -> Unit)? = null,
    afterRails: (@Composable () -> Unit)? = null,
    atEnd: (@Composable () -> Unit)? = null,
    overlays: (@Composable () -> Unit)? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    scrollState: ScrollState = rememberScrollState(),
    episodes: (@Composable () -> Unit)? = null,
) {
    when (state) {
        is DetailState.Loading -> DetailPageSkeleton(state, callbacks.onBack, snackbarHostState, modifier)
        is DetailState.Failed -> DetailPageFailure(state, callbacks, snackbarHostState, modifier)
        is DetailState.Content -> {
            val detail = state.detail
            MediaHeroDetailPage(
                title = detail.title,
                backdropUrl = detail.backdropUrl,
                metaText = detail.metaText,
                onBack = callbacks.onBack,
                modifier = modifier,
                snackbarHostState = snackbarHostState,
                scrollState = scrollState,
                topBarActions = topBarActions,
                genres = detail.genres,
                tagline = detail.tagline,
                wideHeader = MediaWideHeader(detail.posterUrl, detail.overview, detail.stats),
                externalScores = detail.scores,
            ) {
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
                DetailPageBody(detail, callbacks, primary, roundActions, afterPeople, afterRails, atEnd, episodes)
            }
            overlays?.invoke()
        }
    }
}

/** What the page says below its hero. A part the app gave nothing for is not drawn. */
@Composable
private fun DetailPageBody(
    detail: MediaDetail,
    callbacks: MediaDetailCallbacks,
    primary: (@Composable (Modifier) -> Unit)?,
    roundActions: List<DetailRoundAction>,
    afterPeople: (@Composable () -> Unit)?,
    afterRails: (@Composable () -> Unit)?,
    atEnd: (@Composable () -> Unit)?,
    episodes: (@Composable () -> Unit)?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
        // The expanded header already shows the stats and the synopsis.
        val expanded = isExpandedLayout()
        if (!expanded && detail.stats.isNotEmpty()) DetailStatsRows(detail.stats)
        if (primary != null || roundActions.isNotEmpty()) {
            DetailActions(
                primary = primary,
                roundActions = roundActions,
                modifier = Modifier.readingMargin().layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.ACTIONS)),
            )
        }
        if (!expanded) {
            DetailOverviewSection(detail.overviewHeading ?: stringResource(R.string.detail_overview_heading), detail.overview)
        }
        detail.peopleRails.forEachIndexed { index, rail ->
            DetailPeopleRail(
                title = rail.title,
                people = rail.people,
                itemKey = { it.key },
                person = { it.person },
                onOpen = { callbacks.onOpenPerson(it.key) },
                onSeeAll = if (rail.seeAll) ({ callbacks.onSeeAllPeople(index) }) else null,
                cardStyle = rail.cardStyle,
            )
        }
        afterPeople?.invoke()
        episodes?.invoke()
        detail.posterRails.forEachIndexed { index, rail ->
            DetailPosterRail(
                title = rail.title,
                items = rail.items,
                itemKey = { it.key },
                poster = { it.poster },
                onOpen = { callbacks.onOpenPoster(it.key) },
                onSeeAll = if (rail.seeAll) ({ callbacks.onSeeAllPosters(index) }) else null,
            )
        }
        detail.tags?.takeIf { it.tags.isNotEmpty() }?.let { section ->
            Column {
                SectionHeader(section.title, horizontalPadding = readingInset())
                TagRow(
                    tags = section.tags.map { it.label },
                    modifier = Modifier.padding(horizontal = readingInset()),
                    compact = true,
                    onTagClick = if (section.tappable) ({ index -> callbacks.onOpenTag(section.tags[index].key) }) else null,
                )
            }
        }
        afterRails?.invoke()
        detail.info?.let { DetailInfoSection(it.heading, it.entries) }
        atEnd?.invoke()
    }
}

/**
 * The page while it loads, on the same frame as the loaded page: the bar with its Back control is there from the first frame, and the
 * hero, the stats, the actions and the overview land where the page puts them. Only those are held to the page; the overview plates are drawn even
 * for a title that arrives with no synopsis, and what is below the overview depends on what the app returns, so it grows on arrival.
 */
@Composable
private fun DetailPageSkeleton(
    state: DetailState.Loading,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
) {
    val cinematic = isExpandedLayout()
    HeroDetailScreen(
        title = "",
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        heroHeight = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_height else R.dimen.detail_hero_height),
        horizontalInset = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_padding else R.dimen.padding_m),
        contentMaxWidth = Dp.Infinity,
        darkStatusBar = false,
        hero = { DetailHeroSkeleton(cinematic) },
    ) {
        Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
        Column(Modifier.clearAndSetSemantics {}, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
            if (!cinematic) {
                DetailStatsRowsSkeleton(Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.STATS)), count = state.stats)
            }
            // The loaded page draws its action row only when it has something to put in it, so the skeleton does too: an empty row still
            // takes a gap from the column's spacing, and the overview would move up when the page arrives.
            if (state.hasPrimary || state.roundActions > 0) {
                DetailActionsSkeleton(
                    Modifier.readingMargin().layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.ACTIONS)),
                    buttons = state.roundActions,
                    primary = state.hasPrimary,
                )
            }
            if (!cinematic) {
                DetailOverviewSkeleton(
                    Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW)),
                    withToggle = state.hasOverviewToggle,
                )
            }
            repeat(state.rails) { CarouselSkeleton(contentPadding = PaddingValues(horizontal = readingInset())) }
        }
    }
}

/**
 * The failure in place of the page, on the same frame as the loading page so the back control stays where it was, announcing the whole
 * message, with Try again where another attempt can help.
 */
@Composable
private fun DetailPageFailure(
    state: DetailState.Failed,
    callbacks: MediaDetailCallbacks,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
) {
    val cinematic = isExpandedLayout()
    HeroDetailStateScreen(
        onBack = callbacks.onBack,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        horizontalInset = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_padding else R.dimen.padding_m),
    ) {
        ErrorScreen(
            kind = state.kind,
            onRetry = callbacks.onRetry.takeIf { state.retryable },
            modifier = Modifier.fillMaxSize(),
            title = state.title,
            message = state.message,
            secondary = state.secondary,
        )
    }
}
