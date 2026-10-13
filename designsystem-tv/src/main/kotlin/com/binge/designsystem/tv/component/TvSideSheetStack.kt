package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.binge.designsystem.tv.focus.restoreTvOverlayFocus

private const val ENTRY_SEPARATOR = '\u0001'

private data class StackEntry(
    val page: String,
    val openedBy: String?,
)

private val StackSaver =
    Saver<SnapshotStateList<StackEntry>, List<String>>(
        save = { entries -> entries.map { "${it.page}$ENTRY_SEPARATOR${it.openedBy.orEmpty()}" } },
        restore = { saved ->
            mutableStateListOf<StackEntry>().apply {
                saved.forEach { line ->
                    val page = line.substringBefore(ENTRY_SEPARATOR)
                    add(StackEntry(page, line.substringAfter(ENTRY_SEPARATOR).takeIf { it.isNotEmpty() }))
                }
            }
        },
    )

/** The focus requesters of one page's rows, by the key each row gave itself. */
internal class RowRequesters {
    private val requesters = mutableMapOf<String, FocusRequester>()
    var first: String? = null
        private set
    var arrival: String? = null

    fun requesterFor(key: String): FocusRequester {
        if (first == null) first = key
        return requesters.getOrPut(key) { FocusRequester() }
    }

    fun contains(key: String) = key in requesters

    fun target(restoreTo: String?): FocusRequester? = (restoreTo?.takeIf(::contains) ?: arrival ?: first)?.let(requesters::get)
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
    fun row(key: String): Modifier = Modifier.focusRequester(requesters.requesterFor(key))

    /**
     * Where focus lands when this page opens, in place of its first row: the Cancel of a confirm, say, which
     * [TvSideSheetConfirm] takes as `cancelModifier = nav.row("cancel")` with a null `entryFocus`.
     */
    fun arriveAt(key: String) {
        requesters.arrival = key
    }
}

/**
 * A [TvSideSheet] of pages that open one over another, like a settings list whose rows each open their own list, with
 * Jellyfin's TV preferences as the model. The root [root] page shows first. A row opens another with
 * [TvSideSheetNav.push]; Back, or the key pointing away from the panel, goes back one page, and from the root calls
 * [onDismissRequest], where the caller returns focus to what opened the sheet, with `rememberTvOverlayCloser`.
 *
 * Focus follows the pages. A page that opens puts focus on its first row, and going back puts it on the row that opened the
 * page just left, so the user is where they were. Pages are keyed by name, so the stack survives a rotation, and no page
 * opens another of the same name; what a page shows is the caller's to read from its own state. [content] draws the page named by [TvSideSheetNav.page],
 * and gives every row [TvSideSheetNav.row].
 */
@Composable
fun TvSideSheetStack(
    root: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(nav: TvSideSheetNav) -> Unit,
) {
    val entries = rememberSaveable(root, saver = StackSaver) { mutableStateListOf(StackEntry(root, null)) }
    var restoreTo by remember { mutableStateOf<String?>(null) }
    val back: () -> Unit = {
        if (entries.size > 1) {
            restoreTo = entries.last().openedBy
            entries.removeAt(entries.lastIndex)
        } else {
            onDismissRequest()
        }
    }
    TvSideSheet(onDismissRequest = back, modifier = modifier) {
        val top = entries.last()
        key(top.page) {
            val rows = remember { RowRequesters() }
            val nav =
                TvSideSheetNav(
                    page = top.page,
                    depth = entries.size,
                    onPush = { page, openedBy ->
                        restoreTo = null
                        entries.add(StackEntry(page, openedBy))
                    },
                    onBack = back,
                    requesters = rows,
                )
            content(nav)
            // After the page's rows, which are what put their requesters in the map.
            val target = rows.target(restoreTo)
            if (target != null) LaunchedEffect(Unit) { restoreTvOverlayFocus(target) }
        }
    }
}
