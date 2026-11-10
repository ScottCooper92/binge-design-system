package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import com.binge.designsystem.tv.focus.TvOverlayCloser
import com.binge.designsystem.tv.focus.tryRequestFocus
import com.binge.designsystem.tv.focus.tvLateEntryFocusGroup

internal data class StackEntry(
    val page: String,
    val openedBy: String?,
    val parentTitle: String? = null,
)

/**
 * One page of a [TvSideSheetStack]'s path: the [page] name, [openedBy], the key of the row that opened it (null for the
 * root), [parentTitle], the title of the page below it that Back returns to, and [focusedRow], the row that last had focus
 * on it. A host keeps these from [TvSideSheetStackState.path] and hands them back as `startPath`.
 */
data class TvSideSheetStep(
    val page: String,
    val openedBy: String? = null,
    val parentTitle: String? = null,
    val focusedRow: String? = null,
)

/**
 * The pages open in a [TvSideSheetStack], for a host that reopens the stack where the user was. Make one with
 * [rememberTvSideSheetStackState] and pass it to the stack.
 */
@Stable
class TvSideSheetStackState internal constructor(
    internal val entries: SnapshotStateList<StackEntry>,
    private val focusedRows: MutableMap<String, String>,
) {
    /**
     * The pages open, the root first, each with the row that last had focus on it. Reading it in composition follows a page
     * opening or closing, and not each move of focus, so read it when it is wanted, to save it before a sheet row leaves for
     * another screen.
     */
    val path: List<TvSideSheetStep>
        get() = entries.map { TvSideSheetStep(it.page, it.openedBy, it.parentTitle, focusedRows[it.page]) }

    internal fun focusedRow(page: String): String? = focusedRows[page]

    internal fun recordFocus(page: String, row: String) {
        focusedRows[page] = row
    }

    internal fun forget(page: String) {
        focusedRows.remove(page)
    }

    internal companion object {
        private const val ENTRY_SEPARATOR = '\u0001'

        val StateSaver =
            Saver<TvSideSheetStackState, List<String>>(
                save = { state ->
                    state.path.map {
                        listOf(it.page, it.openedBy.orEmpty(), it.parentTitle.orEmpty(), it.focusedRow.orEmpty())
                            .joinToString(ENTRY_SEPARATOR.toString())
                    }
                },
                restore = { saved ->
                    seeded(
                        saved.map { line ->
                            val fields = line.split(ENTRY_SEPARATOR)
                            TvSideSheetStep(
                                page = fields[0],
                                openedBy = fields.getOrNull(1)?.takeIf { it.isNotEmpty() },
                                parentTitle = fields.getOrNull(2)?.takeIf { it.isNotEmpty() },
                                focusedRow = fields.getOrNull(3)?.takeIf { it.isNotEmpty() },
                            )
                        },
                    )
                },
            )

        /** A state holding [path]; one that does not start at the root is not one, and is the empty path. */
        fun seeded(path: List<TvSideSheetStep>, root: String? = null): TvSideSheetStackState {
            val entries = mutableStateListOf<StackEntry>()
            val focusedRows = mutableMapOf<String, String>()
            val usable = path.takeIf { it.isNotEmpty() && (root == null || it.first().page == root) }.orEmpty()
            for (step in usable) {
                if (entries.any { it.page == step.page }) break
                entries.add(StackEntry(step.page, step.openedBy.takeIf { entries.isNotEmpty() }, step.parentTitle))
                step.focusedRow?.let { focusedRows[step.page] = it }
            }
            if (entries.isEmpty() && root != null) entries.add(StackEntry(root, null))
            return TvSideSheetStackState(entries, focusedRows)
        }
    }
}

/**
 * The state of a [TvSideSheetStack] that opens on [root], or, with a [startPath] that came from an earlier
 * [TvSideSheetStackState.path], on the page that path ended on. [startPath] is used when the state is first made, and later
 * the state is restored as the saved state is: it survives a configuration change and the app's process dying. A path that
 * does not begin at [root] is ignored, and the stack opens on [root], as it does for an empty one.
 *
 * Hoist the state above the sheet to keep the path past the sheet closing.
 */
@Composable
fun rememberTvSideSheetStackState(root: String, startPath: List<TvSideSheetStep> = emptyList()): TvSideSheetStackState =
    rememberSaveable(root, saver = TvSideSheetStackState.StateSaver) { TvSideSheetStackState.seeded(startPath, root) }

