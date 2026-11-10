package com.binge.designsystem.tv.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvFocusGroup
import com.binge.designsystem.tv.theme.BingeTvCorners
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** The tabs at rest, while focus is on the page: smaller, so the bar recedes behind what the user is reading. */
private const val TAB_RESTING_SCALE = 0.8f
private const val TAB_SCALE_MILLIS = 150

/** The current tab's pill while focus is elsewhere: there, but quieter than a focused item's. */
private const val CURRENT_TAB_ALPHA = 0.2f

/** The scrim under the bar, from nearly opaque at the top edge to clear, so a page's art shows through below it. */
private const val BAR_SCRIM_TOP_ALPHA = 0.9f

/**
 * The bar over every page: [header] at the start (an item with a [TvNavItem.displayName] draws as a circular avatar, any
 * other as a small-cornered tile), [items] as tabs centred, and [footer] at the end. A focused item is a light tile with dark text, and the
 * tab for [selectedKey] keeps a faint tile while focus is elsewhere. Only the centred tabs scale, from 0.8 at rest to full
 * size while the bar holds focus, so the two ends stay pinned.
 *
 * Focus never selects: OK on an item calls [onSelect] and nothing else does. Items named in
 * [iconOnlyKeys] (the header, an item or the footer) draw as their [TvNavItem.icon] alone; every other item is its
 * label on a single line, so a crowded bar never grows taller.
 * The tabs keep clear of whichever end item is wider, however wide its label. [TvNavItem.badge] is not drawn.
 *
 * A destination is a tab, so a screen reader reads its selection as selected (on a button it reads as checked). Items named
 * in [actionKeys] open something rather than navigate (a Settings sheet, say): those are buttons with no selection.
 *
 * Every item in [itemFocusRequesters] is attached to its requester, so the caller can send focus to a tab. Down from any item
 * lands on [down], the current page's own entry. [onFocusedChange] reports whether the bar holds focus.
 *
 * [trailing] replaces [footer] with arbitrary content at the end (an avatar and a gear, say, with a menu hanging off the avatar).
 * The caller owns that content's focus, but Down from it is the caller's too: give each focusable a `focusProperties { down = ... }`
 * as the bar's own items get. The bar lays the tabs out around the slot's width at rest, so an item in it must keep its size on
 * focus: a `TvIconButton` there passes `expandOnFocus = false`, or its labelled pill would push the avatar and squeeze the tabs.
 * With a [trailing] slot the whole bar is a focus group that restores the item last left, so Up from
 * the page resumes there, on a tab or on the avatar, instead of on the nearest one. With no [items] the bar draws no tabs and no rail,
 * only its ends.
 *
 * A tab keeps its own width, so its label is never cut while the bar has room for it: the tabs sit centred when they fit clear of the
 * wider end on both sides, else between the two ends. Only when even that is too narrow do the widest tabs shrink, to a common width.
 *
 * Until [visible] the bar is drawn clear and takes no focus, so it never shows a tab that is about to vanish. [scrim] fades
 * the bar's background from the theme's background at the top to clear, for a bar over page art; pass false to sit it on a
 * plain surface.
 */
@Composable
fun BingeTvTopBar(
    header: TvNavItem?,
    items: List<TvNavItem>,
    footer: TvNavItem?,
    selectedKey: Any,
    onSelect: (Any) -> Unit,
    itemFocusRequesters: Map<Any, FocusRequester>,
    down: FocusRequester,
    onFocusedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconOnlyKeys: Set<Any> = emptySet(),
    visible: Boolean = true,
    scrim: Boolean = true,
    actionKeys: Set<Any> = emptySet(),
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    var barFocused by remember { mutableStateOf(false) }
    var focusedKey by remember { mutableStateOf<Any?>(null) }
    TvTopBarSurface(
        header = header,
        items = items,
        footer = footer,
        selectedKey = selectedKey,
        barFocused = barFocused,
        focusedKey = focusedKey,
        onItemFocusChanged = { key, focused ->
            if (focused) {
                focusedKey = key
            } else if (focusedKey == key) {
                focusedKey = null
            }
        },
        onSelect = onSelect,
        itemFocusRequesters = itemFocusRequesters,
        down = down,
        iconOnlyKeys = iconOnlyKeys,
        visible = visible,
        scrim = scrim,
        actionKeys = actionKeys,
        trailing = trailing,
        modifier = modifier.onFocusChanged {
            barFocused = it.hasFocus
            onFocusedChange(it.hasFocus)
        },
    )
}

/**
 * The stateless bar: [barFocused] and [focusedKey] are handed in, so a screenshot can hold the bar in a focused state
 * without a focus request. [BingeTvTopBar] owns them in the app.
 */
