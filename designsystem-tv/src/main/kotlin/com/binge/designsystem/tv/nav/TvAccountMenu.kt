package com.binge.designsystem.tv.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.zIndex
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.BingeTvInitialsAvatar
import com.binge.designsystem.tv.component.TvConfirmDialog
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.focus.tryRequestFocus
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.theme.BingeTvCorners
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** The question a [TvAccountMenuRow] asks before it runs, in the confirm dialog that opens over the menu. */
data class TvAccountMenuConfirm(
    val title: String,
    val message: String,
    val confirmLabel: String,
)

/**
 * One row of a [TvAccountMenu]: a [label] with an [icon]. A row with a [confirm] is destructive: it is drawn in the error
 * colour, goes after every other row whatever its place in the list, and asks first.
 */
data class TvAccountMenuRow(
    val key: Any,
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val confirm: TvAccountMenuConfirm? = null,
)

/** Which end of the bar the avatar sits at, and so which way the open menu grows from it. */
enum class TvAccountMenuAnchor { Start, End }

/**
 * The account avatar and the menu that opens from it, for a bar's trailing (or leading) slot.
 *
 * The menu is open while the avatar is focused or focus is inside it. It is laid over the avatar's own place, its header's
 * avatar on the anchor's, and it does not change the size the avatar reports, so the bar does not move. The header is the
 * [name] and [detail] beside the avatar; [extraContent] sits under it, for a line such as a quota. The [rows] follow.
 *
 * Keys. Down from the avatar enters the first row while the menu is open; while it is closed Down goes to [down], the
 * page below. Up from the first row returns to the avatar. Left and Right are not trapped: leaving the menu sideways closes it
 * because focus left. OK on the avatar enters the first row.
 *
 * Back, while the menu is open and focus is on the avatar or in the menu, closes the menu and puts focus on the avatar. The
 * menu then stays closed while the avatar keeps focus, and opens again only after focus has left the avatar and come back,
 * so a closed menu does not reopen at once. A second Back reaches whatever handles Back behind it. Nothing else moves.
 *
 * A destructive row asks first, in a dialog over the menu, which stays open under it. Dismissing the dialog puts focus back
 * on the row. Confirming runs the row's action and puts focus on the avatar with the menu held closed, so it never reopens over a
 * header still naming an account that has just been removed; the hold lifts when the header changes or focus leaves the avatar.
 *
 * [initiallyOpen] draws the menu open until focus first reaches it, for a frame, which fires no focus events; production passes
 * false.
 *
 * [selected] marks the avatar as the bar's current destination, for a host whose Account page is the one showing: the avatar's
 * ring is drawn faintly while focus is elsewhere (and on the header's avatar while the menu is open), where a selected tab in [BingeTvTopBar] keeps a faint tile, and a screen
 * reader reads it as selected. The focused ring is the same either way. The default draws and reads as it did before the parameter.
 *
 * [avatarFocusRequester], when set, is the requester the avatar uses in place of its own, so a host that routes entry focus
 * through a requester per bar item can land focus on the avatar. [onOpenChange] is called with the menu's new state each time
 * it opens or closes, so a host can hold its bar at full size meanwhile. The menu counts as open while its confirm dialog is up,
 * and a menu that leaves the composition while open reports closed. Both default to the behaviour without them.
 */
