package com.binge.designsystem.component

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
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
 * [largeAvatar] gives this destination the larger avatar the custom rail needs for a profile picture at that
 * width — by flag rather than list position, so appending or reordering tabs can't move it.
 *
 * [testTag], when set, tags the item's clickable node in every presentation, so a UI-automation
 * driver can find a destination by id rather than by its label. It is explicit rather than derived
 * from [key] because [key] is opaque to the shell and has no stable string form.
 */
data class BingeNavSuiteItem(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val badge: NavSuiteBadge = NavSuiteBadge.None,
    val avatarName: String? = null,
    val avatarUrl: String? = null,
    val largeAvatar: Boolean = false,
    val testTag: String? = null,
)

/**
 * Which nav presentation the shell renders. Chosen by resource-bucket bools in production
 * ([rememberBingeNavPresentation]); samples pass it explicitly so the canvas size and rendered nav
 * stay in lockstep under preview.
 */
enum class BingeNavPresentation {
    /** Compact / portrait: the bottom navigation bar. */
    BottomBar,

    /**
     * Landscape tablet (a window >=1000dp wide with a smallest width >=600dp): the bespoke expanded
     * rail with the account avatar.
     */
    CustomRail,

    /**
     * Everything that isn't a landscape tablet — phones, portrait tablets and unfolded foldables: the
     * floating pill, bottom-centred over content.
     */
    FloatingBar,
}

/**
 * The Binge app shell. Presentation is chosen by one qualifier-resolved bool resource
 * (`binge_nav_rail_expanded`), not imperative width/orientation code. The bespoke side rail is for a
 * landscape tablet only: a window at least 1000dp wide with a smallest width of at least 600dp
 * (`values-sw600dp-w1000dp`). Every other bucket gets the floating bar: portrait phone, landscape
 * phone, portrait tablet, and an unfolded foldable in either orientation.
 *
 * The 1000dp line sits between a near-square unfolded book-style foldable (~850-880dp wide either
 * way round) and a typical landscape tablet (~1280dp). Material's 1200dp "large" breakpoint was
 * considered, but it would drop ~1100dp small tablets to the floating bar.
 *
 * Resources, not code, because the configuration is known before the first frame. A runtime check
 * such as a fold-state listener reports later, so the shell would draw one presentation and then swap
 * to the other. Orientation does not select a presentation on its own either, so rotating does not
 * need an Activity recreation.
 *
 * [BottomBar] is retained as the pre-floating presentation: nothing in production selects it, and the
 * catalog + retention test pin it explicitly so the docked bar stays comparable and testable.
 *
 * Stateless — the caller supplies [items], [selectedKey], and [onSelect], and renders routed UI in
 * [content]. The nav is always shown: full-screen details live on a root back stack *above* this
 * shell, so there's no `showNavigation` flag and no `movableContentOf`.
 */
@Composable
fun BingeNavSuiteShell(
    items: List<BingeNavSuiteItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
    presentation: BingeNavPresentation = rememberBingeNavPresentation(),
    floatingTone: BingeNavFloatingTone = BingeNavFloatingTone.AlwaysDark,
    floatingStyle: BingeNavFloatingStyle = BingeNavFloatingStyle.IconWithSelectedLabel,
    content: @Composable () -> Unit,
) {
    when (presentation) {
        BingeNavPresentation.CustomRail ->
            BingeNavCustomRail(items = items, selectedKey = selectedKey, onSelect = onSelect, modifier = modifier, content = content)
        BingeNavPresentation.BottomBar ->
            BottomBarScaffold(items = items, selectedKey = selectedKey, onSelect = onSelect, modifier = modifier, content = content)
        BingeNavPresentation.FloatingBar ->
            BingeNavFloatingBarScaffold(items, selectedKey, onSelect, floatingTone, floatingStyle, modifier, content)
    }
}

/**
 * The safe area a navigation surface clears for itself: the system bars and the display cutout, without the keyboard.
 * Every presentation hides under the keyboard rather than riding it, and the navigation bar stays in this inset while
 * the keyboard is up, so the surface does not move when one opens.
 */
@Composable
internal fun navSurfaceInsets(): WindowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout)

/**
 * The safe area [rememberNavOverlayInsets] publishes: [WindowInsets.safeDrawing] less the keyboard. The keyboard inset
 * already includes the navigation bar, so its bottom drops to zero while the keyboard is up. That is deliberate: a
 * consumer's own `imePadding()` on top then counts the navigation bar once, not twice.
 */
@Composable
private fun navOverlayDrawing(): WindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomBarScaffold(
    items: List<BingeNavSuiteItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
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
        modifier = modifier,
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
                    modifier = tab.testTag?.let { Modifier.testTag(it) } ?: Modifier,
                )
            }
        },
        content = content,
    )
}

@Composable
internal fun NavSuiteItemIcon(item: BingeNavSuiteItem, avatarSize: Dp = dimensionResource(R.dimen.nav_item_avatar_size)) {
    // The glyph is decoration: the item names itself, through its label text or its own description, so a
    // description here would read the label twice and an avatar's initials would read in its place.
    val visual = @Composable {
        val avatarName = item.avatarName
        if (avatarName != null) {
            BingeInitialsAvatar(
                name = avatarName,
                avatarUrl = item.avatarUrl,
                size = avatarSize,
                modifier = Modifier.clearAndSetSemantics {},
            )
        } else {
            Icon(item.icon, contentDescription = null)
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
 * `binge_nav_rail_expanded` bool: the custom rail on a landscape tablet (>=1000dp wide, >=600dp
 * smallest width), the floating bar everywhere else, unfolded foldables included. See
 * [BingeNavSuiteShell] for the 1000dp line and why it lives in resources, not code.
 */
@Composable
fun rememberBingeNavPresentation(): BingeNavPresentation =
    if (booleanResource(R.bool.binge_nav_rail_expanded)) {
        BingeNavPresentation.CustomRail
    } else {
        BingeNavPresentation.FloatingBar
    }

/**
 * The [com.binge.designsystem.LocalNavOverlayInsets] value [BingeNavSuiteShell] provides for
 * [presentation]. [BingeNavFloatingBarScaffold] and [BingeNavCustomRail] both call this rather than
 * recomputing it, so it is the one formula rather than one per shell branch; a caller outside the
 * shell entirely — a snackbar host mounted as the shell's sibling rather than beneath it, say —
 * calls it too, to get the same ambient value at its own level.
 *
 * The one gap: [BingeNavFloatingBarScaffold] also applies an upward correction from its own
 * measured toolbar height, for a style whose content outgrows the default container. That
 * correction is real layout state internal to that composition and isn't available here, so this
 * returns its computed floor only — the two already agree for every style currently shipped.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun rememberNavOverlayInsets(presentation: BingeNavPresentation): PaddingValues {
    val safeInsets = navOverlayDrawing().asPaddingValues()
    return when (presentation) {
        BingeNavPresentation.BottomBar -> PaddingValues()
        BingeNavPresentation.CustomRail ->
            PaddingValues(
                start = dimensionResource(R.dimen.nav_custom_rail_width) +
                    safeInsets.calculateStartPadding(LocalLayoutDirection.current),
                bottom = safeInsets.calculateBottomPadding(),
            )
        BingeNavPresentation.FloatingBar ->
            PaddingValues(
                bottom = FloatingToolbarDefaults.ContainerSize + FloatingToolbarDefaults.ScreenOffset +
                    safeInsets.calculateBottomPadding(),
            )
    }
}