@Composable
internal fun TvTopBarSurface(
    header: TvNavItem?,
    items: List<TvNavItem>,
    footer: TvNavItem?,
    selectedKey: Any,
    barFocused: Boolean,
    focusedKey: Any?,
    onItemFocusChanged: (Any, Boolean) -> Unit,
    onSelect: (Any) -> Unit,
    itemFocusRequesters: Map<Any, FocusRequester>,
    down: FocusRequester,
    iconOnlyKeys: Set<Any>,
    visible: Boolean,
    scrim: Boolean,
    modifier: Modifier = Modifier,
    actionKeys: Set<Any> = emptySet(),
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    val background = MaterialTheme.colorScheme.background
    val scale by animateFloatAsState(
        targetValue = if (barFocused) 1f else TAB_RESTING_SCALE,
        animationSpec = if (LocalReduceMotion.current) snap() else tween(TAB_SCALE_MILLIS),
        label = "tvTopBarTabScale",
    )
    val itemModifier = { item: TvNavItem ->
        val requester = itemFocusRequesters[item.key]
        (if (requester != null) Modifier.focusRequester(requester) else Modifier).focusProperties {
            canFocus = visible
            this.down = down
        }
    }
    val spacing = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s))
    val endClearance = dimensionResource(DesR.dimen.padding_m)
    Layout(
        modifier = modifier
            .fillMaxWidth()
            // With a trailing slot, focus arriving on the bar goes to the item last left, so leaving from an avatar returns to it.
            .then(if (trailing != null) Modifier.tvFocusGroup() else Modifier)
            .alpha(if (visible) 1f else 0f)
            .then(
                if (scrim) {
                    Modifier.background(Brush.verticalGradient(listOf(background.copy(alpha = BAR_SCRIM_TOP_ALPHA), Color.Transparent)))
                } else {
                    Modifier
                },
            ).padding(
                top = dimensionResource(TvR.dimen.tv_overscan_vertical),
                bottom = dimensionResource(DesR.dimen.padding_m),
                start = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
            ),
        content = {
            if (header != null) {
                TvTopBarEntry(
                    item = header,
                    selected = (header.key == selectedKey).takeUnless { header.key in actionKeys },
                    focused = header.key == focusedKey,
                    iconOnly = header.key in iconOnlyKeys,
                    onFocusChanged = { onItemFocusChanged(header.key, it) },
                    onClick = { onSelect(header.key) },
                    modifier = itemModifier(header).layoutId(TvTopBarSlot.Start),
                )
            }
            if (items.isNotEmpty()) {
                TvTopBarTabs(
                    spacing = dimensionResource(DesR.dimen.padding_s),
                    modifier = Modifier.layoutId(TvTopBarSlot.Tabs).graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                ) {
                    items.forEach { item ->
                        TvTopBarEntry(
                            item = item,
                            selected = (item.key == selectedKey).takeUnless { item.key in actionKeys },
                            focused = item.key == focusedKey,
                            iconOnly = item.key in iconOnlyKeys,
                            onFocusChanged = { onItemFocusChanged(item.key, it) },
                            onClick = { onSelect(item.key) },
                            modifier = itemModifier(item),
                        )
                    }
                }
            }
            if (trailing != null) {
                Row(
                    modifier = Modifier.layoutId(TvTopBarSlot.End),
                    horizontalArrangement = spacing,
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailing,
                )
            } else if (footer != null) {
                TvTopBarEntry(
                    item = footer,
                    selected = (footer.key == selectedKey).takeUnless { footer.key in actionKeys },
                    focused = footer.key == focusedKey,
                    iconOnly = footer.key in iconOnlyKeys,
                    onFocusChanged = { onItemFocusChanged(footer.key, it) },
                    onClick = { onSelect(footer.key) },
                    modifier = itemModifier(footer).layoutId(TvTopBarSlot.End),
                )
            }
        },
    ) { measurables, constraints ->
        // The ends are measured as they are, a 52dp avatar or a pill as wide as its label. The tabs keep their own width wherever it
        // fits: centred between the bar's edges when it fits clear of the wider end on both sides, else between the two ends, shifted off
        // centre. Only when even that is too narrow do the tabs shrink, the widest first (see TvTopBarTabs), so a label is never
        // cut while there is room for it.
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val start = measurables.firstOrNull { it.layoutId == TvTopBarSlot.Start }?.measure(loose)
        val end = measurables.firstOrNull { it.layoutId == TvTopBarSlot.End }?.measure(loose)
        val clearance = endClearance.roundToPx()
        val startEdge = if (start != null) start.width + clearance else 0
        val endEdge = constraints.maxWidth - if (end != null) end.width + clearance else 0
        val reserved = maxOf(start?.width ?: 0, end?.width ?: 0) + if (start == null && end == null) 0 else clearance
        val symmetricMax = (constraints.maxWidth - 2 * reserved).coerceAtLeast(0)
        val betweenMax = (endEdge - startEdge).coerceAtLeast(0)
        val tabsMeasurable = measurables.firstOrNull { it.layoutId == TvTopBarSlot.Tabs }
        val fitsCentred = tabsMeasurable == null || tabsMeasurable.maxIntrinsicWidth(loose.maxHeight) <= symmetricMax
        val tabs = tabsMeasurable?.measure(loose.copy(maxWidth = if (fitsCentred) symmetricMax else betweenMax))
        val height = maxOf(start?.height ?: 0, tabs?.height ?: 0, end?.height ?: 0)
        layout(constraints.maxWidth, height) {
            start?.placeRelative(0, (height - start.height) / 2)
            if (tabs != null) {
                val centred = (constraints.maxWidth - tabs.width) / 2
                val x = if (fitsCentred) centred else centred.coerceIn(startEdge, (endEdge - tabs.width).coerceAtLeast(startEdge))
                tabs.placeRelative(x, (height - tabs.height) / 2)
            }
            end?.placeRelative(constraints.maxWidth - end.width, (height - end.height) / 2)
        }
    }
}