@Composable
fun TvAccountMenu(
    avatarLabel: String,
    name: String,
    rows: List<TvAccountMenuRow>,
    down: FocusRequester,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    detail: String? = null,
    anchor: TvAccountMenuAnchor = TvAccountMenuAnchor.End,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    initiallyOpen: Boolean = false,
    avatarFocusRequester: FocusRequester? = null,
    onOpenChange: (Boolean) -> Unit = {},
    selected: Boolean = false,
) {
    var avatarFocused by remember { mutableStateOf(false) }
    var menuFocused by remember { mutableStateOf(false) }
    var confirming by remember { mutableStateOf<TvAccountMenuRow?>(null) }
    var refocusRow by remember { mutableStateOf<Any?>(null) }
    var heldClosed by remember { mutableStateOf(false) }
    var seededOpen by remember { mutableStateOf(initiallyOpen) }
    val ownAvatarRequester = remember { FocusRequester() }
    val avatarRequester = avatarFocusRequester ?: ownAvatarRequester
    val rowRequesters = remember { mutableMapOf<Any, FocusRequester>() }
    val ordered = remember(rows) { rows.filter { it.confirm == null } + rows.filter { it.confirm != null } }
    val firstRow = remember(ordered) {
        ordered.firstOrNull()?.let { rowRequesters.getOrPut(it.key) { FocusRequester() } }
    }
    val panelWidthDp = dimensionResource(TvR.dimen.tv_account_menu_width)
    val panelPadDp = dimensionResource(DesR.dimen.padding_m)
    val open = !heldClosed && (avatarFocused || menuFocused || confirming != null || seededOpen)

    val reportOpenChange by rememberUpdatedState(onOpenChange)
    var reportedOpen by remember { mutableStateOf(open) }
    LaunchedEffect(open) {
        if (open != reportedOpen) {
            reportedOpen = open
            reportOpenChange(open)
        }
    }
    DisposableEffect(Unit) { onDispose { if (reportedOpen) reportOpenChange(false) } }

    // The hold lifts once focus has left the avatar and the menu, and when the header changes: a signed-out account has gone.
    LaunchedEffect(avatarFocused, menuFocused, confirming) {
        if (!avatarFocused && !menuFocused && confirming == null) heldClosed = false
    }
    LaunchedEffect(avatarFocused, menuFocused) { if (avatarFocused || menuFocused) seededOpen = false }
    LaunchedEffect(name, avatarUrl) { heldClosed = false }
    LaunchedEffect(refocusRow) {
        refocusRow?.let { rowRequesters[it]?.tryRequestFocus() }
        refocusRow = null
    }
    BackHandler(enabled = open) {
        heldClosed = true
        avatarRequester.tryRequestFocus()
    }

    Layout(
        modifier = modifier.zIndex(if (open) 1f else 0f),
        content = {
            AccountAvatar(
                label = avatarLabel,
                name = name,
                avatarUrl = avatarUrl,
                focused = avatarFocused,
                selected = selected,
                onFocusChanged = { avatarFocused = it },
                onClick = { if (open) firstRow?.tryRequestFocus() },
                modifier = Modifier
                    .layoutId(AccountSlot.Anchor)
                    .focusRequester(avatarRequester)
                    .focusProperties { this.down = if (open && firstRow != null) firstRow else down },
            )
            if (open) {
                AccountPanel(
                    name = name,
                    avatarUrl = avatarUrl,
                    detail = detail,
                    anchor = anchor,
                    selected = selected,
                    rows = ordered,
                    requesterFor = { key -> rowRequesters.getOrPut(key) { FocusRequester() } },
                    avatarRequester = avatarRequester,
                    extraContent = extraContent,
                    onRow = { row ->
                        if (row.confirm != null) confirming = row else row.onClick()
                    },
                    modifier = Modifier
                        .layoutId(AccountSlot.Panel)
                        .onFocusChanged { menuFocused = it.hasFocus },
                )
            }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val anchorPlaceable = measurables.first { it.layoutId == AccountSlot.Anchor }.measure(loose)
        val panelWidth = panelWidthDp.roundToPx()
        val panelPad = panelPadDp.roundToPx()
        val panel = measurables
            .firstOrNull { it.layoutId == AccountSlot.Panel }
            ?.measure(Constraints(minWidth = panelWidth, maxWidth = panelWidth, maxHeight = Constraints.Infinity))
        layout(anchorPlaceable.width, anchorPlaceable.height) {
            anchorPlaceable.placeRelative(0, 0)
            // The panel's own padding is what the header's avatar sits inside, so the panel starts that far outside the anchor:
            // its avatar then lies exactly on this one.
            panel?.placeRelative(
                x = if (anchor == TvAccountMenuAnchor.End) anchorPlaceable.width - panel.width + panelPad else -panelPad,
                y = -panelPad,
            )
        }
    }

    confirming?.let { row ->
        val confirm = row.confirm ?: return@let
        TvConfirmDialog(
            title = confirm.title,
            message = confirm.message,
            confirmLabel = confirm.confirmLabel,
            destructive = true,
            onConfirm = {
                confirming = null
                heldClosed = true
                row.onClick()
                avatarRequester.tryRequestFocus()
            },
            onDismissRequest = {
                confirming = null
                refocusRow = row.key
            },
        )
    }
}

private enum class AccountSlot { Anchor, Panel }

/** The selected avatar's ring: the bar's faint tile alpha would not read on a ring this thin, so it is stronger. */
private const val SELECTED_RING_ALPHA = 0.5f

@Composable
private fun AccountAvatar(
    label: String,
    name: String,
    avatarUrl: String?,
    focused: Boolean,
    selected: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val slot = dimensionResource(TvR.dimen.tv_icon_button_diameter)
    val diameter = dimensionResource(TvR.dimen.tv_top_bar_avatar_size)
    val ring = dimensionResource(TvR.dimen.tv_top_bar_avatar_ring)
    val onSurface = MaterialTheme.colorScheme.onSurface
    val ringColor = when {
        focused -> onSurface
        selected -> onSurface.copy(alpha = SELECTED_RING_ALPHA)
        else -> Color.Transparent
    }
    // The avatar is smaller than a button but sits in a button-sized slot, so the bar's height and the open panel's avatar
    // (which is drawn over this slot) keep their places.
    Box(modifier = modifier.size(slot), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(diameter)
                .clip(BingeShapes.Pill)
                .border(ring, ringColor, BingeShapes.Pill)
                .tvClickable(role = Role.Button, onFocusChanged = onFocusChanged, onClick = onClick)
                .clearAndSetSemantics {
                    contentDescription = label
                    // Only a selected avatar says so, so one that is never selected reads as it always has.
                    if (selected) this.selected = true
                }.padding(ring),
            contentAlignment = Alignment.Center,
        ) {
            BingeTvInitialsAvatar(name = name, size = diameter - ring * 2, avatarUrl = avatarUrl)
        }
    }
}

