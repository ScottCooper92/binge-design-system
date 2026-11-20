package com.binge.designsystem.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeFastScrollbar
import com.binge.designsystem.component.BingeTabbedPager
import com.binge.designsystem.component.PersonGridCard
import com.binge.designsystem.component.PersonGridCardSkeleton
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchorIf
import com.binge.designsystem.mediaGridColumns

/** The lazy key of a section's heading. An app's own keys are text too, so it carries the reserved `binge:` prefix and cannot collide with them. */
private const val SECTION_KEY_PREFIX = "binge:people-grid-section:"

/** How many rows of plates the loading skeleton draws. */
const val PEOPLE_GRID_SKELETON_ROWS: Int = 4

/**
 * One person in a [PeopleGridScreen]. [key] is the app's identity for them, text so a lazy grid can save it, and is handed back to
 * `onOpenPerson`. It must be unique within its [PeopleGridSection]: a person who holds two jobs in one department is one entry there, with the jobs
 * merged into [role], because a lazy grid throws on a duplicate key. The same person may appear in two sections. [role] is the line under the name (a character, a job); [description] replaces the spoken "name, role" when the app words it itself.
 */
@Immutable
class PeopleGridPerson(
    val key: String,
    val name: String,
    val role: String?,
    val photoUrl: String?,
    val description: String? = null,
)

/**
 * A run of people under one heading, such as a department. A null [title] draws the people with no heading. [key] names the section for the
 * grid, so the same person may appear in two sections.
 */
@Immutable
class PeopleGridSection(
    val key: String,
    val title: String?,
    val people: List<PeopleGridPerson>,
)

/** What one tab of a [PeopleGridScreen] shows. */
@Immutable
sealed interface PeopleGridState {
    /** The people are on their way. */
    data object Loading : PeopleGridState

    /** The people, in [sections]. */
    class Ready(
        val sections: List<PeopleGridSection>,
    ) : PeopleGridState

    /** Nobody to list, said under its [icon] with an optional [action]. [title] null says "Nothing here yet". */
    class Empty(
        val message: String,
        val title: String? = null,
        val icon: ImageVector = Icons.Filled.PersonOff,
        val action: ScreenAction? = null,
    ) : PeopleGridState

    /** The load failed. [title] and [message] default to [kind]'s own words; Try again shows only when [retryable]. */
    class Failed(
        val kind: ErrorKind,
        val title: String? = null,
        val message: String? = null,
        val retryable: Boolean = true,
    ) : PeopleGridState
}

/**
 * One tab of a [PeopleGridScreen]: its [label], what it shows, and whether its fast scrollbar's bubble names the section the grid is in
 * ([sectionBubble], for a list long enough to be split into departments).
 */
@Immutable
class PeopleGridTab(
    val label: String,
    val state: PeopleGridState,
    val sectionBubble: Boolean = false,
)

