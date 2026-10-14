package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.isExpandedLayout

/**
 * What a sheet is for, which fixes its form on a wide window. A [Task] is where the user works: a form, a picker, an
 * editor, a flow of steps. A [Menu] is a short choice: an action list, an overflow.
 */
enum class BingeSheetKind { Task, Menu }

/**
 * A [BingeAdaptiveSheet]'s handle: which [page] is showing, and how to open another over it or go back.
 *
 * Pages stack only where the sheet is a side sheet ([stacksPages]). Below the expanded width the sheet is the bottom
 * sheet it always was, and [push] does nothing: a caller there opens whatever it opened before, so a compact window does
 * not change. A caller that opens something from a task sheet therefore asks [stacksPages] first.
 */
@Stable
class BingeSheetNav internal constructor(
    val page: String,
    /** How many pages are open, the root being one. */
    val depth: Int,
    /** Whether [push] opens a page inside the sheet. False for a bottom sheet and for every menu. */
    val stacksPages: Boolean,
    private val onPush: (page: String) -> Unit,
    private val onBack: () -> Unit,
) {
    /** Opens [page] over this one, inside the sheet. Does nothing unless [stacksPages]. */
    fun push(page: String) = onPush(page)

    /** Goes back one page, or closes the sheet from the first. A page left this way is reported to `onPageBack` too. */
    fun pop() = onBack()
}

/**
 * One sheet that picks its form from the window, so a list-detail layout does not get a bottom sheet over both panes.
 *
 * - [BingeSheetKind.Task] at the expanded width (840dp and up) is a side sheet on the end edge, with the pages
 *   [BingeSheetNav.push] opens stacked inside it: a sheet opened from a task sheet is a page with a Back, never a second
 *   sheet. System Back goes back a page, and from the root closes the sheet. A tap on the scrim closes it outright.
 * - [BingeSheetKind.Task] below that width, and [BingeSheetKind.Menu] at every width, is [BingeBottomSheet], with the
 *   same capped width, insets and keyboard handling as before.
 *
 * [content] draws the page named by [BingeSheetNav.page]; what a page shows is the caller's to read from its own state,
 * and no page opens another of the same name. [skipPartiallyExpanded], [gesturesEnabled], [dismissOnClickOutside],
 * [dockable] and [edgeToEdge] are [BingeBottomSheet]'s and apply to its form; the side sheet has no drag, so they do not
 * reach it, except that [dismissOnClickOutside] false also stops a scrim tap from closing it, and [gesturesEnabled] false
 * locks it like the bottom sheet: system Back and the Back bar do nothing, so a write in flight is not walked away from.
 *
 * The page stack is saved, so a rotation keeps the sheet on the page it was on. A caller whose pages are backed by its own
 * state (a view model that holds the draft a page edits) hears every page left by Back, by the Back bar or the system,
 * through [onPageBack], and can drop that page's state there. It is not called for the sheet closing from the root, which
 * is [onDismissRequest], nor for a page the caller pops itself.
 */
@Composable
fun BingeAdaptiveSheet(
    kind: BingeSheetKind,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    root: String = ROOT_PAGE,
    onPageBack: (page: String) -> Unit = {},
    skipPartiallyExpanded: Boolean = true,
    gesturesEnabled: Boolean = true,
    dismissOnClickOutside: Boolean = gesturesEnabled,
    dockable: Boolean = false,
    edgeToEdge: Boolean = false,
    content: @Composable ColumnScope.(nav: BingeSheetNav) -> Unit,
) {
    if (kind == BingeSheetKind.Task && isExpandedLayout()) {
        BingeTaskSideSheet(
            root = root,
            onDismissRequest = onDismissRequest,
            onPageBack = onPageBack,
            locked = !gesturesEnabled,
            scrimDismisses = dismissOnClickOutside,
            modifier = modifier,
            content = content,
        )
    } else {
        val nav = BingeSheetNav(page = root, depth = 1, stacksPages = false, onPush = {}, onBack = onDismissRequest)
        BingeBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            skipPartiallyExpanded = skipPartiallyExpanded,
            gesturesEnabled = gesturesEnabled,
            dismissOnClickOutside = dismissOnClickOutside,
            dockable = dockable,
            edgeToEdge = edgeToEdge,
        ) { content(nav) }
    }
}

/** The page a sheet opens on when its caller names none. */
const val ROOT_PAGE = "root"

@Composable
private fun BingeTaskSideSheet(
    root: String,
    onDismissRequest: () -> Unit,
    onPageBack: (page: String) -> Unit,
    locked: Boolean,
    scrimDismisses: Boolean,
    modifier: Modifier,
    content: @Composable ColumnScope.(nav: BingeSheetNav) -> Unit,
) {
    val pages = rememberSaveable(root, saver = PageStackSaver) { mutableStateListOf(root) }
    val back: () -> Unit = {
        if (locked) {
            Unit
        } else if (pages.size > 1) {
            onPageBack(pages.removeAt(pages.lastIndex))
        } else {
            onDismissRequest()
        }
    }
    BingeSideSheetWindow(
        onDismissRequest = back,
        onScrimTap = { if (scrimDismisses) onDismissRequest() },
        panel = {
            BingeTaskSheetPanel(
                pages = pages,
                onPush = { page -> pages.add(page) },
                onBack = back,
                backEnabled = !locked,
                modifier = modifier,
                content = content,
            )
        },
    )
}

private val PageStackSaver =
    listSaver<MutableList<String>, String>(
        save = { it.toList() },
        restore = { saved -> mutableStateListOf<String>().apply { addAll(saved) } },
    )

/**
 * A task sheet's resting panel: the page at the top of [pages] under a Back bar once there is more than one, inside the
 * side sheet's chrome and clear of the system bars and the keyboard. It is the panel without the modal window around it,
 * which a screenshot does not capture, so a frame or a test can render a sheet's page as the side sheet draws it. A sheet in
 * use is [BingeAdaptiveSheet], which owns the stack; this takes it as [pages] and reports [onPush] and [onBack].
 */
@Composable
fun BingeTaskSheetPanel(
    pages: List<String>,
    onPush: (page: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backEnabled: Boolean = true,
    content: @Composable ColumnScope.(nav: BingeSheetNav) -> Unit,
) {
    BingeSideSheetPanel(modifier = modifier, width = dimensionResource(R.dimen.task_sheet_width)) {
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))) {
            val nav =
                BingeSheetNav(
                    page = pages.last(),
                    depth = pages.size,
                    stacksPages = true,
                    onPush = onPush,
                    onBack = onBack,
                )
            if (pages.size > 1) BingeSheetBackBar(onBack, enabled = backEnabled)
            key(nav.page) { content(nav) }
        }
    }
}

/** The bar above a stacked page: Back at the start, as a sheet that is not the first page of its stack has no other way out but the scrim. */
@Composable
private fun BingeSheetBackBar(onBack: () -> Unit, enabled: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_xs))) {
        IconButton(onClick = onBack, enabled = enabled) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_navigate_back))
        }
    }
}