@Composable
private fun AccountPanel(
    name: String,
    avatarUrl: String?,
    detail: String?,
    anchor: TvAccountMenuAnchor,
    selected: Boolean,
    rows: List<TvAccountMenuRow>,
    requesterFor: (Any) -> FocusRequester,
    avatarRequester: FocusRequester,
    extraContent: (@Composable ColumnScope.() -> Unit)?,
    onRow: (TvAccountMenuRow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val diameter = dimensionResource(TvR.dimen.tv_icon_button_diameter)
    val ring = dimensionResource(TvR.dimen.tv_top_bar_avatar_ring)
    Column(
        modifier = modifier
            .clip(BingeTvCorners.Small)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(dimensionResource(DesR.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
    ) {
        val avatar = @Composable {
            // The open menu keeps the selected avatar's ring, so the bar's current destination does not vanish under it.
            val selectedRing =
                if (selected) {
                    Modifier.border(ring, MaterialTheme.colorScheme.onSurface.copy(alpha = SELECTED_RING_ALPHA), BingeShapes.Pill)
                } else {
                    Modifier
                }
            Box(Modifier.size(diameter).then(selectedRing).padding(ring)) {
                BingeTvInitialsAvatar(name = name, size = diameter - ring * 2, avatarUrl = avatarUrl)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(DesR.dimen.padding_s)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
            // Top, not centre: at a large font the words grow, and the avatar must stay on the anchor's own place.
            verticalAlignment = Alignment.Top,
        ) {
            if (anchor == TvAccountMenuAnchor.End) {
                AccountWords(name, detail)
                avatar()
            } else {
                avatar()
                AccountWords(name, detail)
            }
        }
        extraContent?.invoke(this)
        rows.forEachIndexed { index, row ->
            TvSideSheetRow(
                label = row.label,
                icon = row.icon,
                destructive = row.confirm != null,
                onClick = { onRow(row) },
                modifier = Modifier
                    .focusRequester(requesterFor(row.key))
                    .then(if (index == 0) Modifier.focusProperties { up = avatarRequester } else Modifier),
            )
        }
    }
}

@Composable
private fun RowScope.AccountWords(name: String, detail: String?) {
    Column(Modifier.weight(1f)) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
