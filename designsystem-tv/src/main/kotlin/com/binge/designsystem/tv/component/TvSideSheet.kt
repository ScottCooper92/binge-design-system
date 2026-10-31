package com.binge.designsystem.tv.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.TvOverlayCloser
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvExitFocusGroup
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.tv.focus.tvStartDirectionKey
import com.binge.designsystem.tv.theme.TV_OVERLAY_SCRIM_ALPHA
import com.binge.designsystem.tv.theme.tvStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * An end-edge, focus-trapped sheet over a scrim, at [TV_OVERLAY_SCRIM_ALPHA]: the ten-foot counterpart of a phone's bottom sheet, for a
 * row's actions or a choice. Back, and the key pointing away from the panel, both call [onDismissRequest].
 * The key is read as the event bubbles up, so a control inside that uses it itself, a stepper that lowers a value
 * or a text field moving its cursor, consumes it first and the sheet stays open.
 *
 * [content] gets the entry requester. Each step it shows pins the requester to its first row and pulls it
 * there with [TvSideSheetStepFocus]. The caller owns the visibility flag, and returns focus to what opened
 * the sheet, with `rememberTvOverlayCloser`; pass the closer as [closer] so the sheet lets focus go as it closes and traps again once it is gone.
 * [toBottomEdge] is the panel's: see [TvSideSheetPanel].
 */
@Composable
fun TvSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
    closer: TvOverlayCloser? = null,
    content: @Composable ColumnScope.(entryFocus: FocusRequester) -> Unit,
) = TvSideSheetHost(onDismissRequest, modifier, toBottomEdge, closer, anchor = null, content)

/** [TvSideSheet] with the panel's [anchor], which a stack holds focus on while one page replaces another. */
@Composable
internal fun TvSideSheetHost(
    onDismissRequest: () -> Unit,
    modifier: Modifier,
    toBottomEdge: Boolean,
    closer: TvOverlayCloser?,
    anchor: FocusRequester?,
    content: @Composable ColumnScope.(entryFocus: FocusRequester) -> Unit,
) {
    val entryFocus = remember { FocusRequester() }
    val dismissKey = tvStartDirectionKey()
    BackHandler(onBack = onDismissRequest)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = TV_OVERLAY_SCRIM_ALPHA))
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == dismissKey) {
                        onDismissRequest()
                        true
                    } else if (anchor != null && event.key == Key.Back) {
                        // The anchor makes the panel a focusable ancestor, which can swallow a Back that nothing below it handles; take it here.
                        if (event.type == KeyEventType.KeyDown) onDismissRequest()
                        true
                    } else {
                        false
                    }
                },
    ) {
        TvSideSheetPanelHost(
            modifier = Modifier.align(Alignment.CenterEnd),
            toBottomEdge = toBottomEdge,
            closer = closer,
            anchor = anchor,
        ) { content(entryFocus) }
    }
}

/**
 * Pulls the sheet's entry focus to the step that has just come into composition: the rows when the sheet
 * opens, and again when Cancel on a confirm step brings them back. It runs from the step, not the sheet,
 * because swapping a step recomposes the rows without the sheet itself re-entering.
 */
@Composable
fun TvSideSheetStepFocus(entryFocus: FocusRequester) {
    TvOverlayArrivalFocusEffect(entryFocus)
}

/**
 * The sheet's visible panel, with no scrim and no focus request of its own, so a frame can render it. A step
 * placed inside brings its own request: [TvSideSheetConfirm] calls [TvSideSheetStepFocus] when given an `entryFocus`,
 * so it pulls focus to Cancel wherever it composes; on a stack page the stack does that. The panel is flush to the screen's end, top and bottom edges, so its content keeps
 * the overscan margin from them. [toBottomEdge] drops the bottom margin, for content that scrolls: it runs to the screen's
 * edge, and [TvSideSheetRows] carries the bottom overscan inside its scroll, so its last row can still scroll clear of the edge.
 * Given the [closer] that closes it, the panel stops trapping focus as the close starts and traps again once it is disposed.
 */
@Composable
fun TvSideSheetPanel(
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
    closer: TvOverlayCloser? = null,
    content: @Composable ColumnScope.() -> Unit,
) = TvSideSheetPanelHost(modifier, toBottomEdge, closer, anchor = null, content)