/** The focus requesters of one page's rows, by the key each row gave itself. */
internal class RowRequesters(
    private val restoreKey: () -> String? = { null },
) {
    private val requesters = mutableMapOf<String, FocusRequester>()
    private var claimed = false
    private val placedKeys = mutableSetOf<String>()
    private val refused = mutableSetOf<String>()
    var first: String? = null
        private set
    var arrival: String? = null

    fun requesterFor(key: String): FocusRequester {
        if (first == null) first = key
        return requesters.getOrPut(key) { FocusRequester() }
    }

    fun contains(key: String) = key in requesters

    private fun targetKey(restoreTo: String?): String? = restoreTo?.takeIf(::contains) ?: arrival ?: first

    private fun candidates(): List<String> = listOfNotNull(restoreKey()?.takeIf(::contains), arrival, first).distinct()

    fun target(restoreTo: String?): FocusRequester? = targetKey(restoreTo)?.let(requesters::get)

    /** Gives focus to the arrival row, else the first when that one refuses it. Returns whether a row took it. */
    fun focusEntry(): Boolean = listOfNotNull(arrival, first).distinct().any { requesters[it]?.tryRequestFocus() == true }

    /**
     * Called as the row [key] is placed. The page's target row takes focus the first time it is, once and only once, so the
     * page that opens or comes back to is never first seen with focus on some other row. A target that refuses focus (a row
     * disabled meanwhile) passes it to the next candidate, the arrival row and then the first, once that one is placed. Only
     * when every candidate refuses does focus stay on the anchor.
     */
    fun placed(key: String) {
        placedKeys += key
        if (claimed) return
        for (candidate in candidates()) {
            if (candidate !in placedKeys) return
            if (candidate in refused) continue
            if (requesters.getValue(candidate).tryRequestFocus()) {
                claimed = true
                return
            }
            refused += candidate
        }
    }
}

/**
 * A [TvSideSheetStack] page's handle: which [page] is showing, and how to open another over it or go back. Give every
 * row [row] with its own key, and the stack puts focus where it belongs as pages come and go.
 */
@Stable
class TvSideSheetNav internal constructor(
    val page: String,
    /** How many pages are open, the root being one: for a caller that words its first page differently from the rest. */
    val depth: Int,
    private val onPush: (page: String, openedBy: String) -> Unit,
    private val onBack: () -> Unit,
    private val requesters: RowRequesters,
    private val onRowFocused: (row: String) -> Unit = {},
) {
    /** Opens [page] over this one. [openedBy] is the key of the row that opened it, which takes focus back on [pop]. */
    fun push(page: String, openedBy: String) = onPush(page, openedBy)

    /** Goes back one page, or closes the sheet from the first. */
    fun pop() = onBack()

    /**
     * Marks a row so the stack can focus it, and goes on its `modifier`. Keys are unique within a page. The first row to
     * ask is where focus lands when the page opens, unless [arriveAt] names another. The row that last had focus is kept
     * in the stack's [TvSideSheetStackState.path].
     */
    fun row(key: String): Modifier =
        Modifier
            .focusRequester(requesters.requesterFor(key))
            .onFocusChanged { if (it.hasFocus) onRowFocused(key) }
            .onGloballyPositioned { requesters.placed(key) }

    /**
     * Where focus lands when this page opens, in place of its first row: the Cancel of a confirm, say, which
     * [TvSideSheetConfirm] takes as `cancelModifier = nav.row("cancel")` with a null `entryFocus`.
     */
    fun arriveAt(key: String) {
        requesters.arrival = key
    }

    /**
     * Puts focus where this page puts it when it opens: its arrival row, else its first when the arrival row refuses focus. For a page whose focused row has
     * gone (an answer replaced, an action done) and which has no row of its own to hand focus to. Returns whether a row took it.
     */
    fun focusEntry(): Boolean = requesters.focusEntry()
}

