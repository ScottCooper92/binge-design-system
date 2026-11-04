package com.binge.designsystem.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
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
 * Task sheets stack pages at every width ([stacksPages]). Menus stay a single page, so [push] does nothing for a menu.
 */
@Stable
class BingeSheetNav internal constructor(
    val page: String,
    /** How many pages are open, the root being one. */
    val depth: Int,
    /** Whether [push] opens a page inside the sheet. False for every menu. */
    val stacksPages: Boolean,
    private val onPush: (page: String) -> Unit,
    private val onBack: () -> Unit,
) {
    /** Opens [page] over this one, inside the sheet. Does nothing unless [stacksPages]. */
    fun push(page: String) = onPush(page)

    /**
     * Goes back one page, or closes the sheet from the first. A page left this way is reported to `onPageBack` too. It is
     * the caller's own call, so a sheet locked by `gesturesEnabled` false still obeys it.
     */
    fun pop() = onBack()
}

/**
 * One sheet that picks its form from the window, so a list-detail layout does not get a bottom sheet over both panes.
 *
 * - [BingeSheetKind.Task] at the expanded width (840dp and up) is a side sheet on the end edge, with the pages
 *   [BingeSheetNav.push] opens stacked inside it: a sheet opened from a task sheet is a page with a Back, never a second
 *   sheet. System Back goes back a page, and from the root closes the sheet. A tap on the scrim closes it outright.
 * - [BingeSheetKind.Task] below that width, and [BingeSheetKind.Menu] at every width, is [BingeBottomSheet], with the
 *   same capped width, insets and keyboard handling as before. A compact task sheet stacks pages too: Back closes the
 *   keyboard first, then steps back a page, and closes the sheet from its root.
 *
 * [content] draws the page named by [BingeSheetNav.page]; what a page shows is the caller's to read from its own state,
 * and no page opens another of the same name. [skipPartiallyExpanded], [gesturesEnabled], [dismissOnClickOutside],
 * [dockable] and [edgeToEdge] are [BingeBottomSheet]'s and apply to its form; the side sheet has no drag, so they do not
 * reach it, except that [dismissOnClickOutside] false also stops a scrim tap from closing it, and [gesturesEnabled] false
 * locks it like the bottom sheet: system Back and the Back bar do nothing, so a write in flight is not walked away from.
 *
 * [backLabel] names the page Back returns to, shown beside the arrow of a pushed page's Back bar. It is called with the page now
 * showing and returns the title of the page below it, or null for the icon-only bar. It reaches the side sheet only: the bottom
 * form has no Back bar, so a caller draws its own back row there.
 *
 * The page stack is saved and shared by both forms, so a rotation or a fold keeps the sheet on the page it was on. If the
 * window narrows with pages open, the bottom sheet shows the top one and keeps the same navigation. A scrim tap or drag
 * closes the whole bottom sheet, while system Back and [BingeSheetNav.pop] step back through its pages. A caller whose pages are backed by its own state (a view
 * model that holds the draft a page edits) hears every page left by Back, by the Back bar, by the system or by
 * [BingeSheetNav.pop], through [onPageBack], and can drop that page's state there. It is not called for the sheet closing,
 * which is [onDismissRequest]; the caller drops whatever pages remain then.
 */
@OptIn(ExperimentalLayoutApi::class)
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
    backLabel: (page: String) -> String? = { null },
    content: @Composable ColumnScope.(nav: BingeSheetNav) -> Unit,
) {
    val pages = rememberSaveable(root, saver = PageStackSaver) { mutableStateListOf(root) }
    val pageContent = rememberSheetPageContent(root, content)
    val popPage: () -> Unit = {
        if (pages.size > 1) {
            val popped = pages.removeAt(pages.lastIndex)
            pageContent.drop(popped)
            onPageBack(popped)
        } else {
            onDismissRequest()
        }
    }
    val hosted = pageContent.hosted
    if (kind == BingeSheetKind.Task && isExpandedLayout()) {
        BingeTaskSideSheet(
            pages = pages,
            onDismissRequest = onDismissRequest,
            popPage = popPage,
            locked = !gesturesEnabled,
            scrimDismisses = dismissOnClickOutside,
            backLabel = backLabel,
            modifier = modifier,
            content = hosted,
        )
    } else {
        val nav =
            BingeSheetNav(
                page = pages.last(),
                depth = pages.size,
                stacksPages = kind == BingeSheetKind.Task,
                onPush = { page -> if (kind == BingeSheetKind.Task) pages.add(page) },
                onBack = popPage,
            )
        BingeBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            skipPartiallyExpanded = skipPartiallyExpanded,
            gesturesEnabled = gesturesEnabled,
            dismissOnClickOutside = dismissOnClickOutside,
            dockable = dockable,
            edgeToEdge = edgeToEdge,
        ) {
            val imeVisible = WindowInsets.isImeVisible
            // This handler is inside the modal window. While the keyboard is up the bottom sheet's IME handler owns Back.
            BackHandler(enabled = kind == BingeSheetKind.Task && pages.size > 1 && gesturesEnabled && !imeVisible) { popPage() }
            val wash = rememberBingeSheetWash()
            CompositionLocalProvider(LocalBingeSheetWash provides wash) {
                Column(modifier = Modifier.fillMaxWidth().bingeSheetWashBehind(wash, overdrawTop = true)) { hosted(nav) }
            }
        }
    }
}