/**
 * [TvSideSheetPanel] with an [anchor]: a focus target on the panel itself, inside its trap, that holds focus while rows
 * are swapped so none of them takes it by chance as the old ones dispose. It encloses every row, so a directional search
 * never lands on it.
 */
@Composable
internal fun TvSideSheetPanelHost(
    modifier: Modifier,
    toBottomEdge: Boolean,
    closer: TvOverlayCloser?,
    anchor: FocusRequester?,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (closer != null) {
        DisposableEffect(closer) {
            closer.overlayShown()
            onDispose { closer.overlayGone() }
        }
    }
    val wash = remember { TvSideSheetWash() }
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(dimensionResource(TvR.dimen.tv_side_sheet_width))
                .background(MaterialTheme.tvStyle.panel)
                .tvSideSheetWashBehind(wash)
                .tvExitFocusGroup { closer?.trapping ?: true }
                .then(if (anchor == null) Modifier else Modifier.focusRequester(anchor).focusable())
                .padding(
                    start = dimensionResource(DesR.dimen.padding_l),
                    top = dimensionResource(TvR.dimen.tv_overscan_vertical),
                    end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                ).then(if (toBottomEdge) Modifier else Modifier.padding(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical))),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        CompositionLocalProvider(LocalTvSideSheetToBottomEdge provides toBottomEdge, LocalTvSideSheetWash provides wash) { content() }
    }
}

/**
 * The sheet's heading: what its rows act on. On a page opened over another in a [TvSideSheetStack] it carries [TvSideSheetBack]
 * above its text, which names the page Back returns to.
 */
@Composable
fun TvSideSheetTitle(text: String, modifier: Modifier = Modifier) {
    val page = LocalTvSideSheetPage.current
    SideEffect { page?.onTitle(text) }
    Column(modifier = modifier.padding(bottom = dimensionResource(DesR.dimen.padding_xs))) {
        TvSideSheetBack()
        SideSheetTitleText(text, Modifier)
    }
}

@Composable
private fun SideSheetTitleText(text: String, modifier: Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    )
}

/** What a page in a [TvSideSheetStack] knows of the stack: the title of the page it was opened from, and where to report its own. */
internal class TvSideSheetPage(
    val parentTitle: String?,
    val onTitle: (String) -> Unit,
)

internal val LocalTvSideSheetPage = staticCompositionLocalOf<TvSideSheetPage?> { null }

/** A line under the title, such as what a confirm step is about to do. */
@Composable
fun TvSideSheetBody(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(bottom = dimensionResource(DesR.dimen.padding_xs)),
    )
}

/**
 * One row. Focus is the fill. A [destructive] row rests in the error colour, so it reads as one before it is
 * reached, and fills with it when focused, as a destructive button does. [trailing] ends the row, such as a switch or a short
 * value, and is told whether the row is focused so it can match the fill. It is held to `tv_side_sheet_trailing_max_width`, so
 * at a large font a value cannot take the label's room: give a text value one line and an ellipsis. A row that [opensPage],
 * one that pushes a page on a [TvSideSheetStack], ends in a chevron, after the trailing value and the tick. [role] is what a screen reader calls the row: `Role.Switch` for a row
 * ending in a [TvSwitch], whose on or off state the row then announces with it. A [selected] row carries a tick, for a sheet that is a choice. [enabled] false dims a control that cannot
 * be used yet and keeps it out of the focus order; a fact the sheet shows is a [TvSideSheetReadout]. [initiallyFocused] draws the focused state, for a frame.
 *
 * The [label] holds to one line with an ellipsis, as every TV row's does. [supportingText] is a second line under it, held to one line with an ellipsis: what the row does. [leading] replaces [icon] with a visual of the caller's, such
 * as a poster thumbnail for a sheet about one title; size it with `tv_side_sheet_thumbnail_width` and `_height`.
 * An [icon] sits on its tinted plate, in the error colour for a [destructive] row and dimmed for a disabled one.
 * [reserveSupportingLine] keeps that line's height when [supportingText] is null, so a row with nothing to say there
 * stays as tall as neighbours that have something, such as a count. The label then centres in that height, level with the
 * icon plate, instead of sitting at the top of it.
 *
 * While [saving], a spinner holds the tick's place, in the tick's box, after the trailing value and before the chevron, and
 * the row announces that it is in progress: for a press that applies at once but takes a moment to land. The caller decides
 * what a press does meanwhile.
 */
