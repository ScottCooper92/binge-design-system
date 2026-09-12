package com.binge.designsystem.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R

/** A badge rendered on a [BingeNavSuiteItem]; [Label] holds already-formatted display text. */
sealed interface NavSuiteBadge {
    data object None : NavSuiteBadge

    data class Label(
        val text: String,
    ) : NavSuiteBadge
}

/**
 * One navigation destination in [BingeNavSuiteShell]. [key] identifies the item for
 * selection/click; it is opaque to the shell (the caller maps it back to a route).
 *
 * [avatarName], when non-null, renders a user avatar instead of [icon] (the signed-in account item):
 * the profile image from [avatarUrl] when present, falling back to [avatarName]'s initials. [icon] is
 * the fallback when [avatarName] is null (signed out). [badge] overlays either rendering.
 *
 * [isAccount] marks the Account destination so the custom rail can give it the larger avatar it needs
 * at that width — by flag rather than list position, so appending or reordering tabs can't move it.
 */
data class BingeNavSuiteItem(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val badge: NavSuiteBadge = NavSuiteBadge.None,
    val avatarName: String? = null,
    val avatarUrl: String? = null,
    val isAccount: Boolean = false,
)

/**
 * Which nav presentation the shell renders. Chosen by resource-bucket bools in production
 * ([rememberBingeNavPresentation]); samples pass it explicitly so the canvas size and rendered nav
 * stay in lockstep under preview.
 */
enum class BingeNavPresentation {
    /** Compact / portrait: the bottom navigation bar. */
    BottomBar,

    /** Tablet landscape / unfolded foldable: the bespoke expanded rail with the account avatar. */
    CustomRail,

    /** Everything that isn't an expanded window: the floating pill, bottom-centred over content. */
    FloatingBar,
}

/**
 * The Binge app shell. Presentation is chosen by one qualifier-resolved bool resource
 * (`binge_nav_rail_expanded`), not imperative width/orientation code: a genuine expanded window keeps
 * the bespoke side rail, and every other bucket — portrait phone, landscape phone, portrait tablet —
 * gets the floating bar. Orientation no longer selects a presentation, so the landscape swap that
 * used to need an Activity recreation is gone.
 *
 * [BottomBar] is retained as the pre-floating presentation: nothing in production selects it, and the
 * catalog + retention test pin it explicitly so the docked bar stays comparable and testable.
 *
 * Stateless — the caller supplies [items], [selectedKey], and [onSelect], and renders routed UI in
 * [content]. The nav is always shown: full-screen details live on a root back stack *above* this
 * shell (#1213), so there's no `showNavigation` flag and no `movableContentOf`.
 */
@Composable
fun BingeNavSuiteShell(
    items: List<BingeNavSuiteItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    presentation: BingeNavPresentation = rememberBingeNavPresentation(),
    floatingTone: BingeNavFloatingTone = BingeNavFloatingTone.AlwaysDark,
    floatingStyle: BingeNavFloatingStyle = BingeNavFloatingStyle.IconWithSelectedLabel,
    content: @Composable () -> Unit,
) {
    when (presentation) {
        BingeNavPresentation.CustomRail ->
            BingeNavCustomRail(items = items, selectedKey = selectedKey, onSelect = onSelect, content = content)
        BingeNavPresentation.BottomBar ->
            BottomBarScaffold(items = items, selectedKey = selectedKey, onSelect = onSelect, content = content)
        BingeNavPresentation.FloatingBar ->
            BingeNavFloatingBarScaffold(items, selectedKey, onSelect, floatingTone, floatingStyle, content)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomBarScaffold(
    items: List<BingeNavSuiteItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    content: @Composable () -> Unit,
) {
    // Slide the bottom bar away while the keyboard is up. It sits behind the keyboard anyway, and hiding it lets
    // the scaffold give content the full height: otherwise it reserves the bar's height, a typing screen's own
    // imePadding() stacks on that reservation, and a gap opens above the keyboard (the Search field's black band).
    val scaffoldState = rememberNavigationSuiteScaffoldState()
    val imeVisible = WindowInsets.isImeVisible
    LaunchedEffect(imeVisible) {
        if (imeVisible) scaffoldState.hide() else scaffoldState.show()
    }
    NavigationSuiteScaffold(
        state = scaffoldState,
        layoutType = NavigationSuiteType.NavigationBar,
        containerColor = MaterialTheme.colorScheme.background,
        navigationSuiteItems = {
            items.forEach { tab ->
                item(
                    selected = selectedKey == tab.key,
                    onClick = { onSelect(tab.key) },
                    icon = { NavSuiteItemIcon(tab) },
                    label = { Text(tab.label) },
                )
            }
        },
        content = content,
    )
}

@Composable
internal fun NavSuiteItemIcon(item: BingeNavSuiteItem, avatarSize: Dp = dimensionResource(R.dimen.nav_item_avatar_size)) {
    val visual = @Composable {
        val avatarName = item.avatarName
        if (avatarName != null) {
            BingeInitialsAvatar(name = avatarName, avatarUrl = item.avatarUrl, size = avatarSize)
        } else {
            Icon(item.icon, contentDescription = item.label)
        }
    }
    when (val badge = item.badge) {
        NavSuiteBadge.None -> visual()
        is NavSuiteBadge.Label ->
            BadgedBox(badge = { Badge { Text(badge.text) } }) { visual() }
    }
}

/**
 * The [BingeNavPresentation] for the current window, read straight from the qualifier-resolved
 * `binge_nav_rail_expanded` bool: the custom rail on an expanded window, the floating bar everywhere
 * else. See [BingeNavSuiteShell] for why this lives in resources, not code.
 */
@Composable
fun rememberBingeNavPresentation(): BingeNavPresentation =
    if (booleanResource(R.bool.binge_nav_rail_expanded)) {
        BingeNavPresentation.CustomRail
    } else {
        BingeNavPresentation.FloatingBar
    }