/**
 * A screen of people in a grid of portrait cards, optionally split into tabs such as Cast and Crew: [BingeScreenScaffold] with a tab row under its
 * bar and one page per tab, swiped or tapped between. Each page is a skeleton, an empty or failed plate, or a grid of [PersonGridCard]s in
 * sections, each section with its own heading, with a fast scrollbar down the end edge for a long list. With one tab there is no tab row.
 *
 * Nothing here is an app's type: people are [PeopleGridPerson]s with a text key, and the app owns its loading and its selected tab. Each tab keeps its own
 * scroll position, so switching away and back finds the grid where it was left. Tabs are a different kind of content, so a screen uses tabs
 * here and never filter chips as well.
 *
 * The scrollbar's bubble names the section the grid is in for a tab with [PeopleGridTab.sectionBubble]; a tab with none draws a scrollbar with no bubble.
 *
 * @param loadingLabel what a screen reader says of the first loading plate; the app words it ("Loading cast").
 * @param onRetry asked for when a failed tab's Try again is pressed, with the index of that tab.
 *
 * @need show people as a grid of portrait cards, in sections, with tabs such as cast and crew
 * @group screens
 * @replaces a hand-built scaffold with tabs over a pager of person grids
 * @sample PeopleGridCastCrewSample
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleGridScreen(
    title: String,
    tabs: List<PeopleGridTab>,
    selectedTab: Int,
    onTabChange: (Int) -> Unit,
    onOpenPerson: (key: String) -> Unit,
    onRetry: (tab: Int) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    bar: ScreenBar = ScreenBar.Collapsing,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    loadingLabel: String = stringResource(R.string.paged_grid_loading),
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    overlays: @Composable () -> Unit = {},
) {
    require(tabs.isNotEmpty()) { "A people grid needs at least one tab." }
    // Held here, not left to the scaffold, so the tabs' scrim can follow the bar's own collapse.
    val behavior = scrollBehavior ?: rememberScreenBarScrollBehavior(bar)
    val scrim = screenBarScrim(bar, behavior)
    // One state per tab, kept here: a pager page leaves composition when it is two pages away, and its grid would otherwise lose its place.
    val gridStates = List(tabs.size) { rememberLazyGridState() }
    BingeScreenScaffold(
        title = title,
        modifier = modifier,
        onBack = onBack,
        bar = bar,
        scrollBehavior = behavior,
        snackbarHostState = snackbarHostState,
        actions = actions,
    ) { padding ->
        val direction = LocalLayoutDirection.current
        val listPadding = padding.screenListPadding()
        val page: @Composable (PaddingValues, Int) -> Unit = { pagePadding, index ->
            PeoplePage(
                tab = tabs[index],
                gridState = gridStates[index],
                contentPadding = PaddingValues(
                    start = listPadding.calculateStartPadding(direction),
                    top = pagePadding.calculateTopPadding() + dimensionResource(R.dimen.padding_s),
                    end = listPadding.calculateEndPadding(direction),
                    bottom = listPadding.calculateBottomPadding() + dimensionResource(R.dimen.padding_m),
                ),
                onOpenPerson = onOpenPerson,
                onRetry = { onRetry(index) },
                loadingLabel = loadingLabel,
            )
        }
        if (tabs.size == 1) {
            ScreenBody(padding) { page(PaddingValues(top = it.calculateTopPadding()), 0) }
        } else {
            BingeTabbedPager(
                tabs = tabs.map { it.label },
                selectedIndex = selectedTab.coerceIn(0, tabs.lastIndex),
                onSelectedIndexChange = onTabChange,
                modifier = Modifier.fillMaxSize().padding(padding.screenOuterPadding()).consumeWindowInsets(padding.screenOuterPadding()),
                // The bar's height joins the pager's header, so the grids reach the top of the window and pass under both.
                header = { Spacer(Modifier.height(padding.calculateTopPadding())) },
                headerBackground = Color.Transparent,
                scrimFraction = scrim,
                pageContent = page,
            )
        }
    }
    overlays()
}

@Composable
private fun PeoplePage(
    tab: PeopleGridTab,
    gridState: LazyGridState,
    contentPadding: PaddingValues,
    onOpenPerson: (String) -> Unit,
    onRetry: () -> Unit,
    loadingLabel: String,
) {
    when (val state = tab.state) {
        PeopleGridState.Loading -> PeopleGridSkeleton(contentPadding, loadingLabel)
        is PeopleGridState.Empty ->
            EmptyScreen(
                message = state.message,
                modifier = Modifier.padding(contentPadding),
                title = state.title ?: stringResource(R.string.empty_screen_title),
                icon = state.icon,
                action = state.action,
            )
        is PeopleGridState.Failed ->
            ErrorScreen(
                kind = state.kind,
                onRetry = onRetry.takeIf { state.retryable },
                modifier = Modifier.padding(contentPadding),
                title = state.title,
                message = state.message,
            )
        is PeopleGridState.Ready -> {
            val sectionAtIndex = remember(state.sections) { sectionTitlesByIndex(state.sections) }
            Box(Modifier.fillMaxSize()) {
                PeopleGridFrame(contentPadding, gridState) {
                    var first = true
                    state.sections.forEach { section ->
                        if (section.people.isEmpty()) return@forEach
                        if (section.title != null) {
                            item(key = SECTION_KEY_PREFIX + section.key, span = { GridItemSpan(maxLineSpan) }) {
                                SectionHeader(
                                    title = section.title,
                                    horizontalPadding = dimensionResource(R.dimen.zero),
                                    startPadding = dimensionResource(R.dimen.zero),
                                    endPadding = dimensionResource(R.dimen.zero),
                                )
                            }
                        }
                        section.people.forEach { person ->
                            val isFirst = first
                            first = false
                            item(key = section.key + "/" + person.key) {
                                PersonGridCard(
                                    name = person.name,
                                    role = person.role,
                                    photoUrl = person.photoUrl,
                                    onClick = { onOpenPerson(person.key) },
                                    modifier = Modifier.layoutAnchorIf(isFirst, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
                                    description = person.description,
                                )
                            }
                        }
                    }
                }
                BingeFastScrollbar(
                    state = gridState,
                    bubbleText = if (tab.sectionBubble) ({ index -> sectionAtIndex.getOrNull(index) }) else null,
                    // Inside the bar's and the navigation bar's own room, so the track neither runs under the bar nor the window's edge.
                    modifier = Modifier.padding(
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                )
            }
        }
    }
}

/** The title of the section each lazy item index belongs to, in the order [PeopleGridScreen] lays them out: a heading, then its people. */
internal fun sectionTitlesByIndex(sections: List<PeopleGridSection>): List<String?> =
    buildList {
        sections.forEach { section ->
            if (section.people.isEmpty()) return@forEach
            if (section.title != null) add(section.title)
            repeat(section.people.size) { add(section.title) }
        }
    }

/** The grid's frame, shared by the loaded grid and its skeleton so the columns, gaps and padding are one thing. */
@Composable
private fun PeopleGridFrame(
    contentPadding: PaddingValues,
    state: LazyGridState,
    content: LazyGridScope.() -> Unit,
) {
    LazyVerticalGrid(
        state = state,
        columns = GridCells.Fixed(mediaGridColumns()),
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        content = content,
    )
}

/** How many plates the skeleton draws at the current width: the grid's columns times [PEOPLE_GRID_SKELETON_ROWS]. */
@Composable
fun peopleGridSkeletonCells(): Int = mediaGridColumns() * PEOPLE_GRID_SKELETON_ROWS

@Composable
private fun PeopleGridSkeleton(contentPadding: PaddingValues, loading: String) {
    val cells = peopleGridSkeletonCells()
    PeopleGridFrame(contentPadding, rememberLazyGridState()) {
        items(cells) { index ->
            PersonGridCardSkeleton(
                modifier = Modifier.layoutAnchorIf(index == 0, LayoutAnchors.section(LayoutAnchors.Collection.FIRST_ITEM)),
                description = if (index == 0) loading else null,
            )
        }
    }
}
