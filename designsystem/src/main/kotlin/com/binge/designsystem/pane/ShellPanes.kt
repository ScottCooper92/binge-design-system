package com.binge.designsystem.pane

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.binge.designsystem.R

private const val SHELL_ROLE = "binge.designsystem.shell"
private const val PAGE_ROLE = "binge.designsystem.page"
private const val ITEM_ROLE = "binge.designsystem.item"
private const val OPENS_ITEMS_ROLE = "binge.designsystem.opensItems"

/**
 * The metadata that tells [ShellPanesStrategy] what each entry on the app's stack is. Pass one as a `NavEntry`'s `metadata`:
 * the entry that holds the tab bar and the home with [shell], a page that opens beside the home with [page], and what a page opens
 * with [item].
 */
object ShellPaneRole {
    /** The shell's entry: the one that holds the tab bar or rail and the home the pages open beside. */
    val shell: Map<String, Any> = mapOf(SHELL_ROLE to true)

    /** A page that opens beside the home. [opensItems] is true for a page whose items open as a third pane on a window that has one. */
    fun page(opensItems: Boolean = false): Map<String, Any> = mapOf(PAGE_ROLE to true, OPENS_ITEMS_ROLE to opensItems)

    /** What a page opens: a task, a log, a user. The third pane where there is one, and over its page where there is not. */
    val item: Map<String, Any> = mapOf(ITEM_ROLE to true)
}

/**
 * What sits above the shell on the app's stack, for the home to draw beside itself: the newest [page] (or none, for the default
 * page), and on a [threePane] window the newest [item] of a page that [opensItems].
 */
@Immutable
class ShellDetail internal constructor(
    val page: NavEntry<NavKey>?,
    val item: NavEntry<NavKey>?,
    val opensItems: Boolean,
    val threePane: Boolean,
)

/** The detail the scene hands the shell's entry. Null outside [ShellPanesStrategy]'s scene: the home is then alone, as on a phone. */
val LocalShellDetail = compositionLocalOf<ShellDetail?> { null }

/**
 * A wide window's shell: one scene for the shell's entry and the pages above it, so the bar or rail stays and the home draws them
 * beside itself. The pages stay on the app's stack at every width; only how they are drawn changes with the window, so a fold moves no
 * entry and its view models and scroll survive. A stack whose first entry above the shell is not a [ShellPaneRole.page] covers the
 * whole window, as on a phone, and so does any stack when the strategy is not [enabled].
 *
 * On a [threePane] window, a page marked as opening items is the second pane and its item the third; otherwise the newest entry above
 * the shell is the one other pane.
 *
 * @param enabled false where the window has no room beside the home, so the strategy leaves every stack to the next one.
 * @param threePane true where the window has room for a page and its item beside the home.
 */
class ShellPanesStrategy(
    private val enabled: Boolean,
    private val threePane: Boolean = false,
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(entries: List<NavEntry<NavKey>>): Scene<NavKey>? {
        if (!enabled) return null
        val shell = entries.firstOrNull()?.takeIf { it.metadata[SHELL_ROLE] == true } ?: return null
        val above = entries.drop(1)
        if (above.isNotEmpty() && above.first().metadata[PAGE_ROLE] != true) return null
        val top = above.lastOrNull()
        val topIsItem = top?.metadata?.get(ITEM_ROLE) == true
        // The page is the last page entry, not the one under the top: an item may be open over another page's.
        val page = if (topIsItem) above.lastOrNull { it.metadata[PAGE_ROLE] == true } else top
        val opensItems = page?.metadata?.get(OPENS_ITEMS_ROLE) == true
        val detail =
            if (threePane && opensItems) {
                ShellDetail(page, top.takeIf { topIsItem }, opensItems = true, threePane = true)
            } else {
                ShellDetail(top, null, opensItems = false, threePane = threePane)
            }
        return ShellPanesScene(entries, shell, detail)
    }
}

private class ShellPanesScene(
    override val entries: List<NavEntry<NavKey>>,
    private val shell: NavEntry<NavKey>,
    private val detail: ShellDetail,
) : Scene<NavKey> {
    /** One key for every stack, so opening or closing a page swaps a pane's content rather than animating a new scene in. */
    override val key: Any = ShellPanesScene::class
    override val previousEntries: List<NavEntry<NavKey>> = entries.dropLast(1)

    override val content: @Composable () -> Unit = {
        CompositionLocalProvider(LocalShellDetail provides detail) { shell.Content() }
    }
}

/**
 * The home with what the shell's scene gave it beside it: the [home] alone where no scene gave a detail (a narrow window), else the
 * home beside the open page or [defaultPage], and on a window with a third pane a page's item or [itemPlaceholder]. The home never
 * changes; a page and what it opens replace only the panes beside it.
 *
 * @param itemPlaceholder what the third pane shows while its page has no item open, given that page's entry.
 * @param modifier applied to the outermost node: the home alone, or the row of panes.
 *
 * @need draw a home beside the pages opened from it on a wide window, and alone on a narrow one
 * @group screens
 * @replaces a hand-built row of panes that reads the app's back stack
 */
@Composable
fun ShellPanes(
    home: @Composable () -> Unit,
    defaultPage: @Composable () -> Unit,
    itemPlaceholder: @Composable (page: NavEntry<NavKey>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val detail = LocalShellDetail.current
    when {
        detail == null -> Box(modifier) { home() }
        detail.opensItems && detail.page != null ->
            PaneRow(
                modifier = modifier,
                weights = listOf(1f, 1f, 1f),
                panes =
                    listOf(
                        home,
                        { detail.page.Content() },
                        { detail.item?.Content() ?: itemPlaceholder(detail.page) },
                    ),
            )
        detail.threePane ->
            PaneRow(
                modifier = modifier,
                weights = listOf(1f, 2f),
                panes = listOf(home, { detail.page?.Content() ?: defaultPage() }),
            )
        else ->
            PaneRow(
                modifier = modifier,
                weights = listOf(1f, 1f),
                panes = listOf(home, { detail.page?.Content() ?: defaultPage() }),
            )
    }
}

/** Panes in a row, sharing the width by their [weights], with the design system's gap between them. */
@Composable
private fun PaneRow(
    weights: List<Float>,
    panes: List<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    val gap = dimensionResource(R.dimen.pane_spacer)
    Row(modifier.fillMaxSize()) {
        panes.forEachIndexed { index, pane ->
            if (index > 0) Spacer(Modifier.width(gap))
            Box(Modifier.weight(weights[index]).fillMaxHeight()) { pane() }
        }
    }
}
