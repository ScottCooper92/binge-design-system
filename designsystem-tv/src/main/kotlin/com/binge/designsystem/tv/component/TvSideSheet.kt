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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.tv.material3.Icon
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
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val SCRIM_ALPHA = 0.6f

/**
 * An end-edge, focus-trapped sheet over a scrim: the ten-foot counterpart of a phone's bottom sheet, for a
 * row's actions or a choice. Back, and the key pointing away from the panel, both call [onDismissRequest].
 *
 * [content] gets the entry requester. Each step it shows pins the requester to its first row and pulls it
 * there with [TvSideSheetStepFocus]. The caller owns the visibility flag, and returns focus to what opened
 * the sheet, with `rememberTvOverlayCloser`.
 */
@Composable
fun TvSideSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.(entryFocus: FocusRequester) -> Unit,
) {
    val entryFocus = remember { FocusRequester() }
    val dismissKey = tvStartDirectionKey()
    BackHandler(onBack = onDismissRequest)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = SCRIM_ALPHA))
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown && event.key == dismissKey) {
                        onDismissRequest()
                        true
                    } else {
                        false
                    }
                },
    ) {
        TvSideSheetPanel(modifier = Modifier.align(Alignment.CenterEnd)) { content(entryFocus) }
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
 * placed inside brings its own request: [TvSideSheetConfirm] calls [TvSideSheetStepFocus], so it pulls focus to
 * Cancel wherever it composes.
 */
@Composable
fun TvSideSheetPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
            modifier
                .fillMaxHeight()
                .width(dimensionResource(TvR.dimen.tv_side_sheet_width))
                .background(MaterialTheme.colorScheme.surface)
                .tvExitFocusGroup()
                .padding(dimensionResource(DesR.dimen.padding_l)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s)),
        content = content,
    )
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
 * reached. A [selected] row carries a tick, for a sheet that is a choice. [enabled] false renders a read-out
 * that stays out of the focus order. [initiallyFocused] draws the focused state, for a frame.
 *
 * [supportingText] is a second line under the label. [leading] replaces [icon] with a visual of the caller's, such
 * as a poster thumbnail for a sheet about one title; size it with `tv_side_sheet_thumbnail_width` and `_height`.
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
) {
    var focused by remember { mutableStateOf(initiallyFocused) }
    val resting =
        when {
            !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
            destructive -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
        }
    val content = tvFocusContentColor(isFocused = focused, resting = resting)
    val supporting =
        tvFocusContentColor(
            isFocused = focused,
            resting = if (enabled && !destructive) MaterialTheme.colorScheme.onSurfaceVariant else resting,
        )
    val clickable =
        if (enabled) {
            Modifier.tvClickable(role = Role.Button, onFocusChanged = { focused = it }, onClick = onClick)
        } else {
            Modifier
        }
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(BingeShapes.TvListItem)
                .tvFocusFill(isFocused = focused, shape = BingeShapes.TvListItem)
                .then(clickable)
                .then(if (selected) Modifier.semantics { this.selected = true } else Modifier)
                .padding(
                    horizontal = dimensionResource(DesR.dimen.padding_l),
                    vertical = dimensionResource(DesR.dimen.padding_m),
                ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            leading != null -> leading()
            icon != null ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = content,
                    modifier = Modifier.size(dimensionResource(TvR.dimen.tv_button_icon)),
                )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = content)
            supportingText?.let { Text(text = it, style = MaterialTheme.typography.bodyMedium, color = supporting) }
        }
        if (selected) TvSelectedTick(tint = content)
    }
}

/**
 * A sheet's second step, for an action that deletes or blocks: what is about to happen, then the confirm and
 * Cancel rows. Focus lands on Cancel, so a stray OK does nothing. [cancelInitiallyFocused] draws that, for a frame.
 * The confirm row rests in the error colour; pass `destructive = false` for a step that asks first but destroys nothing.
 */
@Composable
fun ColumnScope.TvSideSheetConfirm(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    entryFocus: FocusRequester,
    cancelInitiallyFocused: Boolean = false,
    destructive: Boolean = true,
) {
    TvSideSheetTitle(title)
    TvSideSheetBody(message)
    TvSideSheetRow(label = confirmLabel, onClick = onConfirm, destructive = destructive)
    TvSideSheetRow(
        label = stringResource(DesR.string.action_cancel),
        onClick = onCancel,
        initiallyFocused = cancelInitiallyFocused,
        modifier = Modifier.focusRequester(entryFocus),
    )
    TvSideSheetStepFocus(entryFocus)
}
