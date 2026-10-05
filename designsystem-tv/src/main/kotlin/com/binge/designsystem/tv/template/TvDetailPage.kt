package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.TvArrivalFocus
import com.binge.designsystem.tv.focus.TvArrivalFocusEffect
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.tv.focus.tvArrivalObserver
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.tv.layout.TvLayoutAnchors
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.R as TvR

/**
 * The scrolling body every TV detail page shares: a hero band as the first item, content sections below,
 * and the page's one vertical-scroll contract baked in so a screen cannot re-derive it wrong.
 *
 * ## The contract
 *
 * **[TvStableFocusScroll] scopes the list**, so crossing sections never re-pivots the column on a child
 * that is already on screen — focus is colour, not geometry. Rows built on [TvCardRow]
 * restore the ambient parking for their own horizontal scroll, the split the immersive hub established.
 *
 * **[rememberAnchorKeeper] is the single vertical writer.** The focused section is pulled to a fixed rest:
 * the hero to the very top, every other section to `tv_detail_page_section_anchor_top` below it, animated
 * (a snap under reduce-motion). Two other shapes were tried and rejected: a
 * continuous `snapshotFlow` pin (which fought `bringIntoView` — a fight the immersive hub's anchor
 * scroll measured and rejected) and bare minimum-scroll (which leaves a ↓-entered
 * section flush against the bottom edge, ring clipped, with no peek saying the page continues).
 *
 * **Sections are tracked by key, not index.** Detail sections are conditional on data that can arrive
 * or vanish across a refresh (providers, videos, gallery), so a section can be inserted above the one
 * being browsed; an index would silently retarget the anchor at the wrong section — the hub's own
 * async-carousel lesson.
 *
 * ## Entry and seeding
 *
 * [entryFocus] rides the list as a [tvEntryFocusGroup] target: focus entering the group is routed to wherever
 * the caller attached the requester — the action row — instead of a geometric pick. A page is a template like
 * [TvBoard]: [hosting] decides who places first focus. As an overlay (how a detail page is shown) it claims focus
 * itself, once, on arrival; in a rail destination the shell offers it.
 *
 * [initialFocusedSectionKey] seeds both the anchor and the list position, which is what makes the
 * anchored (off-rest) states screenshot-testable: a static frame runs no coroutines, so a baseline
 * relying on the keeper's effect would quietly capture an unscrolled list.
 */
