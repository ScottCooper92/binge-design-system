package com.binge.designsystem.tv.template

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.logTvHandoffMiss
import com.binge.designsystem.tv.template.TvHubRow

private const val NO_KEY = ""

/**
 * Where focus is in the expanding hub: the row it last took and whether the user has pressed a key yet (both kept across recreation,
 * as the hub is rebuilt when a page opened from it closes), and which way focus last moved. [entry] is the one requester arriving focus (startup, or ↓ from the top bar) is aimed at,
 * so it goes to the row last focused, else the first, never wherever a 2D search lands.
 */
@Stable
internal class TvExpandingHubFocus(
    private val listState: LazyListState,
    savedKey: String?,
    userMoved: Boolean = false,
    nextKey: String? = null,
) {
    val entry = FocusRequester()
    var savedKey by mutableStateOf(savedKey)
        private set

    /** The row below [savedKey] when it took focus (above it for the last row), where entry goes if that row has gone. */
    var nextKey by mutableStateOf(nextKey)
        private set
    var userMoved by mutableStateOf(userMoved)
    var hubHasFocus by mutableStateOf(false)

    /** The focused row's index in the list, -1 until a row takes focus. */
    var focusedRowIndex by mutableIntStateOf(-1)
        private set

    /** Whether focus last moved down the list: a row arrived at from above settles in briefly, one from below is just open. */
    var movedDown by mutableStateOf(true)
        private set

    fun onFocused(
        key: String,
        index: Int,
        nextKey: String? = null,
    ) {
        savedKey = key
        this.nextKey = nextKey
        // The first row to take focus in this composition (startup, or the return from a page opened from the hub) pins at once.
        movedDown = focusedRowIndex >= 0 && index > focusedRowIndex
        // Up arrives with no glide: pinned at the next layout, in the frame focus moves, so no frame shows the gap above.
        if (!movedDown) listState.requestScrollToItem(index)
        focusedRowIndex = index
    }

    /** Whether the head of the list is now [firstKey] and nothing the user did says otherwise: focus is in, no key yet, on another row. */
    fun shouldFollowHeadTo(firstKey: String): Boolean = hubHasFocus && !userMoved && savedKey != firstKey

    /**
     * Forgets the remembered row and its neighbour, so entry follows the head of the list again, and owes the entry one request made when
     * its row is placed, unless the user has pressed a key by then. Both keys go: a kept [nextKey] would aim entry at the row below.
     */
    fun followHead() {
        savedKey = null
        nextKey = null
        yieldsToUser = true
        awaitingEntry = true
    }

    /** Whether the owed entry request is dropped if the user has pressed a key since it was owed (head-follow), as it never pulls focus back. */
    private var yieldsToUser = false

    /** Whether a request for the entry is owed and waiting for the entry row to be placed. */
    var awaitingEntry = false
        private set

    /**
     * Owes the entry one request, made when the entry row is placed: a lazy row composes during measure, after any effect, so
     * a request made earlier lands on the row that is going away. Whatever key the user pressed before, a row that went under
     * focus still owes its neighbour focus.
     */
    fun aimAtEntry() {
        yieldsToUser = false
        awaitingEntry = true
    }

    /** Called as the entry row is placed. Makes the owed request, once. */
    fun entryPlaced() {
        if (!awaitingEntry) return
        awaitingEntry = false
        if (yieldsToUser && userMoved) return
        runCatching { entry.requestFocus() }
    }

    companion object {
        fun saver(listState: LazyListState) =
            listSaver<TvExpandingHubFocus, Any>(
                save = { listOf(it.savedKey ?: NO_KEY, it.userMoved, it.nextKey ?: NO_KEY) },
                restore = { saved ->
                    TvExpandingHubFocus(
                        listState,
                        savedKey = (saved[0] as String).takeIf { it != NO_KEY },
                        userMoved = saved[1] as Boolean,
                        nextKey = (saved[2] as String).takeIf { it != NO_KEY },
                    )
                },
            )
    }
}

/** [initialKey] seeds the row focus is on, for a screenshot, which dispatches no focus events; production passes null. */
@Composable
internal fun rememberTvExpandingHubFocus(listState: LazyListState, initialKey: String? = null): TvExpandingHubFocus {
    val focus = rememberSaveable(saver = TvExpandingHubFocus.saver(listState)) { TvExpandingHubFocus(listState, savedKey = initialKey) }
    val context = LocalContext.current
    DisposableEffect(focus) { onDispose { if (focus.awaitingEntry) context.logTvHandoffMiss("the hub left with its entry unplaced") } }
    return focus
}

/**
 * Rows fill in any order, so one can arrive above the row startup focus took. Until a key is pressed, entry follows the head of the
 * list: when [firstKey] changes it re-aims focus there, with one request when that row is placed, and scrolls the list back to the top,
 * unless a key is pressed first.
 */
@Composable
internal fun TvExpandingHubHeadFollow(
    focus: TvExpandingHubFocus,
    firstKey: String?,
    listState: LazyListState,
) {
    LaunchedEffect(firstKey) {
        if (firstKey == null || !focus.shouldFollowHeadTo(firstKey)) return@LaunchedEffect
        focus.followHead()
        if (!focus.userMoved) listState.scrollToItem(0)
    }
}

