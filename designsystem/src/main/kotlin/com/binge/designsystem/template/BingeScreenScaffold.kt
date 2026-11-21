package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.max
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLargeTopBar
import com.binge.designsystem.component.BingePaneTopBar
import com.binge.designsystem.component.BingeSnackbarHost
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.LocalRowFocus
import com.binge.designsystem.component.OverlaidHeaderContent
import com.binge.designsystem.component.RowFocus
import com.binge.designsystem.component.keepingBarHeightDuringRowFocus
import com.binge.designsystem.paneBackOrNull
import com.binge.designsystem.paneSideInsets
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.resolvedContentPadding
import kotlinx.coroutines.flow.first

/** Which top bar a [BingeScreenScaffold] carries. */
enum class ScreenBar {
    /** A medium bar whose title collapses as the body scrolls, and a two-row bar beside another pane. */
    Collapsing,

    /** A single-line bar that slides away as the body scrolls down and returns as it scrolls up, or stays put when the scaffold is `pinned`. */
    Small,

    /** A large collapsing bar whose expanded heading carries a subtitle line and a trailing control. */
    Large,

    /** No bar: the body reaches the top of the window and clears the status bar itself. */
    None,
}

/** How a [BingeScreenScaffold]'s bar draws itself and its controls. */
enum class ScreenBarChrome {
    /** A transparent bar the body scrolls under, whose Back and actions sit on scrim-backed circles. The default. */
    Glass,

    /**
     * An opaque bar in the app's background, with a plain Back and actions. For a screen with no imagery under its
     * bar, such as a first-run flow, where a circle would frame the arrow against nothing.
     */
    Plain,
}

