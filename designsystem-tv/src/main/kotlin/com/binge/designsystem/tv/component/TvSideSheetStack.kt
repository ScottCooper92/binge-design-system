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
import androidx.compose.ui.layout.onGloballyPositioned
import com.binge.designsystem.tv.focus.tryRequestFocus
import com.binge.designsystem.tv.focus.tvLateEntryFocusGroup

private const val ENTRY_SEPARATOR = '\u0001'

private data class StackEntry(
    val page: String,
    val openedBy: String?,
    val parentTitle: String? = null,
)

private val StackSaver =
    Saver<SnapshotStateList<StackEntry>, List<String>>(
        save = { entries ->
            entries.map { "${it.page}$ENTRY_SEPARATOR${it.openedBy.orEmpty()}$ENTRY_SEPARATOR${it.parentTitle.orEmpty()}" }
        },
        restore = { saved ->
            mutableStateListOf<StackEntry>().apply {
                saved.forEach { line ->
                    val fields = line.split(ENTRY_SEPARATOR)
                    add(
                        StackEntry(
                            fields[0],
                            fields.getOrNull(1)?.takeIf { it.isNotEmpty() },
                            fields.getOrNull(2)?.takeIf { it.isNotEmpty() },
                        ),
                    )
                }
            }
        },
    )

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
     * disabled meanwhile) passes it to the next candidate, the arrival row and then the first, once that one is placed, so
     * focus never stays on the anchor.
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
) {
    /** Opens [page] over this one. [openedBy] is the key of the row that opened it, which takes focus back on [pop]. */
    fun push(page: String, openedBy: String) = onPush(page, openedBy)

    /** Goes back one page, or closes the sheet from the first. */
    fun pop() = onBack()

    /**
     * Marks a row so the stack can focus it, and goes on its `modifier`. Keys are unique within a page. The first row to
     * ask is where focus lands when the page opens, unless [arriveAt] names another.
     */
    fun row(key: String): Modifier =
        Modifier
            .focusRequester(requesters.requesterFor(key))
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
 * state. [content] draws the page named by [TvSideSheetNav.page], and gives every row [TvSideSheetNav.row].
 */
@Composable
fun TvSideSheetStack(
    root: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
    content: @Composable ColumnScope.(nav: TvSideSheetNav) -> Unit,
) {
    val entries = rememberSaveable(root, saver = StackSaver) { mutableStateListOf(StackEntry(root, null)) }
    val pageStates = rememberSaveableStateHolder()
    var restoreTo by remember { mutableStateOf<String?>(null) }
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
        } else {
            onDismissRequest()
        }
    }
    // The sheet's window takes focus before the page's effect runs; routing entry to the target keeps it off the first row.
    var currentRows by remember { mutableStateOf<RowRequesters?>(null) }
    val routed = modifier.tvLateEntryFocusGroup { currentRows?.target(restoreTo) }
    TvSideSheetHost(onDismissRequest = back, modifier = routed, toBottomEdge = toBottomEdge, closer = null, anchor = anchor) {
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
                    )
                CompositionLocalProvider(LocalTvSideSheetPage provides page) { column.content(nav) }
            }
        }
    }
}
