package com.binge.designsystem.tv.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.focus.tvClickable
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
 * other as a pill), [items] as tabs centred, and [footer] at the end. A focused item is a light pill with dark text, and the
 * tab for [selectedKey] keeps a faint pill while focus is elsewhere. Only the centred tabs scale, from 0.8 at rest to full
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
            Row(
                modifier = Modifier.layoutId(TvTopBarSlot.Tabs).graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
                horizontalArrangement = spacing,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    TvTopBarEntry(
                        item = item,
                        selected = (item.key == selectedKey).takeUnless { item.key in actionKeys },
                        focused = item.key == focusedKey,
                        iconOnly = item.key in iconOnlyKeys,
                        onFocusChanged = { onItemFocusChanged(item.key, it) },
                        onClick = { onSelect(item.key) },
                        // Tabs share what width there is, each up to its own: none is starved to nothing by the ones before it.
                        modifier = itemModifier(item).weight(1f, fill = false),
                    )
                }
            }
            if (footer != null) {
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
        // The ends are measured as they are, a 52dp avatar or a pill as wide as its label, and the tabs get what is left on
        // both sides of the wider one so they stay centred. A crowded bar then shortens its tabs instead of running under an end.
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val start = measurables.firstOrNull { it.layoutId == TvTopBarSlot.Start }?.measure(loose)
        val end = measurables.firstOrNull { it.layoutId == TvTopBarSlot.End }?.measure(loose)
        val clearance = if (start == null && end == null) 0 else endClearance.roundToPx()
        val reserved = maxOf(start?.width ?: 0, end?.width ?: 0) + clearance
        val tabs = measurables
            .first { it.layoutId == TvTopBarSlot.Tabs }
            .measure(loose.copy(maxWidth = (constraints.maxWidth - 2 * reserved).coerceAtLeast(0)))
        val height = maxOf(start?.height ?: 0, tabs.height, end?.height ?: 0)
        layout(constraints.maxWidth, height) {
            start?.placeRelative(0, (height - start.height) / 2)
            tabs.placeRelative((constraints.maxWidth - tabs.width) / 2, (height - tabs.height) / 2)
            end?.placeRelative(constraints.maxWidth - end.width, (height - end.height) / 2)
        }
    }
}

private enum class TvTopBarSlot { Start, Tabs, End }

/**
 * One bar entry: an avatar for an item that carries a name, an icon for one in the icon-only set, a label pill otherwise.
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
 * A tab as a text pill, or, when [iconOnly], as its icon alone; named for a screen reader either way. A null [selected] makes
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
            .clip(BingeShapes.Pill)
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
