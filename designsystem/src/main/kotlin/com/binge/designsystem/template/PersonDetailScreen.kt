package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLoadingIndicator
import com.binge.designsystem.component.CarouselSkeleton
import com.binge.designsystem.component.DetailOverviewSkeleton
import com.binge.designsystem.component.ExpandableOverview
import com.binge.designsystem.component.MediaCard
import com.binge.designsystem.component.PersonFact
import com.binge.designsystem.component.PersonProfileHeader
import com.binge.designsystem.component.PersonProfileSkeleton
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.navOverlayEnd
import com.binge.designsystem.resolvedContentInset
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/** How many cards from the end of a credits row the next page is asked for. */
const val PERSON_CREDITS_LOAD_MORE_AHEAD: Int = 3

/**
 * One row of a person's credits, such as "Acting" or "Crew". [items] are the same [GridItem]s a poster grid takes, with the person's role as the
 * subtitle. With [hasMore] the row asks for its next page near its end; [appending] and [appendFailed] draw a spinner or a retry tile after the last card.
 * A section with no items is not drawn. [key] is stable for the section.
 */
@Immutable
class CreditSection(
    val key: String,
    val title: String,
    val items: List<GridItem>,
    val hasMore: Boolean = false,
    val appending: Boolean = false,
    val appendFailed: Boolean = false,
)

/** What a person's credits are doing. The rest of the page shows whatever state they are in. */
sealed interface CreditsState {
    /** The credits are on their way: two placeholder rows. */
    data object Loading : CreditsState

    /** The credits failed; the page stays. [message] defaults to this library's words; [retryable] shows a Try again button. */
    @Immutable
    class Failed(
        val message: String? = null,
        val retryable: Boolean = true,
    ) : CreditsState

    /** The credits, as rows. */
    @Immutable
    class Ready(
        val sections: List<CreditSection>,
    ) : CreditsState
}

/**
 * What a person's page shows, as plain values: no app, server or media-database type. Every word is already in the user's language. A part the app
 * has nothing for is left out: no [biography], no section.
 *
 * @property eyebrow a line above the name in capitals, such as the department they are known for
 * @property biographyHeading the heading over [biography]; null uses this library's word for it
 * @property refreshing a pull to refresh is under way
 */
@Immutable
class PersonDetail(
    val name: String,
    val photoUrl: String? = null,
    val eyebrow: String? = null,
    val facts: List<PersonFact> = emptyList(),
    val biography: String? = null,
    val biographyHeading: String? = null,
    val credits: CreditsState = CreditsState.Loading,
    val refreshing: Boolean = false,
)

/** The three states of a person's page. [Failed] keeps the back control, so the user is never stranded. */
sealed interface PersonDetailState {
    /** [facts] lines and [creditRails] rows of placeholders, [hasBiography] a block of prose, in the loaded page's own room. */
    @Immutable
    class Loading(
        val facts: Int = 3,
        val hasBiography: Boolean = true,
        val creditRails: Int = 2,
    ) : PersonDetailState

    @Immutable
    class Content(
        val person: PersonDetail,
    ) : PersonDetailState

    /** [title] and [message] default to the words for [kind]; [retryable] shows Try again. */
    @Immutable
    class Failed(
        val kind: ErrorKind = ErrorKind.Network,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
    ) : PersonDetailState
}

/**
 * What the page does with what the user does. [onOpenCredit] is handed the tapped card's [GridItem.key]; [onLoadMoreCredits] the index of the
 * [CreditSection] that wants a page; [onRetryCredits] the credits' Try again; [onRetry] the whole page's. A non-null [onRefresh] lets the
 * page be pulled down.
 */
@Immutable
class PersonDetailCallbacks(
    val onBack: (() -> Unit)?,
    val onRetry: () -> Unit,
    val onOpenCredit: (key: String) -> Unit,
    val onLoadMoreCredits: (sectionIndex: Int) -> Unit = {},
    val onRetryCredits: () -> Unit = {},
    val onRefresh: (() -> Unit)? = null,
)

/**
 * A person's page: a profile (portrait, name, facts), a biography, and their credits as rows of posters, on the hero page's bar and reading column.
 * It holds no view model and no media-database type. While [state] is [PersonDetailState.Loading] it draws placeholders in the loaded page's room;
 * when it fails it keeps the back control over the message.
 *
 * Slots: [topBarActions] sit in the bar (a share button; they are handed the bar's glass alpha), [afterCredits] follows the credits (links, in the app's
 * words), and [overlays] holds anything drawn over the page.
 *
 * @need build a person's page with a profile, a biography and rows of credits
 * @group screens
 * @replaces a hand-built hero page of a portrait, text and credit rows
 */