/**
 * A failed row whose retry holds focus is swapped for its titles when the retry works, and the retry's node goes with it. This sees
 * that swap as it is composed, while focus is still on the retry, and owes the hub's entry (that row's key) one focus request, made
 * when the replacing row is placed, so focus lands on the row that replaced the retry rather than nowhere.
 */
@Composable
internal fun TvExpandingRecoveredRowFocus(rows: List<TvHubRow<TvHubItem>>, focus: TvExpandingHubFocus) {
    val failedKeys = rows.filter { it.items.isEmpty() && it.failure != null }.mapTo(HashSet()) { it.key }
    val lastFailed = remember { SeenKeys() }
    val recovered = focus.savedKey?.takeIf { focus.hubHasFocus && it in lastFailed.keys && it !in failedKeys }
    // Owed as the swap is applied, before the new row is laid out: a request recorded in a later effect finds it already placed.
    SideEffect {
        lastFailed.keys = failedKeys
        if (recovered != null) focus.aimAtEntry()
    }
}

/**
 * The focused row leaving the hub (a "Continue watching" emptied while focus is on it) takes its node with it. This sees that as it
 * is composed, while focus is still there, and aims focus at the entry, which the gone row hands to [TvExpandingHubFocus.nextKey],
 * so focus stays in the rows rather than falling to the top bar or the hero.
 */
@Composable
internal fun TvExpandingVanishedRowFocus(rowKeys: List<String>, focus: TvExpandingHubFocus) {
    val lastShown = remember { SeenKeys() }
    val shown = rowKeys.toHashSet()
    val vanished = focus.savedKey?.takeIf { focus.hubHasFocus && it in lastShown.keys && it !in shown }
    SideEffect {
        lastShown.keys = shown
        if (vanished != null) focus.aimAtEntry()
    }
}

/** Row keys as they were at the last composition; a plain field, read only to spot a row that changed. */
private class SeenKeys {
    var keys: Set<String> = emptySet()
}

/** The list follows the focused row to the top: instantly with reduced motion, else a short glide, and not at all moving up. */
@Composable
internal fun TvExpandingFollowFocusedRow(focus: TvExpandingHubFocus, listState: LazyListState) {
    val reduceMotion = LocalReduceMotion.current
    val index = focus.focusedRowIndex
    LaunchedEffect(index) {
        if (index < 0) return@LaunchedEffect
        when {
            !focus.movedDown -> Unit
            reduceMotion -> listState.scrollToItem(index)
            else -> listState.followToTop(index)
        }
    }
}

/**
 * The row arriving focus is aimed at: the one last focused if it is still there, else the row beside it ([nextKey]), so a row that
 * emptied hands focus on rather than back to the top, else the hero, else the first row.
 */
internal fun tvExpandingEntryKey(
    savedKey: String?,
    hasHero: Boolean,
    rowKeys: List<String>,
    nextKey: String? = null,
): String? {
    val saved = savedKey?.takeIf { key -> if (key == TV_EXPANDING_HERO_KEY) hasHero else key in rowKeys }
    return saved ?: nextKey?.takeIf { it in rowKeys } ?: if (hasHero) TV_EXPANDING_HERO_KEY else rowKeys.firstOrNull()
}

/** Whether the hero slot was shown at the last composition; a plain field, read only to spot the slot leaving. */
private class HeroSlotSeen(
    var shown: Boolean,
)

private class OverlaySeen(
    var open: Boolean,
)

/**
 * Hands focus to the hub's entry where nothing else would: when the hub mounts over the plate that held it ([arriving]: the error
 * or empty plate a retry replaced), and when the hero slot leaves while focus is on it (a trending list that came back empty). Both
 * are read in the composition that removes the node, while focus is still on it, and place focus once, not on a clock.
 */
@Composable
internal fun TvExpandingEntryFocus(
    arriving: Boolean,
    hasHeroSlot: Boolean,
    focus: TvExpandingHubFocus,
) {
    val seen = remember { HeroSlotSeen(hasHeroSlot) }
    val onHero = focus.savedKey.let { it == null || it == TV_EXPANDING_HERO_KEY }
    val heroLeft = seen.shown && !hasHeroSlot && focus.hubHasFocus && onHero
    var arrivals by remember { mutableIntStateOf(if (arriving) 1 else 0) }
    SideEffect {
        seen.shown = hasHeroSlot
        if (heroLeft) arrivals++
    }
    if (arrivals > 0) TvOverlayArrivalFocusEffect(focus.entry, key = arrivals)
}

/**
 * Brings focus back to the hub's entry when an overlay opened from it ([overlayOpen]) is gone, so Back from a card's sheet lands on
 * the card it was opened from, or its neighbour where that card left meanwhile. Like the arrivals above it is read in the composition
 * that removes the overlay, and places focus once.
 */
@Composable
internal fun TvExpandingOverlayReturn(overlayOpen: Boolean, focus: TvExpandingHubFocus) {
    val seen = remember { OverlaySeen(overlayOpen) }
    var returns by remember { mutableIntStateOf(0) }
    SideEffect {
        if (seen.open && !overlayOpen) returns++
        seen.open = overlayOpen
    }
    if (returns > 0) TvOverlayArrivalFocusEffect(focus.entry, key = returns)
}