@Composable
fun TvSideSheetRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    selected: Boolean = false,
    destructive: Boolean = false,
    enabled: Boolean = true,
    initiallyFocused: Boolean = false,
    supportingText: String? = null,
    leading: (@Composable () -> Unit)? = null,
    reserveSupportingLine: Boolean = false,
    role: Role = Role.Button,
    opensPage: Boolean = false,
    saving: Boolean = false,
    note: TvSideSheetNote? = null,
    trailing: (@Composable (isFocused: Boolean) -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val savingDescription = stringResource(DesR.string.cd_list_item_loading)
    val resting =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
            destructive -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        }
    val style = MaterialTheme.tvStyle
    val focusedDestructive = focused && destructive
    val content = if (focusedDestructive) style.onDestructiveFocusContainer else tvFocusContentColor(isFocused = focused, resting = resting)
    val supporting =
        if (focusedDestructive) {
            style.onDestructiveFocusContainer
        } else {
            tvFocusContentColor(
                isFocused = focused,
                resting = if (enabled && !destructive) MaterialTheme.colorScheme.onSurfaceVariant else resting,
            )
        }
    val clickable =
        if (enabled) {
            Modifier.tvClickable(role = role, onFocusChanged = { focused = it }, onClick = onClick)
        } else {
            Modifier
        }
    SideSheetRowLayout(
        label = label,
        content = content,
        supporting = supporting,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(BingeShapes.TvListItem)
                .tvFocusFill(
                    isFocused = focused,
                    shape = BingeShapes.TvListItem,
                    fillColor = if (destructive) style.destructiveFocusContainer else style.focusContainer,
                ).then(clickable)
                .then(if (selected) Modifier.semantics { this.selected = true } else Modifier)
                .then(if (saving) Modifier.semantics { stateDescription = savingDescription } else Modifier),
        icon = icon,
        iconTint =
            when {
                !enabled -> resting
                destructive -> MaterialTheme.colorScheme.error
                else -> style.rowIcon
            },
        focusedIconTint = if (destructive) style.onDestructiveFocusContainer else style.onFocusContainer,
        isFocused = focused,
        selected = selected,
        supportingText = supportingText,
        leading = leading,
        reserveSupportingLine = reserveSupportingLine,
        trailing = trailing?.let { slot -> { slot(focused) } },
        chevronTint = if (opensPage) supporting else null,
        saving = saving,
        note = note,
        noteColor = if (note?.warning == true && enabled && !focused) MaterialTheme.colorScheme.error else supporting,
    )
}

/**
 * A fact a [TvSideSheetRow] shows on a line of its own under the second line: a licence, say, which the
 * one-line label must not cut off. It takes one line and ellipsizes, so keep [text] short. It leads with an optional [icon], and rests in the error colour when it is a
 * [warning]; focused, it takes the fill's content colour like the rest of the row.
 */
@Immutable
data class TvSideSheetNote(
    val text: String,
    val icon: ImageVector? = null,
    val warning: Boolean = false,
)

/**
 * A read-out in a side sheet: a fact the sheet shows, such as a server's address or version, rather than a control. It stays out
 * of the focus order, as a disabled [TvSideSheetRow] does, but draws at full contrast, since the value is what the row is for.
 * A screen reader reads the [label] and [supportingText] together, as one item.
 */
@Composable
fun TvSideSheetReadout(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    supportingText: String? = null,
    leading: (@Composable () -> Unit)? = null,
    reserveSupportingLine: Boolean = false,
) {
    SideSheetRowLayout(
        label = label,
        content = MaterialTheme.colorScheme.onSurface,
        supporting = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) {},
        icon = icon,
        iconTint = MaterialTheme.tvStyle.rowIcon,
        isFocused = false,
        selected = false,
        supportingText = supportingText,
        leading = leading,
        reserveSupportingLine = reserveSupportingLine,
    )
}

/**
 * The row both [TvSideSheetRow] and [TvSideSheetReadout] draw: what leads, the label and its second line, then the trailing
 * slot, the tick or, while [saving], the spinner in its place, and, given a [chevronTint], the chevron of a row that opens a page.
 */
