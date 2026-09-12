package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarColors
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R

/**
 * Which colours the floating nav wears. The bar floats over whatever the screen is showing —
 * poster art as often as a plain surface — so the container has to separate itself from arbitrary
 * content, not just from the background token.
 */
enum class BingeNavFloatingTone {
    /** M3's surface-container tint. Reads as chrome, but sits only a tone step off the background. */
    Standard,

    /** M3's primary-tinted container — louder, and still a theme colour, so artwork can swallow it. */
    Vibrant,

    /**
     * Inverted surface: a dark container in the light theme and a light one in the dark theme. The
     * only tone whose contrast doesn't depend on what it happens to be floating over — and what
     * Google Photos uses over a photo grid.
     */
    HighContrast,

    /**
     * Dark in both themes: the inverted surface in the light theme, and the *lifted* surface
     * container in the dark one rather than the inversion, so the bar never flips to a light slab
     * on a dark UI. Contrast in dark mode comes from being a step lighter than the background plus
     * the outline, not from inverting.
     */
    AlwaysDark,

    /**
     * The standard container, lifted a tone step and given a hairline outline. Keeps the bar reading
     * as light chrome rather than an inverted slab, while still having a hard edge against a plain
     * background — the case a shadow alone doesn't carry.
     */
    Outlined,
}

/** Below this surface luminance the active scheme is a dark one. */
private const val DARK_SURFACE_LUMINANCE = 0.5f

/** Opacity of the inverted container — translucent enough to feel layered, opaque enough to stay legible. */
private const val NAV_FLOATING_CONTAINER_ALPHA = 0.92f

/** How a destination inside the floating container is drawn. */
enum class BingeNavFloatingStyle {
    /** Icon over label on every item, the bottom-bar arrangement carried into the pill. */
    Stacked,

    /**
     * Label-primary, Google Photos' arrangement: unselected destinations are text alone, and the
     * icon appears only on the selected item, inside its indicator pill.
     */
    TextFirst,

    /**
     * The inverse, and the one the shell defaults to: unselected destinations are icon-only, and the
     * selected one expands into an icon + label pill. Compact enough that the row fits without the
     * account item having to leave the container.
     */
    IconWithSelectedLabel,
}

/**
 * The M3 expressive `FloatingToolbar` pressed into service as the app's primary navigation,
 * replacing the bottom bar and the standard rail on every window bucket except an expanded one,
 * which keeps [BingeNavCustomRail].
 *
 * Off-spec by design: M3 scopes floating toolbars to contextual *actions*, not navigation, so the
 * container is Material but the items below are not [androidx.compose.material3.NavigationBarItem] —
 * see [NavFloatingItem] for what that costs.
 *
 * Unlike the docked bar this **overlays** [content] rather than reserving height for it, so the strip
 * it occupies is published through [LocalNavOverlayInsets] for scrollables to pad themselves by.
 *
 * That inset is computed rather than measured. A measured one arrives a frame late, and a scrollable
 * that has already settled at its maximum scroll never re-clamps, leaving the last row stranded under
 * the pill — which the `ScrolledToEnd` screenshot caught. The container's height is fixed by the
 * toolbar's minimum size, so the strip is known before first layout; the measurement is kept only as
 * an upward correction, should a style's items ever outgrow the default container.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BingeNavFloatingBarScaffold(
    items: List<BingeNavSuiteItem>,
    selectedKey: Any?,
    onSelect: (Any) -> Unit,
    tone: BingeNavFloatingTone,
    style: BingeNavFloatingStyle,
    content: @Composable () -> Unit,
) {
    val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
    val computed = FloatingToolbarDefaults.ContainerSize +
        FloatingToolbarDefaults.ScreenOffset +
        safeInsets.calculateBottomPadding()
    var measured by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    Box(Modifier.fillMaxSize()) {
        CompositionLocalProvider(LocalNavOverlayInsets provides PaddingValues(bottom = maxOf(computed, measured))) {
            content()
        }
        val colors = tone.resolve()
        val containerShape = FloatingToolbarDefaults.ContainerShape
        HorizontalFloatingToolbar(
            expanded = true,
            colors = colors.toolbar,
            shape = containerShape,
            expandedShadowElevation = dimensionResource(R.dimen.nav_floating_shadow_elevation),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { measured = with(density) { it.height.toDp() } }
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(bottom = FloatingToolbarDefaults.ScreenOffset)
                .then(
                    colors.outline?.let {
                        Modifier.border(dimensionResource(R.dimen.hairline_thickness), it, containerShape)
                    } ?: Modifier,
                ),
        ) {
            items.forEach { tab ->
                NavFloatingItem(
                    item = tab,
                    selected = selectedKey == tab.key,
                    onSelect = onSelect,
                    style = style,
                    colors = colors,
                )
            }
        }
    }
}

/**
 * One destination inside the floating container. Hand-rolled because `NavigationBarItem` can't be
 * borrowed here — it weights itself to fill an equal share of its parent Row, which would stretch a
 * wrap-content pill to the full window width. So the selected indicator, the icon/label arrangement
 * and the `Role.Tab` semantics that item gives for free are all restated here.
 */
@Composable
private fun NavFloatingItem(
    item: BingeNavSuiteItem,
    selected: Boolean,
    onSelect: (Any) -> Unit,
    style: BingeNavFloatingStyle,
    colors: NavFloatingColors,
) {
    val contentColor = if (selected) colors.onIndicator else LocalContentColor.current
    val modifier = Modifier
        .clip(MaterialTheme.shapes.large)
        .then(if (selected) Modifier.background(colors.indicator) else Modifier)
        .selectable(selected = selected, role = Role.Tab, onClick = { onSelect(item.key) })
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        when (style) {
            BingeNavFloatingStyle.Stacked -> StackedItemContent(item, modifier)
            BingeNavFloatingStyle.TextFirst -> TextFirstItemContent(item, selected, modifier)
            BingeNavFloatingStyle.IconWithSelectedLabel -> IconWithSelectedLabelItemContent(item, selected, modifier)
        }
    }
}