@Composable
fun PersonDetailScreen(
    state: PersonDetailState,
    callbacks: PersonDetailCallbacks,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    topBarActions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    afterCredits: @Composable ColumnScope.() -> Unit = {},
    overlays: @Composable () -> Unit = {},
) {
    when (state) {
        is PersonDetailState.Failed ->
            HeroDetailStateScreen(onBack = callbacks.onBack, modifier = modifier, snackbarHostState = snackbarHostState) {
                ErrorScreen(
                    kind = state.kind,
                    onRetry = callbacks.onRetry.takeIf { state.retryable },
                    modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                    title = state.title,
                    message = state.message,
                )
            }
        else -> {
            val person = (state as? PersonDetailState.Content)?.person
            HeroDetailScreen(
                title = person?.name.orEmpty(),
                onBack = callbacks.onBack,
                modifier = modifier,
                snackbarHostState = snackbarHostState,
                contentMaxWidth = Dp.Infinity,
                darkStatusBar = false,
                onRefresh = callbacks.onRefresh.takeIf { person != null },
                isRefreshing = person?.refreshing == true,
                pullIndicator = HeroPullIndicator.InBar,
                actions = topBarActions,
                hero = {
                    Spacer(
                        Modifier
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .height(dimensionResource(R.dimen.person_profile_top_bar_offset)),
                    )
                },
            ) {
                if (person !=
                    null
                ) {
                    PersonContent(person, callbacks, afterCredits)
                } else {
                    PersonPlaceholder(state as PersonDetailState.Loading)
                }
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_l)))
            }
        }
    }
    overlays()
}

@Composable
private fun ColumnScope.PersonContent(
    person: PersonDetail,
    callbacks: PersonDetailCallbacks,
    afterCredits: @Composable ColumnScope.() -> Unit,
) {
    PersonProfileHeader(name = person.name, photoUrl = person.photoUrl, eyebrow = person.eyebrow, facts = person.facts)
    person.biography?.let { biography ->
        Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
        Column(Modifier.readingMargin().padding(horizontal = resolvedContentInset())) {
            Text(
                text = person.biographyHeading ?: stringResource(R.string.person_biography_heading),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_s)),
            )
            ExpandableOverview(text = biography)
        }
    }
    when (val credits = person.credits) {
        CreditsState.Loading -> CreditsPlaceholder()
        is CreditsState.Failed -> CreditsFailed(credits, callbacks.onRetryCredits)
        is CreditsState.Ready ->
            credits.sections.forEachIndexed { index, section ->
                if (section.items.isNotEmpty()) {
                    CreditRail(section, onOpen = callbacks.onOpenCredit, onLoadMore = { callbacks.onLoadMoreCredits(index) })
                }
            }
    }
    afterCredits()
}

@Composable
private fun ColumnScope.PersonPlaceholder(state: PersonDetailState.Loading) {
    PersonProfileSkeleton(facts = state.facts)
    if (state.hasBiography) {
        Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
        DetailOverviewSkeleton()
    }
    repeat(state.creditRails) { CarouselSkeleton(Modifier.padding(top = dimensionResource(R.dimen.padding_m))) }
}

@Composable
private fun CreditsPlaceholder() {
    repeat(2) { CarouselSkeleton(Modifier.padding(top = dimensionResource(R.dimen.padding_m))) }
}

@Composable
private fun CreditsFailed(failed: CreditsState.Failed, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().readingMargin().padding(
            horizontal = resolvedContentInset(),
            vertical = dimensionResource(R.dimen.padding_m),
        ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        Text(
            text = failed.message ?: stringResource(R.string.person_credits_failed),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (failed.retryable) {
            TextButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text(
                    stringResource(R.string.error_kind_retry),
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_s)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun CreditRail(
    section: CreditSection,
    onOpen: (String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val inset = readingInset()
    val listState = rememberLazyListState()
    val nearEnd by remember(listState) {
        derivedStateOf {
            val info = listState.layoutInfo
            isNearEnd(info.visibleItemsInfo.lastOrNull()?.index ?: 0, info.totalItemsCount, PERSON_CREDITS_LOAD_MORE_AHEAD)
        }
    }
    LaunchedEffect(listState, section.hasMore, section.items.size) {
        if (section.hasMore) snapshotFlow { nearEnd }.distinctUntilChanged().filter { it }.collect { onLoadMore() }
    }
    Column(Modifier.fillMaxWidth()) {
        SectionHeader(title = section.title, horizontalPadding = inset, startPadding = inset, endPadding = inset + navOverlayEnd())
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(start = inset, end = inset + navOverlayEnd()),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
        ) {
            itemsIndexed(section.items, key = { _, item -> item.key }) { _, item ->
                MediaCard(
                    posterUrl = item.posterUrl,
                    title = item.title,
                    rating = item.rating,
                    onClick = { onOpen(item.key) },
                    modifier = Modifier.width(dimensionResource(R.dimen.card_width)),
                    status = item.status,
                    progress = item.progress,
                    subtitle = item.subtitle,
                    favourite = item.favourite,
                    watched = item.watched,
                    unwatchedCount = item.unwatchedCount,
                )
            }
            if (section.appending || section.appendFailed) {
                item(key = "append") {
                    Box(
                        Modifier.width(dimensionResource(R.dimen.see_all_tile_width)).height(dimensionResource(R.dimen.card_height)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (section.appendFailed) {
                            TextButton(onClick = onLoadMore) {
                                Icon(Icons.Filled.Refresh, contentDescription = null)
                                Text(stringResource(R.string.list_more_failed), style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            BingeLoadingIndicator()
                        }
                    }
                }
            }
        }
    }
}