/**
 * The frame a phone, foldable or tablet screen with a top bar shares: a transparent bar over a body that
 * scrolls under it, a snackbar host, and an optional pinned [bottomBar].
 *
 * The bar scrims in as the body passes under it, so the title stays legible. [ScreenBar.Collapsing] becomes
 * the two-row pane bar when another pane sits beside it, and [onBack] is dropped where that pane already
 * offers the way back. A [header] (a search field, filter chips) is drawn over the body below the bar, and one
 * scrim spans both. [barScrim] is false only where something under the bar draws the scrim for it.
 *
 * [pinned] keeps a [ScreenBar.Small] in place while the body scrolls, as a search screen's bar stays over its field: the body still passes
 * under it and the scrim still follows how far it has, but the bar never slides away. It is false by default, so a small bar slides as it
 * always has, and is ignored for the other bars.
 *
 * [scrollBehavior] replaces the one the template picks from [bar], for a caller that owns the bar's state; it
 * is ignored for [ScreenBar.None]. [subtitle] and [titleTrailing] are [ScreenBar.Large]'s, and show while it is
 * expanded; a caller whose action takes over once it collapses hoists [scrollBehavior] to fade it.
 *
 * [bottomBar] reaches the true edge of the window and clears the navigation bar and a side cutout itself, by
 * [com.binge.designsystem.bottomBarInsets]. [ScreenFooter] is that chrome ready made, for a bar of any content.
 *
 * A [bottomBar] under the floating navigation pill
 * ([com.binge.designsystem.component.BingeNavPresentation.FloatingBar]) is not supported. The body's padding
 * still clears the pill, but the bar and the snackbar are not lifted above it, so the pill covers them. A
 * screen shown as a tab root under the pill commits from the top bar instead.
 *
 * [barChrome] picks the transparent bar with circled controls, or the opaque one with plain controls.
 *
 * [content] is handed padding that already clears the bar, the header, the system bars, a cutout and a
 * floating navigation bar. Split it with [screenOuterPadding] and [screenInnerPadding], or use [ScreenBody].
 *
 * [listState], when the body is a lazy list, lets a collapsing or large bar arrive collapsed over a list that is
 * already scrolled: one opened part way down, or jumped there by `scrollToItem` before the user has touched it. The
 * bar's collapse otherwise moves only through the user's scroll, so such a list would sit under a fully expanded bar.
 * Once the user scrolls, the bar behaves as it always does. Leave it null for a body that opens at the top.
 *
 * [onTitleLongClick] adds an optional title action, named for accessibility by [onTitleLongClickLabel].
 * Both default to absent, so existing titles keep their behaviour and appearance.
 *
 * @need frame a screen with a top bar, scrolling body and snackbar host
 * @group screens
 * @replaces a hand-built Scaffold with a transparent scrolling top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeScreenScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    bar: ScreenBar = ScreenBar.Collapsing,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    barScrim: Boolean = true,
    pinned: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    subtitle: String? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    onTitleLongClick: (() -> Unit)? = null,
    onTitleLongClickLabel: String? = null,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    barChrome: ScreenBarChrome = ScreenBarChrome.Glass,
    listState: LazyListState? = null,
    content: @Composable (padding: PaddingValues) -> Unit,
) {
    val barColor = if (barChrome == ScreenBarChrome.Plain) Color.Unspecified else Color.Transparent
    val ownBehavior = rememberScreenBarScrollBehavior(bar, pinned)
    val behavior = if (bar == ScreenBar.None) null else scrollBehavior ?: ownBehavior
    val scrim = screenBarScrim(bar, behavior)
    val bodyTouch = if (behavior != null && listState != null && bar.collapses()) {
        rememberCollapsedForScrolledList(behavior, listState)
    } else {
        null
    }
    val rowFocus = LocalRowFocus.current
    val back = onBack?.let { paneBackOrNull(it) }
    var bottomBarHeight by remember { mutableIntStateOf(0) }
    // What an ancestor already reserved, such as the custom rail's start inset, is not reserved again.
    var consumed by remember { mutableStateOf(WindowInsets(0, 0, 0, 0)) }
    Scaffold(
        modifier = (behavior?.let { modifier.nestedScroll(barConnection(bar, it, rowFocus)) } ?: modifier)
            .then(bodyTouch ?: Modifier)
            .onConsumedWindowInsetsChanged { consumed = it },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            val barScrimFraction = if (barScrim && header == null) scrim else 0f
            when (bar) {
                ScreenBar.Collapsing ->
                    BingePaneTopBar(
                        title = title,
                        onTitleLongClick = onTitleLongClick,
                        onTitleLongClickLabel = onTitleLongClickLabel,
                        onBack = back,
                        scrollBehavior = behavior,
                        containerColor = barColor,
                        scrimFraction = barScrimFraction,
                        foregroundScrimFraction = scrim,
                        actions = actions,
                    )
                ScreenBar.Small ->
                    BingeTopBar(
                        title = title,
                        onTitleLongClick = onTitleLongClick,
                        onTitleLongClickLabel = onTitleLongClickLabel,
                        onBack = back,
                        scrollBehavior = behavior,
                        containerColor = barColor,
                        scrimFraction = barScrimFraction,
                        foregroundScrimFraction = scrim,
                        actions = actions,
                    )
                ScreenBar.Large ->
                    BingeLargeTopBar(
                        title = title,
                        onTitleLongClick = onTitleLongClick,
                        onTitleLongClickLabel = onTitleLongClickLabel,
                        subtitle = subtitle,
                        onBack = back,
                        scrollBehavior = behavior,
                        containerColor = barColor,
                        scrimFraction = barScrimFraction,
                        foregroundScrimFraction = scrim,
                        titleTrailing = titleTrailing,
                        actions = actions,
                    )
                ScreenBar.None -> Unit
            }
        },
        bottomBar = { Box(Modifier.onSizeChanged { bottomBarHeight = it.height }) { bottomBar() } },
        snackbarHost = {
            // The Scaffold lifts the snackbar above a pinned bar. With none, it clears the navigation bar the
            // way the body does: by the part the floating navigation bar's inset does not already cover. Its sides clear
            // a side navigation bar or cutout, as the body's do.
            val uncovered = if (bottomBarHeight > 0) dimensionResource(R.dimen.zero) else snackbarNavigationInset()
            BingeSnackbarHost(snackbarHostState, Modifier.windowInsetsPadding(paneSideInsets()).padding(bottom = uncovered))
        },
    ) { scaffoldPadding ->
        val padding = screenPadding(scaffoldPadding, hasBar = bar != ScreenBar.None, consumed = consumed)
        // The overlay is folded into [padding], so a child's own navOverlayPadding() must not count it twice.
        CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues()) {
            if (header == null) {
                content(padding)
            } else {
                OverlaidHeaderContent(
                    // The bar's height joins the header rather than padding it from outside, so the body reaches
                    // the top of the window and passes under both.
                    header = {
                        Spacer(Modifier.height(padding.calculateTopPadding()))
                        header()
                    },
                    // Consumed as well as padded, so a header's chip row does not add the side insets again.
                    modifier = Modifier.padding(padding.screenOuterPadding()).consumeWindowInsets(padding.screenOuterPadding()),
                    headerBackground = Color.Transparent,
                    scrimFraction = scrim,
                ) { overlay ->
                    content(PaddingValues(top = overlay.calculateTopPadding(), bottom = padding.calculateBottomPadding()))
                }
            }
        }
    }
}

/**
 * The common body of a [BingeScreenScaffold]: a full-size box placed by [screenOuterPadding], handing
 * [content] the [screenInnerPadding] that belongs inside its scroll, or around a state that does not scroll.
 *
 * @need place a scaffold body with the standard screen padding
 * @group screens
 * @replaces hand-computed screen padding around a body
 */
