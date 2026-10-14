package com.binge.designsystem.tv.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DISABLED_ALPHA
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.tvClickable
import com.binge.designsystem.tv.focus.tvExitFocusGroup
import com.binge.designsystem.tv.focus.tvFocusContentColor
import com.binge.designsystem.tv.focus.tvFocusFill
import com.binge.designsystem.tv.focus.tvStartDirectionKey
import com.binge.designsystem.tv.theme.TV_OVERLAY_SCRIM_ALPHA
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
 * the sheet, with `rememberTvOverlayCloser`. [toBottomEdge] is the panel's: see [TvSideSheetPanel].
 */
@Composable
fun TvSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
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
                    } else {
                        false
                    }
                },
    ) {
        TvSideSheetPanel(modifier = Modifier.align(Alignment.CenterEnd), toBottomEdge = toBottomEdge) { content(entryFocus) }
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
 */
@Composable
fun TvSideSheetPanel(
    modifier: Modifier = Modifier,
    toBottomEdge: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(dimensionResource(TvR.dimen.tv_side_sheet_width))
                .background(MaterialTheme.colorScheme.surface)
                .tvExitFocusGroup()
                .padding(
                    start = dimensionResource(DesR.dimen.padding_l),
                    top = dimensionResource(TvR.dimen.tv_overscan_vertical),
                    end = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                ).then(if (toBottomEdge) Modifier else Modifier.padding(bottom = dimensionResource(TvR.dimen.tv_overscan_vertical))),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
    ) {
        CompositionLocalProvider(LocalTvSideSheetToBottomEdge provides toBottomEdge) { content() }
    }
}

/** The sheet's heading: what its rows act on. */
@Composable
fun TvSideSheetTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(bottom = dimensionResource(DesR.dimen.padding_xs)),
    )
}

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
 * reached, and fills with it when focused, as a destructive button does. [trailing] ends the row, such as a switch, and is told
 * whether the row is focused so it can match the fill. [role] is what a screen reader calls the row: `Role.Switch` for a row
 * ending in a [TvSwitch], whose on or off state the row then announces with it. A [selected] row carries a tick, for a sheet that is a choice. [enabled] false dims a control that cannot
 * be used yet and keeps it out of the focus order; a fact the sheet shows is a [TvSideSheetReadout]. [initiallyFocused] draws the focused state, for a frame.
 *
 * The [label] holds to one line with an ellipsis, as every TV row's does. [supportingText] is a second line under it, held to one line with an ellipsis: what the row does. [leading] replaces [icon] with a visual of the caller's, such
 * as a poster thumbnail for a sheet about one title; size it with `tv_side_sheet_thumbnail_width` and `_height`.
 * An [icon] sits on its tinted plate, in the error colour for a [destructive] row and dimmed for a disabled one.
 * [reserveSupportingLine] keeps that line's height when [supportingText] is null, so a row with nothing to say there
 * stays as tall as neighbours that have something, such as a count. The label then centres in that height, level with the
 * icon plate, instead of sitting at the top of it.
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
    trailing: (@Composable (isFocused: Boolean) -> Unit)? = null,
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val resting =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
            destructive -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        }
    val focusedDestructive = focused && destructive
    val content = if (focusedDestructive) MaterialTheme.colorScheme.onError else tvFocusContentColor(isFocused = focused, resting = resting)
    val supporting =
        if (focusedDestructive) {
            MaterialTheme.colorScheme.onError
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
                    fillColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                ).then(clickable)
                .then(if (selected) Modifier.semantics { this.selected = true } else Modifier),
        icon = icon,
        iconTint =
            when {
                !enabled -> resting
                destructive -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.primary
            },
        focusedIconTint = if (destructive) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary,
        isFocused = focused,
        selected = selected,
        supportingText = supportingText,
        leading = leading,
        reserveSupportingLine = reserveSupportingLine,
        trailing = trailing?.let { slot -> { slot(focused) } },
    )
}

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
        iconTint = MaterialTheme.colorScheme.primary,
        isFocused = false,
        selected = false,
        supportingText = supportingText,
        leading = leading,
        reserveSupportingLine = reserveSupportingLine,
    )
}

/** The row both [TvSideSheetRow] and [TvSideSheetReadout] draw: what leads, the label and its second line, and the tick. */
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
    focusedIconTint: Color = MaterialTheme.colorScheme.onPrimary,
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
        if (supportingText == null && reserveSupportingLine) {
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
            }
        }
        trailing?.invoke()
        if (selected) TvSelectedTick(tint = content)
    }
}

/**
 * A sheet's second step, for an action that deletes or blocks: what is about to happen, then the confirm and
 * Cancel rows. Focus lands on Cancel, so a stray OK does nothing. [cancelInitiallyFocused] draws that, for a frame.
 * The confirm row rests in the error colour; pass `destructive = false` for a step that asks first but destroys nothing.
 *
 * On a [TvSideSheetStack] page there is no [entryFocus]: pass null, give Cancel `nav.row("cancel")` as [cancelModifier] and
 * call `nav.arriveAt("cancel")`, and the stack lands focus there.
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
) {
    TvSideSheetTitle(title)
    TvSideSheetBody(message)
    TvSideSheetRows {
        TvSideSheetRow(label = confirmLabel, onClick = onConfirm, destructive = destructive)
        TvSideSheetRow(
            label = stringResource(DesR.string.action_cancel),
            onClick = onCancel,
            initiallyFocused = cancelInitiallyFocused,
            modifier = (if (entryFocus != null) Modifier.focusRequester(entryFocus) else Modifier).then(cancelModifier),
        )
    }
    if (entryFocus != null) TvSideSheetStepFocus(entryFocus)
}