@Composable
fun TvDetailPage(
    modifier: Modifier = Modifier,
    entryFocus: FocusRequester? = null,
    hosting: TvPageHosting = currentTvPageHosting(),
    initialFocusedSectionKey: String? = null,
    listState: LazyListState? = null,
    contentPadding: PaddingValues = PaddingValues(bottom = dimensionResource(TvR.dimen.tv_detail_page_content_bottom)),
    // Gates rememberAnchorKeeper's corrective scroll below. Every real caller leaves this at its default;
    // TvDetailPageScrollContractTest sets it false to read rememberLazyListState's own restored
    // position with no chance of this correction supplying it instead.
    anchorCorrectionEnabled: Boolean = true,
    sections: TvDetailPageScope.() -> Unit,
) {
    val entries = TvDetailPageScope().apply(sections).entries
    if (entries.isEmpty()) return
    val anchorTopPx = with(LocalDensity.current) {
        dimensionResource(TvR.dimen.tv_detail_page_section_anchor_top).roundToPx()
    }
    // Saveable so a drill-down push-and-pop (which disposes this composition) restores the browsed
    // section rather than presenting the page back at rest.
    var focusedSectionKey by rememberSaveable { mutableStateOf(initialFocusedSectionKey) }
    // The focused cell within the section — an anchor re-key only, so a focus return that changes no
    // section (a sheet round trip, a rail round trip) still re-asserts the rest position.
    var focusedItemKey by remember { mutableStateOf<Any?>(null) }
    // Where an arriving focus lands, following the browse like the hub's entry slot follows its rows: the remembered section's
    // wrapper once the user has left the band, the caller's action row before. It rides the wrapper with a focusGroup to delegate
    // inward, so entry chains through the row's own entry group onto the cell TvRowEntry remembers — the card a pop returns to.
    val sectionEntry = remember { FocusRequester() }

    val initialIndex = entries.indexOfKeyOrZero(initialFocusedSectionKey)
    val state = listState ?: rememberLazyListState(
        initialFirstVisibleItemIndex = initialIndex,
        initialFirstVisibleItemScrollOffset = if (initialIndex == 0) 0 else -anchorTopPx,
    )

    val scrollTarget = entries.indexOfKeyOrZero(focusedSectionKey)
    val scrollOffset = if (scrollTarget == 0) 0 else -anchorTopPx
    val reduceMotion = LocalReduceMotion.current
    val reportBodyFocus = rememberAnchorKeeper(
        scrollTarget,
        focusedItemKey,
        shouldScroll = {
            anchorCorrectionEnabled &&
                (scrollTarget != state.firstVisibleItemIndex || scrollOffset != state.firstVisibleItemScrollOffset)
        },
    ) {
        if (reduceMotion) {
            state.scrollToItem(scrollTarget, scrollOffset)
        } else {
            state.animateScrollToItem(scrollTarget, scrollOffset)
        }
    }

    // Index 0 is the hero/at-rest case: there the caller's action-row requester is the deterministic
    // target (a bare wrapper delegation could land on the synopsis). Below it, the remembered section.
    val rememberedSectionKey = focusedSectionKey?.takeIf { entries.indexOfKeyOrZero(it) != 0 }
    val entryTarget = if (rememberedSectionKey != null) sectionEntry else entryFocus
    // A page that is not a rail destination places its own first focus. The claim aims at the list node, a target that
    // does not move as the user browses, and the entry group routes it on — to the action row on a fresh page, to the
    // remembered section once the user has been below it.
    val claimsFocus = hosting != TvPageHosting.RailDestination
    val pageArrival = remember { TvArrivalFocus(FocusRequester()) }
    if (claimsFocus) TvArrivalFocusEffect(pageArrival)
    TvStableFocusScroll {
        LazyColumn(
            state = state,
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_detail_page_section_gap)),
            modifier = modifier
                .fillMaxSize()
                .onFocusChanged(reportBodyFocus)
                .then(if (claimsFocus) Modifier.focusRequester(pageArrival.requester).tvArrivalObserver(pageArrival) else Modifier)
                .then(entryTarget?.let { Modifier.tvEntryFocusGroup(it) } ?: Modifier),
        ) {
            entries.forEach { entry ->
                item(key = entry.key) {
                    // `hasFocus` covers the whole subtree, so a section reports arrival with no new API; gains only — the loss
                    // belongs to whichever section gained. The remembered section's wrapper carries [sectionEntry] plus a focusGroup,
                    // so a request on it delegates inward — how a descendant-only group takes one (as the hub's hero item does).
                    Box(
                        modifier = Modifier
                            // So a skeleton's promise about this entry is assertable — see [TvLayoutAnchors].
                            .tvLayoutAnchor(TvLayoutAnchors.entry(entry.key))
                            .onFocusChanged { if (it.hasFocus) focusedSectionKey = entry.key }
                            .then(
                                if (entry.key == rememberedSectionKey) {
                                    Modifier.focusRequester(sectionEntry).focusGroup()
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        entry.content { itemKey -> focusedItemKey = itemKey }
                    }
                }
            }
        }
    }
}

/** Collects a detail page's items: one [hero] first, then a [section] per content row. */
class TvDetailPageScope internal constructor() {
    internal val entries = mutableListOf<TvDetailPageEntry>()

    /** The opening band. First in the list and anchored to the very top when it holds focus. */
    fun hero(key: String = TvLayoutAnchors.HERO_KEY, content: @Composable () -> Unit) {
        entries += TvDetailPageEntry(key) { content() }
    }

    /**
     * A content section, anchored to `tv_detail_page_section_anchor_top` when it holds focus. [content]
     * receives a reporter to chain into the row's per-cell focus callback ([TvCardRow.onCellFocused],
     * [TvMediaRow]'s `onItemFocused`) so a sideways move re-asserts the anchor.
     */
    fun section(key: String, content: @Composable (onItemFocused: (Any) -> Unit) -> Unit) {
        entries += TvDetailPageEntry(key, content)
    }
}

internal class TvDetailPageEntry(
    val key: String,
    val content: @Composable (onItemFocused: (Any) -> Unit) -> Unit,
)

private fun List<TvDetailPageEntry>.indexOfKeyOrZero(key: String?): Int =
    key?.let { k -> indexOfFirst { it.key == k } }?.takeIf { it >= 0 } ?: 0