/**
 * The tabs in a row, [spacing] apart. Each keeps its own width while the row has room for all of them. When it has not, the
 * widest are cut back to a common cap, the largest that fits, so a short label is left whole and no tab is starved to nothing
 * by the ones before it.
 */
@Composable
private fun TvTopBarTabs(
    spacing: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gap = spacing.roundToPx()
        val gaps = gap * (measurables.size - 1).coerceAtLeast(0)
        // A Measurable is measured once, so the natural widths come from the intrinsics and each tab is measured at the width it gets.
        val natural = measurables.map { it.maxIntrinsicWidth(constraints.maxHeight) }
        val total = natural.sum() + gaps
        val cap = if (total <= constraints.maxWidth) {
            Constraints.Infinity
        } else {
            val budget = (constraints.maxWidth - gaps).coerceAtLeast(0)
            var low = 0
            var high = natural.max()
            while (low < high) {
                val mid = (low + high + 1) / 2
                if (natural.sumOf { minOf(it, mid) } <= budget) low = mid else high = mid - 1
            }
            low
        }
        val placeables = measurables.mapIndexed { index, measurable ->
            measurable.measure(constraints.copy(minWidth = 0, maxWidth = minOf(natural[index], cap)))
        }
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(placeables.sumOf { it.width } + gaps, height) {
            var x = 0
            placeables.forEach { placeable ->
                placeable.placeRelative(x, (height - placeable.height) / 2)
                x += placeable.width + gap
            }
        }
    }
}

private enum class TvTopBarSlot { Start, Tabs, End }

/**
 * One bar entry: an avatar for an item that carries a name, an icon for one in the icon-only set, a label tile otherwise.
 * [selected] is null for an action, which is a button with no selection.
 */
@Composable
private fun TvTopBarEntry(
    item: TvNavItem,
    selected: Boolean?,
    focused: Boolean,
    iconOnly: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val displayName = item.displayName
    if (displayName != null) {
        TvTopBarAvatar(
            item = item,
            name = displayName,
            focused = focused,
            onFocusChanged = onFocusChanged,
            onClick = onClick,
            modifier = modifier,
        )
    } else {
        TvTopBarPill(
            item = item,
            selected = selected,
            focused = focused,
            iconOnly = iconOnly,
            onFocusChanged = onFocusChanged,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

/** The signed-in user's picture, or their initials, ringed in the focused pill's colour when focused. */
@Composable
private fun TvTopBarAvatar(
    item: TvNavItem,
    name: String,
    focused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val diameter = dimensionResource(TvR.dimen.tv_icon_button_diameter)
    val ring = dimensionResource(TvR.dimen.tv_top_bar_avatar_ring)
    Box(
        modifier = modifier
            .size(diameter)
            .clip(BingeShapes.Pill)
            .border(ring, if (focused) MaterialTheme.colorScheme.onSurface else Color.Transparent, BingeShapes.Pill)
            .tvClickable(role = Role.Button, onFocusChanged = onFocusChanged, onClick = onClick)
            .clearAndSetSemantics { contentDescription = item.label }
            .padding(ring),
        contentAlignment = Alignment.Center,
    ) {
        BingeTvInitialsAvatar(name = name, size = diameter - ring * 2, avatarUrl = item.avatarUrl)
    }
}

/**
 * A tab as a text tile, or, when [iconOnly], as its icon alone; named for a screen reader either way. A null [selected] makes
 * it an action's button.
 */
@Composable
private fun TvTopBarPill(
    item: TvNavItem,
    selected: Boolean?,
    focused: Boolean,
    iconOnly: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = when {
        focused -> scheme.onSurface
        selected == true -> scheme.onSurface.copy(alpha = CURRENT_TAB_ALPHA)
        else -> Color.Transparent
    }
    val content = if (focused) scheme.surface else scheme.onSurface
    Row(
        modifier = modifier
            .heightIn(min = dimensionResource(TvR.dimen.tv_icon_button_diameter))
            .clip(BingeTvCorners.Small)
            .background(container)
            .tvClickable(role = if (selected == null) Role.Button else Role.Tab, onFocusChanged = onFocusChanged, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = item.label
                if (selected != null) this.selected = selected
            }.padding(horizontal = dimensionResource(TvR.dimen.tv_top_bar_item_padding_horizontal)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconOnly) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
            )
        } else {
            // One line however crowded the bar: a wrapped label would grow the bar over the page below it.
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium,
                color = content,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
