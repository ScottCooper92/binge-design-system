package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.component.DetailHeroSkeleton
import com.binge.designsystem.component.DetailOverviewSection
import com.binge.designsystem.component.DetailOverviewSkeleton
import com.binge.designsystem.component.MediaResult
import com.binge.designsystem.component.MediaResultKind
import com.binge.designsystem.component.MediaResultRow
import com.binge.designsystem.component.MediaResultRowSkeleton
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.resolvedContentInset

/**
 * What a collection's page shows, as plain values: a series of titles that belong together, such as a film franchise. Every word is already in
 * the user's language, including [metaText] (a count the app words for the plural). A part the app has nothing for is left out.
 *
 * @property backdropUrl the image behind the hero; with none the hero is the plain scrim
 * @property overviewHeading the heading over [overview]; null uses this library's word for it
 * @property partsHeading the heading over the parts; null draws no heading
 * @property parts the titles, in the order they are drawn. With none the page is [emptyTitle] and [emptyMessage] over the back control.
 */
@Immutable
class CollectionDetail(
    val title: String,
    val backdropUrl: String? = null,
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
    /** A block of prose when [hasOverview], and [parts] rows of placeholders, in the loaded page's own room. */
    @Immutable
    class Loading(
        val hasOverview: Boolean = true,
        val parts: Int = 4,
    ) : CollectionDetailState

    @Immutable
    class Content(
        val collection: CollectionDetail,
    ) : CollectionDetailState

    /** [title] and [message] default to the words for [kind]; [retryable] shows Try again. */
    @Immutable
    class Failed(
        val kind: ErrorKind = ErrorKind.Network,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
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
                    if (collection != null) {
                        DetailHero(
                            title = collection.title,
                            backdropUrl = collection.backdropUrl,
                            tagline = null,
                            metaText = collection.metaText,
                            onBack = {},
                            showChrome = false,
                            richBackdrop = true,
                        )
                    } else {
                        DetailHeroSkeleton(cinematic = false, tagline = null, genres = emptyList(), metaText = " ")
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
    collection.partsHeading?.let { SectionHeader(title = it, horizontalPadding = readingInset(), startPadding = readingInset()) }
    Column(
        Modifier.fillMaxWidth().readingMargin().padding(horizontal = resolvedContentInset()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        collection.parts.forEachIndexed { index, part ->
            MediaResultRow(
                result = part,
                onClick = { onOpen(part.key) },
                modifier = if (index == 0) Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)) else Modifier,
                trailing = partTrailing?.invoke(part),
            )
        }
    }
}

@Composable
private fun ColumnScope.CollectionPlaceholder(state: CollectionDetailState.Loading) {
    if (state.hasOverview) DetailOverviewSkeleton()
    Column(
        Modifier.fillMaxWidth().readingMargin().padding(horizontal = resolvedContentInset()),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
    ) {
        repeat(state.parts) { index ->
            MediaResultRowSkeleton(
                modifier = if (index == 0) Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)) else Modifier,
                kind = MediaResultKind.Poster,
            )
        }
    }
}
