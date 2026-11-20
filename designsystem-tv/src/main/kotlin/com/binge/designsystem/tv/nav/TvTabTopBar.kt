package com.binge.designsystem.tv.nav

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvEntryFocusGroup
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val TAB_RESTING_SCALE = 0.8f
private const val TAB_SCALE_MILLIS = 150
private const val CURRENT_TAB_ALPHA = 0.2f

/** The signed-in avatar's menu: its [rows], and [onOpenChange] to tell the scaffold when it opens or closes. */
class TvTopBarAccount(
    val rows: List<TvAccountMenuRow>,
    val onOpenChange: (Boolean) -> Unit,
)

/**
 * The tab bar across the top of a TV shell: [slots] with the account avatar at the start, the tabs centred and a button at the end.
 * A tab commits on click, not on focus. Entering the bar returns to the tab focus last left, [entryOverride] naming another
 * once. [holdOpen] keeps the tabs at full size and the bar above the page while the avatar's menu is open. Every key in
 * [iconOnlyKeys] draws its icon without a label. Down from any item lands on [down].
 *
 * @need show tabs across the top of a tv screen with an account avatar and a button at the ends
 * @group tv
 * @replaces a hand-built tv top bar
 */
@Composable
fun TvTabTopBar(
    slots: TvTopBarSlots,
    selectedKey: Any,
    requesters: Map<Any, FocusRequester>,
    down: FocusRequester,
    onSelect: (Any) -> Unit,
    onFocusedChange: (Boolean) -> Unit,
    holdOpen: Boolean,
    entryOverride: Any?,
    account: TvTopBarAccount?,
    iconOnlyKeys: Set<Any> = emptySet(),
) {
    var barFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (barFocused || holdOpen) 1f else TAB_RESTING_SCALE,
        animationSpec = if (LocalReduceMotion.current) snap() else tween(TAB_SCALE_MILLIS),
        label = "tvTopBarTabScale",
    )
    // Entering the bar returns to the tab focus last left: the selected one, unless the user left on another without opening it.
    var lastFocusedKey by remember(selectedKey) { mutableStateOf(selectedKey) }
    val entryKey = listOfNotNull(entryOverride, lastFocusedKey, selectedKey).firstOrNull { it in requesters } ?: slots.header.key
    val item = { entry: TvNavItem ->
        Modifier
            .onFocusChanged { if (it.hasFocus) lastFocusedKey = entry.key }
            .focusRequester(requesters.getValue(entry.key))
            .focusProperties { this.down = down }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // Above the content below it, which draws later: the account menu opens downward out of the bar's own bounds.
            .zIndex(if (holdOpen) 1f else 0f)
            .onFocusChanged {
                barFocused = it.hasFocus
                onFocusedChange(it.hasFocus)
            }.tvEntryFocusGroup(requesters.getValue(entryKey))
            .padding(
                top = dimensionResource(TvR.dimen.tv_overscan_vertical),
                bottom = dimensionResource(DesR.dimen.padding_s),
                start = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
            ),
    ) {
        val header = slots.header
        if (header.displayName != null && account != null) {
            TvAccountMenu(
                avatarLabel = header.label,
                name = header.displayName.orEmpty(),
                avatarUrl = header.avatarUrl,
                rows = account.rows,
                down = down,
                anchor = TvAccountMenuAnchor.Start,
                avatarFocusRequester = requesters.getValue(header.key),
                onOpenChange = account.onOpenChange,
                selected = header.key == selectedKey,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .reportingSize(dimensionResource(TvR.dimen.tv_top_bar_avatar_size))
                    .onFocusChanged { if (it.hasFocus) lastFocusedKey = header.key }
                    // Right out of the menu goes to the first tab, from the avatar and from every row alike.
                    .focusProperties { right = requesters.getValue(slots.items.first().key) },
            )
        } else {
            TopBarAvatar(
                entry = header,
                selected = header.key == selectedKey,
                onClick = { onSelect(header.key) },
                modifier = item(header).align(Alignment.CenterStart),
            )
        }
        Row(
            modifier = Modifier.align(Alignment.Center).graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            slots.items.forEach { entry ->
                TopBarItem(
                    entry = entry,
                    selected = entry.key == selectedKey,
                    onClick = { onSelect(entry.key) },
                    modifier = item(entry),
                    iconOnly = entry.key in iconOnlyKeys,
                )
            }
        }
        // The gear opens the Settings sheet and selects no destination, so it stays a button.
        TopBarItem(
            entry = slots.footer,
            selected = null,
            onClick = { onSelect(slots.footer.key) },
            modifier = item(slots.footer).align(Alignment.CenterEnd),
            iconOnly = true,
        )
    }
}

@Composable
private fun TopBarAvatar(
    entry: TvNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    TvTopBarAvatarSurface(
        entry = entry,
        selected = selected,
        isFocused = focused,
        modifier = modifier
            .tvClickable(
                role = Role.Tab,
                onFocusChanged = { focused = it },
                onClick = onClick,
            ).clearAndSetSemantics {
                contentDescription = entry.label
                this.selected = selected
            },
    )
}

/**
 * One of the bar's controls. A destination passes [selected] and is a tab, which the accessibility bridge reports as selected
 * (a button's selection reads as checked, #3994); null makes it a plain button.
 */
@Composable
private fun TopBarItem(
    entry: TvNavItem,
    selected: Boolean?,
    onClick: () -> Unit,
    iconOnly: Boolean,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val scheme = MaterialTheme.colorScheme
    val container = when {
        focused -> scheme.onSurface
        selected == true -> scheme.onSurface.copy(alpha = CURRENT_TAB_ALPHA)
        else -> Color.Transparent
    }
    val tint = if (focused) scheme.surface else scheme.onSurface
    // Sized as the detail tabs are, by its own padding, so the surface hugs the label and an icon gets a square.
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .tvClickable(role = if (selected == null) Role.Button else Role.Tab, onFocusChanged = { focused = it }, onClick = onClick)
            .clearAndSetSemantics {
                contentDescription = entry.label
                if (selected != null) this.selected = selected
            }.padding(
                horizontal = if (iconOnly) {
                    dimensionResource(
                        DesR.dimen.padding_s,
                    )
                } else {
                    dimensionResource(TvR.dimen.tv_tab_top_bar_item_padding_horizontal)
                },
                vertical = dimensionResource(DesR.dimen.padding_s),
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (iconOnly) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
            )
        } else {
            Text(text = entry.label, style = MaterialTheme.typography.titleMedium, color = tint, maxLines = 1, softWrap = false)
        }
    }
}

/**
 * Reports a square of [size] to the parent however large the content is, centring the content in it. The shared account menu
 * reserves a larger slot around its 36 dp avatar, which would make the signed-in bar taller than the signed-out one and push the
 * avatar in from the edge.
 */
private fun Modifier.reportingSize(size: Dp): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val reported = size.roundToPx()
        layout(reported, reported) { placeable.placeRelative((reported - placeable.width) / 2, (reported - placeable.height) / 2) }
    }