/**
 * A [TvSideSheet] of pages that open one over another, like a settings list whose rows each open their own list. The
 * root [root] page shows first. A row opens another with
 * [TvSideSheetNav.push]; Back, or the key pointing away from the panel, goes back one page, and from the root calls
 * [onDismissRequest], where the caller returns focus to what opened the sheet, with `rememberTvOverlayCloser`.
 *
 * Focus follows the pages. A page that opens puts focus on its first row, and going back puts it on the row that opened the
 * page just left, so the user is where they were. Each page keeps its own saved state while a page is open over it, so a
 * list scrolled down comes back where it was, the default scroll of [TvSideSheetRows] included; a page gone back from forgets
 * its own. Pages are keyed by name, so the stack survives a rotation, and no page
 * opens another of the same name; what a page shows is the caller's to read from its own
 * state. [content] draws the page named by [TvSideSheetNav.page], and gives every row [TvSideSheetNav.row]. Pass the
 * [closer] the caller closes it with, so it lets focus go as the sheet closes and returns it once the sheet is gone. To reopen
 * where the user was, use the overload that takes a [TvSideSheetStackState].
 */
@Composable
fun TvSideSheetStack(
    root: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
    closer: TvOverlayCloser? = null,
    content: @Composable ColumnScope.(nav: TvSideSheetNav) -> Unit,
) {
    TvSideSheetStack(
        state = rememberTvSideSheetStackState(root),
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        toBottomEdge = toBottomEdge,
        closer = closer,
        content = content,
    )
}

/**
 * A [TvSideSheetStack] that keeps its pages in [state], so a host can read where the user was
 * ([TvSideSheetStackState.path]) and reopen the stack there ([rememberTvSideSheetStackState]'s `startPath`): after a screen
 * that a sheet row opened comes back, say, without pushing its pages again from an effect. The stack opens on the last page
 * of the path, with focus on the row that last had it there, or on the page's entry row when that row is gone. Back from
 * that page goes to the one below it, with focus on the row that opened it, as it does for a stack the user opened.
 *
 * The stack does not check the page names it is given: [content] must draw every name that a path holds. A page that was
 * not open when the path was saved has no saved scroll position, so a restored list starts at the top and focus scrolls the
 * remembered row into view.
 *
 * The root-only overload is this one with a state it makes for itself.
 */
@Composable
fun TvSideSheetStack(
    state: TvSideSheetStackState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
    closer: TvOverlayCloser? = null,
    content: @Composable ColumnScope.(nav: TvSideSheetNav) -> Unit,
) {
    val entries = state.entries
    val pageStates = rememberSaveableStateHolder()
    // A page that opens with a remembered row, from a restored path, puts focus there as a page it went back to does.
    var restoreTo by remember { mutableStateOf(state.focusedRow(entries.last().page)) }
    // The title each page shows, read only when a page opens another, so the new page can name the one Back returns to.
    val titles = remember { mutableMapOf<String, String>() }
    // Focus rests here while one page's rows replace another's, so no row takes it by chance as the old ones dispose.
    val anchor = remember { FocusRequester() }
    val back: () -> Unit = {
        if (entries.size > 1) {
            anchor.tryRequestFocus()
            val left = entries.removeAt(entries.lastIndex)
            restoreTo = left.openedBy
            pageStates.removeState(left.page)
            state.forget(left.page)
        } else {
            onDismissRequest()
        }
    }
    // The sheet's window takes focus before the page's effect runs; routing entry to the target keeps it off the first row.
    var currentRows by remember { mutableStateOf<RowRequesters?>(null) }
    val routed = modifier.tvLateEntryFocusGroup { currentRows?.target(restoreTo) }
    TvSideSheetHost(onDismissRequest = back, modifier = routed, toBottomEdge = toBottomEdge, closer = closer, anchor = anchor) {
        val column = this
        val top = entries.last()
        key(top.page) {
            pageStates.SaveableStateProvider(top.page) {
                val rows = remember { RowRequesters { restoreTo } }
                currentRows = rows
                val page = remember(top.page, top.parentTitle) { TvSideSheetPage(top.parentTitle) { titles[top.page] = it } }
                val nav =
                    TvSideSheetNav(
                        page = top.page,
                        depth = entries.size,
                        onPush = { page, openedBy ->
                            anchor.tryRequestFocus()
                            restoreTo = null
                            entries.add(StackEntry(page, openedBy, titles[top.page]))
                        },
                        onBack = back,
                        requesters = rows,
                        onRowFocused = { row -> state.recordFocus(top.page, row) },
                    )
                CompositionLocalProvider(LocalTvSideSheetPage provides page) { column.content(nav) }
            }
        }
    }
}
