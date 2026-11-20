package com.binge.designsystem.tv.template

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import coil3.compose.AsyncImage
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SCRIM_ALPHA = 0.92f
private const val EXPANDED_SCRIM_ALPHA = 0.85f
private const val SIDE_SCRIM_END = 0.65f
internal const val DETAIL_SCRIM_START = 0.45f
internal const val DETAIL_SWAP_MILLIS = 220

/** Whether arrival at rest lands on the action row: a page that has one, or a loading page that holds placeholders for one. */
private fun restsOnActions(
    hasActions: Boolean,
    loading: Boolean,
    loadingHasActions: Boolean,
): Boolean = hasActions || (loading && loadingHasActions)

/** The focus requesters of a page's tabs by key, kept for as long as the page so a tab that outlives a change of tab list keeps its own. */
private class TabRequesterPool {
    private val byKey = mutableMapOf<String, FocusRequester>()

    fun of(key: String): FocusRequester = byKey.getOrPut(key) { FocusRequester() }
}

/** One tab along the page's foot: a stable [key], its [label], and what it shows once the page is expanded onto it. */
class TvDetailTab(
    val key: String,
    val label: String,
    val content: @Composable () -> Unit,
)

/** The title as the page draws it: name, logo, backdrop, synopsis, and the meta line's parts (the [facts] after the genres). */
class TvDetailTitle(
    val title: String,
    val logoUrl: String?,
    val backdropUrl: String?,
    val overview: String,
    val genres: List<String>,
    val facts: List<String>,
    val rating: Float?,
    val certification: String?,
    /** A portrait right- and top-aligned at rest, in place of a backdrop: a 2:3 photo cropped to 16:9 loses the face. */
    val portraitUrl: String? = null,
    /** The photo's left-edge colour when it is already known (a frame's seed); darkened as a sample is, and sampled from the photo when null. */
    val portraitTint: Color? = null,
)

/**
 * Marks a page whose title is still loading: the copy is drawn as plates, over the tabs the caller gives, which stay focusable and are
 * the nodes the loaded tabs reuse. The action row is the caller's, drawn as placeholders in the same composable that draws the real
 * row, so the primary the user is on is the node that becomes the real button. OK and ↓ on a tab do nothing until the title is in.
 */
object TvDetailLoading

/** The loading page's tabs: a stable [TvDetailTab.key] and label each, with nothing behind them yet. */
fun tvDetailLoadingTabs(vararg keyedLabels: Pair<String, String>): List<TvDetailTab> =
    keyedLabels.map { (key, label) -> TvDetailTab(key, label) {} }

/** The title of a page that has none yet. */
val TV_DETAIL_LOADING_TITLE = TvDetailTitle("", null, null, "", emptyList(), emptyList(), null, null)

/**
 * A title's page: the backdrop, the copy, an action row and tabs that expand over it.
 *
 * At rest the copy sits bottom-left with the [actions] row and the [tabs] along the foot. ↓ or OK on a tab expands the page, moving
 * the tabs to the top over that tab's content (reported through [onTabShown]). ↑ from the top tabs, or Back, folds it.
 * [initialExpanded] and [initialTabKey] seed the saved state, for a frame.
 * Focus is offered once, as the page mounts: to the open tab's content when expanded, else the actions' primary slot (the real
 * primary, drawn busy while [loading]), else the tabs. Nothing offers it again.
 *
 * @need show a title's page with its copy, an action row and tabs that expand over the backdrop on a tv screen
 * @group tv
 * @replaces a hand-built tv detail page with its own expand, fold and focus rules
 * @sample TvTitleDetailPageSample
 *
 * @param initialFocusedTabKey draws the tab with this key focused, for a frame: a frame fires no focus events.
 * @param loadingHasActions false for a page with no action row, so its loading state rests on the tabs, not on action placeholders.
 * @param backdropModel maps the title's backdrop URL to what the image loader is given: an app that wants a larger size for the
 *   10-foot screen resizes it here. The default uses the URL as it is.
 * @param portraitFallbackUrl a smaller form of the portrait to try when it fails to load, or null for none.
 */