@Composable
fun ScreenBody(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(inner: PaddingValues) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize().padding(padding.screenOuterPadding()).consumeWindowInsets(padding.screenOuterPadding())) {
        content(padding.screenInnerPadding())
    }
}

/**
 * The part of a screen's padding that stays outside a scrolling body: the sides. A body that scrolls
 * keeps the top and bottom inside its scroll, so rows pass under the bar and behind the navigation bar.
 */
@Composable
fun PaddingValues.screenOuterPadding(): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(start = calculateStartPadding(direction), end = calculateEndPadding(direction))
}

/** The part of a screen's padding that goes inside a scrolling body: the bar above it and the navigation bar below. */
fun PaddingValues.screenInnerPadding(): PaddingValues = PaddingValues(top = calculateTopPadding(), bottom = calculateBottomPadding())

/**
 * [screenInnerPadding] with the content inset on the sides and below, for a list under the bar or a header: the top is
 * already the bar's or header's own height, so the inset is not stacked onto it as well. The sides are
 * [resolvedContentPadding]'s, so beside another pane the shared edge takes the narrower inner inset.
 */
@Composable
fun PaddingValues.screenListPadding(): PaddingValues {
    // Per side, so the edge a pane shares with the one beside it takes pane_inner_inset rather than the window inset.
    val sides = resolvedContentPadding()
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = sides.calculateStartPadding(direction),
        top = calculateTopPadding(),
        end = sides.calculateEndPadding(direction),
        bottom = resolvedContentInset() + calculateBottomPadding(),
    )
}

/**
 * The connection a [bar]'s [behavior] listens to the body through. A sliding bar leaves its height alone for the scroll a row focus makes to
 * bring a row into view, so a screen opened on a row still has its bar. The collapsing and large bars are left as they were: a collapsing bar
 * arrives collapsed and scrimmed over the rows the user scrolled under it, which is how it should look. A list that
 * starts scrolled passes its state as `listState`, which collapses the bar for it ([rememberCollapsedForScrolledList]).
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun barConnection(
    bar: ScreenBar,
    behavior: TopAppBarScrollBehavior,
    rowFocus: RowFocus?,
): NestedScrollConnection =
    remember(bar, behavior, rowFocus) {
        if (bar == ScreenBar.Small) {
            behavior.nestedScrollConnection.keepingBarHeightDuringRowFocus(rowFocus, behavior.state)
        } else {
            behavior.nestedScrollConnection
        }
    }

/**
 * How far a [bar]'s scrim has faded in under [behavior]: with the body's collapse for the collapsing and large bars,
 * and with the content passing under it for the small one.
 */
