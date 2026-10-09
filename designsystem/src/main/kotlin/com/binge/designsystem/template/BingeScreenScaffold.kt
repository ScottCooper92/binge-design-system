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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.binge.designsystem.component.OverlaidHeaderContent
import com.binge.designsystem.paneBackOrNull
import com.binge.designsystem.paneSideInsets
import com.binge.designsystem.resolvedContentInset
import com.binge.designsystem.resolvedContentPadding

/** Which top bar a [BingeScreenScaffold] carries. */
enum class ScreenBar {
    /** A medium bar whose title collapses as the body scrolls, and a two-row bar beside another pane. */
    Collapsing,

    /** A single-line bar that slides away as the body scrolls down and returns as it scrolls up. */
    Small,

    /** A large collapsing bar whose expanded heading carries a subtitle line and a trailing control. */
    Large,

    /** No bar: the body reaches the top of the window and clears the status bar itself. */
    None,
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
 * [scrollBehavior] replaces the one the template picks from [bar], for a caller that owns the bar's state; it
 * is ignored for [ScreenBar.None]. [subtitle] and [titleTrailing] are [ScreenBar.Large]'s, and show while it is
 * expanded; a caller whose action takes over once it collapses hoists [scrollBehavior] to fade it.
 *
 * [bottomBar] reaches the true edge of the window and clears the navigation bar itself, as `FormFooter` does.
 *
 * [content] is handed padding that already clears the bar, the header, the system bars, a cutout and a
 * floating navigation bar. Split it with [screenOuterPadding] and [screenInnerPadding], or use [ScreenBody].
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
    scrollBehavior: TopAppBarScrollBehavior? = null,
    subtitle: String? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (padding: PaddingValues) -> Unit,
) {
    val ownBehavior = rememberScreenBarScrollBehavior(bar)
    val behavior = if (bar == ScreenBar.None) null else scrollBehavior ?: ownBehavior
    val scrim = behavior?.let { if (bar == ScreenBar.Small) it.state.overlappedFraction else it.state.collapsedFraction } ?: 0f
    val back = onBack?.let { paneBackOrNull(it) }
    var bottomBarHeight by remember { mutableIntStateOf(0) }
    // What an ancestor already reserved, such as the custom rail's start inset, is not reserved again (#372).
    var consumed by remember { mutableStateOf(WindowInsets(0, 0, 0, 0)) }
    Scaffold(
        modifier = (behavior?.let { modifier.nestedScroll(it.nestedScrollConnection) } ?: modifier)
            .onConsumedWindowInsetsChanged { consumed = it },
        contentWindowInsets = WindowInsets(0),
        topBar = {
            val barScrimFraction = if (barScrim && header == null) scrim else 0f
            when (bar) {
                ScreenBar.Collapsing ->
                    BingePaneTopBar(
                        title = title,
                        onBack = back,
                        scrollBehavior = behavior,
                        containerColor = Color.Transparent,
                        scrimFraction = barScrimFraction,
                        foregroundScrimFraction = scrim,
                        actions = actions,
                    )
                ScreenBar.Small ->
                    BingeTopBar(
                        title = title,
                        onBack = back,
                        scrollBehavior = behavior,
                        containerColor = Color.Transparent,
                        scrimFraction = barScrimFraction,
                        foregroundScrimFraction = scrim,
                        actions = actions,
                    )
                ScreenBar.Large ->
                    BingeLargeTopBar(
                        title = title,
                        subtitle = subtitle,
                        onBack = back,
                        scrollBehavior = behavior,
                        containerColor = Color.Transparent,
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
                    // Consumed as well as padded, so a header's chip row does not add the side insets again (#371).
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun rememberScreenBarScrollBehavior(bar: ScreenBar): TopAppBarScrollBehavior? =
    when (bar) {
        ScreenBar.Collapsing, ScreenBar.Large -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        ScreenBar.Small -> TopAppBarDefaults.enterAlwaysScrollBehavior()
        ScreenBar.None -> null
    }

@Composable
private fun snackbarNavigationInset(): Dp {
    val overlay = LocalNavOverlayInsets.current.calculateBottomPadding()
    val navigation = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return max(overlay, navigation) - overlay
}

/**
 * The one insets policy. The sides clear this pane's system bars and cutout, less what an ancestor [consumed], plus a
 * navigation rail drawn over it. The bottom clears a pinned bottom bar when there is one, or else the floating navigation bar or the
 * gesture bar, whichever is taller: inside the shell the floating bar's inset already covers the gesture bar.
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
        start = sides.calculateStartPadding(direction) + overlay.calculateStartPadding(direction),
        top = if (hasBar) scaffoldPadding.calculateTopPadding() else WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        end = sides.calculateEndPadding(direction) + overlay.calculateEndPadding(direction),
        bottom = if (bottomBar > dimensionResource(R.dimen.zero)) bottomBar else navigation,
    )
}