@Composable
fun TvTitleDetailPage(
    title: TvDetailTitle,
    tabs: List<TvDetailTab>,
    actionsFocus: FocusRequester,
    hasActions: Boolean,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    initialTabKey: String? = null,
    initialFocusedTabKey: String? = null,
    onTabShown: (key: String) -> Unit = {},
    loading: TvDetailLoading? = null,
    loadingHasActions: Boolean = true,
    backdropModel: (String?) -> String? = { it },
    portraitFallbackUrl: (String) -> String? = { null },
    actions: @Composable () -> Unit,
) {
    var expandedState by rememberSaveable { mutableStateOf(initialExpanded) }
    var selectedKey by rememberSaveable { mutableStateOf(initialTabKey ?: tabs.firstOrNull()?.key) }
    val selected = tabs.firstOrNull { it.key == selectedKey } ?: tabs.firstOrNull()
    val isLoading = loading != null
    // A loading page has no content to be open onto, so it rests whatever it was showing, and stays folded when the title returns.
    val expanded = expandedState && !isLoading
    LaunchedEffect(isLoading) { if (isLoading) expandedState = false }
    // Kept across the title loading in, so a tab that is in both lists is the same node and keeps its focus.
    val requesterPool = remember { TabRequesterPool() }
    val tabRequesters = tabs.associate { it.key to requesterPool.of(it.key) }
    val tabsEntry = selected?.let { tabRequesters[it.key] } ?: remember { FocusRequester() }
    // Where arrival lands, including the return from a page opened here (the image viewer, a person): the open tab's content, which
    // routes to the item it last focused; at rest, the actions. The open tab and each tab's own memory are saved, so both survive.
    val contentFocus = remember { FocusRequester() }
    TvOverlayArrivalFocusEffect(
        target = if (expanded) {
            contentFocus
        } else if (restsOnActions(hasActions, isLoading, loadingHasActions)) {
            actionsFocus
        } else {
            tabsEntry
        },
        key = Unit,
    )
    // A tab's content is shown once the page is expanded onto it; the report is deduplicated by its consumer, as a row's is.
    val currentOnTabShown by rememberUpdatedState(onTabShown)
    LaunchedEffect(expanded, selected?.key) {
        if (expanded) selected?.key?.let(currentOnTabShown)
    }
    // The strip is one node moved between the layouts, and the move disposes the node focus was on, so a fold asks for its tab once,
    // in the composition that places it in the resting layout.
    var foldEpoch by remember { mutableIntStateOf(0) }
    LaunchedEffect(foldEpoch) {
        if (foldEpoch > 0) runCatching { tabsEntry.requestFocus() }
    }
    val fold = {
        expandedState = false
        foldEpoch++
    }
    BackHandler(enabled = expanded) { fold() }
    // One movable node, read through state rather than parameters, so its requesters never go stale.
    val currentTabs by rememberUpdatedState(tabs)
    val isLoadingNow by rememberUpdatedState(isLoading)
    val currentRequesters by rememberUpdatedState(tabRequesters)
    val strip = remember {
        movableContentOf {
            TvDetailTabStrip(
                tabs = currentTabs,
                selectedKey = selectedKey,
                expanded = expandedState && !isLoadingNow,
                requesters = currentRequesters,
                onExpand = { key ->
                    if (!isLoadingNow) {
                        selectedKey = key
                        expandedState = true
                    }
                },
                onFocusTab = { key -> if (expandedState) selectedKey = key },
                onUpFromTop = { fold() },
                focusedKey = initialFocusedTabKey,
            )
        }
    }
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TvDetailBackdrop(title.backdropUrl, expanded, backdropModel)
        title.portraitUrl?.let { TvDetailPortrait(it, title.portraitTint, expanded, portraitFallbackUrl) }
        TvDetailLayout(expanded) { isExpanded ->
            if (isExpanded) {
                // The tabs take the overscan; the content does not, as the rows and sections in it pad themselves to the gutter.
                Column(
                    Modifier.fillMaxSize().padding(vertical = dimensionResource(TvR.dimen.tv_overscan_vertical)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l)),
                ) {
                    Box(Modifier.padding(start = tvContentGutterStart())) { strip() }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .focusRequester(contentFocus)
                            .focusGroup(),
                    ) { selected?.content?.invoke() }
                }
            } else {
                TvDetailResting(title, tabsEntry, loading, actions, strip)
            }
        }
    }
}

@Composable
private fun TvDetailBackdrop(
    backdropUrl: String?,
    expanded: Boolean,
    backdropModel: (String?) -> String?,
) {
    if (backdropUrl != null) {
        AsyncImage(
            model = backdropModel(backdropUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    }
    val scrim = if (expanded) {
        Brush.horizontalGradient(0f to Color.Black.copy(alpha = SCRIM_ALPHA), 1f to Color.Black.copy(alpha = EXPANDED_SCRIM_ALPHA))
    } else {
        Brush.horizontalGradient(0f to Color.Black.copy(alpha = SCRIM_ALPHA), SIDE_SCRIM_END to Color.Transparent)
    }
    Box(Modifier.fillMaxSize().background(scrim))
    if (!expanded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(DETAIL_SCRIM_START to Color.Transparent, 1f to Color.Black.copy(alpha = SCRIM_ALPHA)),
                ),
        )
    }
}

/** Crossfades between the two layouts, snapping with reduced motion. */
@Composable
private fun TvDetailLayout(expanded: Boolean, content: @Composable (isExpanded: Boolean) -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    AnimatedContent(
        targetState = expanded,
        transitionSpec = {
            val spec = if (reduceMotion) snap<Float>() else tween(DETAIL_SWAP_MILLIS)
            fadeIn(spec) togetherWith fadeOut(spec)
        },
        label = "tvDetailExpanded",
    ) { isExpanded -> content(isExpanded) }
}

/** The resting layout: the copy, the actions (↓ reaches the tabs) and the tab strip, bottom-left under the overscan. */
@Composable
private fun TvDetailResting(
    title: TvDetailTitle,
    tabsEntry: FocusRequester,
    loading: TvDetailLoading?,
    actions: @Composable () -> Unit,
    strip: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(
                start = tvContentGutterStart(),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                top = dimensionResource(TvR.dimen.tv_overscan_vertical),
                bottom = dimensionResource(TvR.dimen.tv_overscan_vertical),
            ),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        Spacer(Modifier.weight(1f))
        if (loading == null) TvDetailCopy(title) else TvDetailCopyPlates()
        Box(Modifier.focusProperties { down = tabsEntry }.focusGroup()) {
            actions()
        }
        strip()
    }
}