@Composable
private fun SideSheetRowLayout(
    label: String,
    content: Color,
    supporting: Color,
    modifier: Modifier,
    icon: ImageVector?,
    iconTint: Color,
    isFocused: Boolean,
    selected: Boolean,
    supportingText: String?,
    leading: (@Composable () -> Unit)?,
    reserveSupportingLine: Boolean,
    trailing: (@Composable () -> Unit)? = null,
    focusedIconTint: Color = MaterialTheme.tvStyle.onFocusContainer,
    chevronTint: Color? = null,
    saving: Boolean = false,
    note: TvSideSheetNote? = null,
    noteColor: Color = supporting,
) {
    Row(
        modifier =
            modifier.padding(
                horizontal = dimensionResource(DesR.dimen.padding_sm),
                vertical = dimensionResource(DesR.dimen.padding_s),
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> leading()
            icon != null ->
                TvRowIcon(
                    icon = icon,
                    tint = iconTint,
                    isFocused = isFocused,
                    focusedTint = focusedIconTint,
                )
        }
        if (supportingText == null && note == null && reserveSupportingLine) {
            // The reserved line is ghost text that only sets the height, so the label centres in the row against the icon plate
            // rather than sitting at the top of the two lines.
            Box(modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.clearAndSetSemantics {}) {
                    Text(text = "", style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(text = "", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.CenterStart),
                )
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (supportingText != null) {
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = supporting,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                note?.let { RowNote(it, noteColor) }
            }
        }
        if (trailing != null) {
            Box(
                modifier = Modifier.widthIn(max = dimensionResource(TvR.dimen.tv_side_sheet_trailing_max_width)),
                contentAlignment = Alignment.CenterEnd,
            ) { trailing() }
        }
        when {
            saving ->
                Box(modifier = Modifier.size(dimensionResource(TvR.dimen.tv_selected_tick_size)), contentAlignment = Alignment.Center) {
                    TvSpinner(
                        color = content,
                        diameter = dimensionResource(TvR.dimen.tv_list_pane_row_spinner),
                        stroke = dimensionResource(TvR.dimen.tv_list_pane_row_spinner_stroke),
                    )
                }
            selected -> TvSelectedTick(tint = content)
        }
        if (chevronTint != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = chevronTint,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_side_sheet_chevron_size)),
            )
        }
    }
}

@Composable
private fun RowNote(note: TvSideSheetNote, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        note.icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_side_sheet_note_icon_size)),
            )
            Spacer(Modifier.width(dimensionResource(DesR.dimen.padding_xs)))
        }
        Text(
            text = note.text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A sheet's second step, for an action that deletes or blocks: what is about to happen, then the safe answer first and the
 * confirm last. Focus lands on the safe answer, so a stray OK does nothing; [cancelInitiallyFocused] draws that, for a frame.
 * The confirm row rests in the error colour; pass `destructive = false` for a step that asks first but destroys nothing.
 * [dismissLabel] is for a step whose safe answer is not "Cancel" ("Keep", for a request someone might withdraw).
 *
 * On a [TvSideSheetStack] page pass a null [entryFocus], `nav.row("cancel")` as [cancelModifier] and call `nav.arriveAt("cancel")`.
 *
 * @param entryFocus the request [TvSideSheet] hands its content. Null only on a [TvSideSheetStack] page: anywhere else
 * a null leaves the step taking no focus.
 */
@Composable
fun ColumnScope.TvSideSheetConfirm(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    entryFocus: FocusRequester?,
    cancelInitiallyFocused: Boolean = false,
    destructive: Boolean = true,
    cancelModifier: Modifier = Modifier,
    dismissLabel: String = stringResource(DesR.string.action_cancel),
) {
    TvSideSheetTitle(title)
    TvSideSheetBody(message)
    TvSideSheetRows {
        TvSideSheetRow(
            label = dismissLabel,
            onClick = onCancel,
            initiallyFocused = cancelInitiallyFocused,
            modifier = (if (entryFocus != null) Modifier.focusRequester(entryFocus) else Modifier).then(cancelModifier),
        )
        TvSideSheetRow(label = confirmLabel, onClick = onConfirm, destructive = destructive)
    }
    if (entryFocus != null) TvSideSheetStepFocus(entryFocus)
}