@OptIn(ExperimentalMaterial3Api::class)
internal fun screenBarScrim(bar: ScreenBar, behavior: TopAppBarScrollBehavior?): Float =
    behavior?.let { if (bar == ScreenBar.Small) it.state.overlappedFraction else it.state.collapsedFraction } ?: 0f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberScreenBarScrollBehavior(bar: ScreenBar, pinned: Boolean = false): TopAppBarScrollBehavior? =
    when (bar) {
        ScreenBar.Collapsing, ScreenBar.Large -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        ScreenBar.Small -> if (pinned) TopAppBarDefaults.pinnedScrollBehavior() else TopAppBarDefaults.enterAlwaysScrollBehavior()
        ScreenBar.None -> null
    }

@Composable
private fun snackbarNavigationInset(): Dp {
    val overlay = LocalNavOverlayInsets.current.calculateBottomPadding()
    val navigation = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return max(overlay, navigation) - overlay
}

/**
 * The one insets policy. The sides clear this pane's system bars and cutout, less what an ancestor [consumed], or a
 * navigation rail drawn over it, whichever reaches further: the rail's inset already holds the cutout, so the two are not summed. The bottom clears
 * whichever is tallest of a pinned bottom bar, the floating navigation bar and the gesture bar. The pinned bar already
 * clears the gesture bar itself, and inside the shell the floating bar's inset already covers it, so none is summed.
 */
@Composable
private fun screenPadding(
    scaffoldPadding: PaddingValues,
    hasBar: Boolean,
    consumed: WindowInsets,
): PaddingValues {
    val direction = LocalLayoutDirection.current
    val sides = paneSideInsets().exclude(consumed).asPaddingValues()
    val overlay = LocalNavOverlayInsets.current
    val bottomBar = scaffoldPadding.calculateBottomPadding()
    val navigation = max(overlay.calculateBottomPadding(), WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    return PaddingValues(
        start = max(sides.calculateStartPadding(direction), overlay.calculateStartPadding(direction)),
        top = if (hasBar) scaffoldPadding.calculateTopPadding() else WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        end = max(sides.calculateEndPadding(direction), overlay.calculateEndPadding(direction)),
        bottom = max(bottomBar, navigation),
    )
}

private fun ScreenBar.collapses(): Boolean = this == ScreenBar.Collapsing || this == ScreenBar.Large

/**
 * Collapses [behavior]'s bar once for a list that is already scrolled when the user has not yet touched it, and returns the
 * modifier that tells it when the user does. A collapsing bar moves only through nested scroll, which a restored position or a
 * programmatic `scrollToItem` never dispatches, so such a list would sit under a fully expanded bar.
 *
 * The collapse waits for the bar's height limit to be measured (it is not known until the first layout), happens at most
 * once, and never after the user has scrolled the body: from then on the bar follows the user as it always does.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberCollapsedForScrolledList(behavior: TopAppBarScrollBehavior, listState: LazyListState): Modifier {
    var userScrolled by remember(behavior, listState) { mutableStateOf(false) }
    val touch = remember(behavior, listState) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput) userScrolled = true
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(behavior, listState) {
        snapshotFlow {
            val limit = behavior.state.heightOffsetLimit
            // Unmeasured, the limit is a huge negative placeholder; measured it is the bar's collapsible height.
            val measured = limit < 0f && limit > UNMEASURED_LIMIT
            val scrolled = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            measured && scrolled && !userScrolled
        }.first { it }
        behavior.state.heightOffset = behavior.state.heightOffsetLimit
    }
    return Modifier.nestedScroll(touch)
}

/** Half of the placeholder limit a bar reports before it is measured, so only a measured limit lies above it. */
private const val UNMEASURED_LIMIT = -Float.MAX_VALUE / 2