@Composable
private fun StackedItemContent(item: BingeNavSuiteItem, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(
            horizontal = dimensionResource(R.dimen.nav_floating_item_padding_h),
            vertical = dimensionResource(R.dimen.nav_floating_item_padding_v),
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.nav_floating_item_gap)),
    ) {
        NavSuiteItemIcon(item)
        Text(text = item.label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

/**
 * Label alone until selected, when the icon joins it inside the indicator. The account item keeps its
 * avatar in both states: the avatar carries the unread badge, which is the one item whose glyph says
 * something the label can't, so dropping it while unselected would hide the signal it exists for.
 */
@Composable
private fun TextFirstItemContent(
    item: BingeNavSuiteItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(
            horizontal = dimensionResource(R.dimen.nav_floating_text_item_padding_h),
            vertical = dimensionResource(R.dimen.nav_floating_text_item_padding_v),
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.nav_floating_text_item_gap)),
    ) {
        if (selected || item.avatarName != null) {
            NavSuiteItemIcon(item)
        }
        Text(text = item.label, style = MaterialTheme.typography.titleSmall)
    }
}

/**
 * Icon alone until selected, when the label joins it inside the indicator. Unlike [TextFirstItemContent]
 * the account avatar needs no exception here — every item shows its glyph in both states, so the
 * unread badge is never hidden.
 */
@Composable
private fun IconWithSelectedLabelItemContent(
    item: BingeNavSuiteItem,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(
            horizontal = dimensionResource(R.dimen.nav_floating_text_item_padding_h),
            vertical = dimensionResource(R.dimen.nav_floating_text_item_padding_v),
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.nav_floating_text_item_gap)),
    ) {
        NavSuiteItemIcon(item)
        if (selected) {
            Text(text = item.label, style = MaterialTheme.typography.titleSmall)
        }
    }
}

/**
 * The container colours plus the indicator pair, resolved together. They can't be picked
 * independently: the indicator has to contrast with *its own* container, and a `secondaryContainer`
 * indicator is invisible inside the vibrant container, which is the same colour family.
 */
private data class NavFloatingColors(
    val toolbar: FloatingToolbarColors,
    val indicator: Color,
    val onIndicator: Color,
    val outline: Color? = null,
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BingeNavFloatingTone.resolve(): NavFloatingColors {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        BingeNavFloatingTone.Standard ->
            NavFloatingColors(
                toolbar = FloatingToolbarDefaults.standardFloatingToolbarColors(),
                indicator = scheme.secondaryContainer,
                onIndicator = scheme.onSecondaryContainer,
            )
        BingeNavFloatingTone.Vibrant ->
            NavFloatingColors(
                toolbar = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
                indicator = scheme.primary,
                onIndicator = scheme.onPrimary,
            )
        BingeNavFloatingTone.Outlined ->
            NavFloatingColors(
                toolbar = FloatingToolbarDefaults.standardFloatingToolbarColors().copy(
                    toolbarContainerColor = scheme.surfaceContainerHighest,
                ),
                indicator = scheme.secondaryContainer,
                onIndicator = scheme.onSecondaryContainer,
                outline = scheme.outlineVariant,
            )
        BingeNavFloatingTone.AlwaysDark -> {
            // Read darkness off the scheme rather than isSystemInDarkTheme(): the theme owns that
            // decision and a caller may override it, but surface's luminance is always the truth.
            val darkTheme = scheme.surface.luminance() < DARK_SURFACE_LUMINANCE
            NavFloatingColors(
                toolbar = FloatingToolbarDefaults.standardFloatingToolbarColors().copy(
                    // Translucent only in the light theme. A dark pill over a light background loses
                    // nothing to 8% alpha, but a dark pill over dark artwork does: bright cells bleed
                    // through and the container stops reading as a solid object.
                    toolbarContainerColor = if (darkTheme) {
                        scheme.surfaceContainerHighest
                    } else {
                        scheme.inverseSurface.copy(alpha = NAV_FLOATING_CONTAINER_ALPHA)
                    },
                    toolbarContentColor = if (darkTheme) scheme.onSurface else scheme.inverseOnSurface,
                ),
                indicator = scheme.secondaryContainer,
                onIndicator = scheme.onSecondaryContainer,
                // Only in the dark theme, where a dark pill on a dark background needs the edge that
                // inversion would otherwise have provided.
                outline = if (darkTheme) scheme.outlineVariant else null,
            )
        }
        BingeNavFloatingTone.HighContrast ->
            NavFloatingColors(
                toolbar = FloatingToolbarDefaults.standardFloatingToolbarColors().copy(
                    // Slightly translucent so content reads as passing *under* the bar. Only an
                    // inverted container can afford this: a translucent surface-toned container
                    // blends into the background it is meant to separate from.
                    toolbarContainerColor = scheme.inverseSurface.copy(alpha = NAV_FLOATING_CONTAINER_ALPHA),
                    toolbarContentColor = scheme.inverseOnSurface,
                ),
                // The same secondaryContainer indicator the other tones use: it already reads as
                // "selected" everywhere else in the app, and being a light tone it separates from the
                // inverted container without needing a second accent colour.
                indicator = scheme.secondaryContainer,
                onIndicator = scheme.onSecondaryContainer,
            )
    }
}