/**
 * Draws a sheet's pages so one survives the sheet turning from a bottom sheet into a side sheet or back (a rotation, a fold;
 * jellyfin#301). The two draw in different windows, so a `rememberSaveable` in the content has a different positional key in
 * each and is not found again after the activity is recreated. A [movableContentOf] made above both composes the
 * pages under the same key wherever they are shown, and a [SaveableStateHolder] keeps each page's saved state.
 */
@Stable
private class SheetPageContent(
    val hosted: @Composable ColumnScope.(BingeSheetNav) -> Unit,
    val drop: (page: String) -> Unit,
)

@Composable
private fun rememberSheetPageContent(root: String, content: @Composable ColumnScope.(BingeSheetNav) -> Unit): SheetPageContent {
    // A new root resets the page stack, so the pages' saved state goes with it and a later push starts fresh.
    val saveableState = key(root) { rememberSaveableStateHolder() }
    val latest = rememberUpdatedState(content)
    val movable =
        remember(saveableState) {
            movableContentOf<ColumnScope, BingeSheetNav> { scope, nav ->
                val body = latest.value
                key(nav.page) { saveableState.SaveableStateProvider(nav.page) { with(scope) { body(nav) } } }
            }
        }
    return remember(saveableState) {
        SheetPageContent(
            hosted = { nav -> movable(this, nav) },
            drop = { page -> saveableState.removeState(page) },
        )
    }
}

/** The page a sheet opens on when its caller names none. */
const val ROOT_PAGE = "root"

@Composable
private fun BingeTaskSideSheet(
    pages: MutableList<String>,
    onDismissRequest: () -> Unit,
    popPage: () -> Unit,
    locked: Boolean,
    scrimDismisses: Boolean,
    backLabel: (page: String) -> String?,
    modifier: Modifier,
    content: @Composable ColumnScope.(nav: BingeSheetNav) -> Unit,
) {
    BingeSideSheetWindow(
        // The lock stops the user's Back; a caller's own nav.pop() goes through popPage and is not held by it.
        onDismissRequest = { if (!locked) popPage() },
        onScrimTap = { if (scrimDismisses) onDismissRequest() },
        hideImeOnBack = !locked,
        panel = {
            BingeTaskSheetPanel(
                pages = pages,
                onPush = { page -> pages.add(page) },
                onBack = popPage,
                backEnabled = !locked,
                backLabel = backLabel(pages.last()),
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
    backLabel: String? = null,
    content: @Composable ColumnScope.(nav: BingeSheetNav) -> Unit,
) {
    val wash = rememberBingeSheetWash()
    BingeSideSheetPanel(modifier = modifier, width = dimensionResource(R.dimen.task_sheet_width), wash = wash) {
        CompositionLocalProvider(LocalBingeSheetWash provides wash) {
            Column(modifier = Modifier.fillMaxSize()) {
                val nav =
                    BingeSheetNav(
                        page = pages.last(),
                        depth = pages.size,
                        stacksPages = true,
                        onPush = onPush,
                        onBack = onBack,
                    )
                if (pages.size > 1) BingeSheetBackBar(onBack, enabled = backEnabled, label = backLabel)
                key(nav.page) { SheetPageWithFooter { content(nav) } }
            }
        }
    }
}

/**
 * The bar above a stacked page: Back at the start, as a sheet that is not the first page of its stack has no other way out
 * but the scrim. With a [label], the name of the page Back returns to sits beside the arrow and the pair is one target.
 */
@Composable
private fun BingeSheetBackBar(
    onBack: () -> Unit,
    enabled: Boolean,
    label: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.padding_xs)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label == null) {
            IconButton(onClick = onBack, enabled = enabled) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_navigate_back))
            }
        } else {
            Row(
                modifier = Modifier
                    .clickable(enabled = enabled, role = Role.Button, onClick = onBack)
                    .minimumInteractiveComponentSize()
                    .padding(horizontal = dimensionResource(R.dimen.padding_s)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
