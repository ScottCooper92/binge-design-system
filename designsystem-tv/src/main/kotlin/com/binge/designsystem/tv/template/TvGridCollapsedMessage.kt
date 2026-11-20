package com.binge.designsystem.tv.template

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvCompactButton
import com.binge.designsystem.tv.component.TvErrorPlate
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.tvFocusIndicator
import com.binge.designsystem.tv.theme.TvButtonStyle

/** The emblem for an empty grid whose caller has none of its own, so the plate never reads as a lone grey line. */
private val DEFAULT_EMPTY_ICON = Icons.Outlined.Inbox

/**
 * What a grid shows in place of its cells when the load failed or came back empty: one message plate with a standard
 * button, and focus moved to it as the state arrives. A failure carries Try again; an empty result carries
 * [emptyAction] if there is one, and otherwise the plate itself takes focus so the remote has somewhere to be.
 */
@Composable
internal fun BoxScope.TvGridCollapsedMessage(
    results: TvGridResults<*>,
    onRetry: () -> Unit,
    emptyMessage: String,
    emptyIcon: ImageVector?,
    emptyAction: TvGridAction?,
    focus: FocusRequester,
) {
    TvOverlayArrivalFocusEffect(focus)
    when (results) {
        is TvGridResults.Error -> TvErrorPlate(
            kind = results.kind,
            onRetry = onRetry,
            title = results.title,
            message = results.message,
            alignment = Alignment.Center,
            retryFocusRequester = focus,
        )
        TvGridResults.Empty ->
            if (emptyAction != null) {
                TvMessagePlate(body = emptyMessage, icon = emptyIcon ?: DEFAULT_EMPTY_ICON, alignment = Alignment.Center, announce = true) {
                    TvCompactButton(
                        label = emptyAction.label,
                        onClick = emptyAction.onClick,
                        style = TvButtonStyle.Primary,
                        modifier = Modifier.focusRequester(focus),
                    )
                }
            } else {
                FocusableMessagePlate(body = emptyMessage, icon = emptyIcon ?: DEFAULT_EMPTY_ICON, focus = focus)
            }
        TvGridResults.Loading, is TvGridResults.Items -> Unit
    }
}

/**
 * An empty plate with nothing to press: focusable, with the focus ring around the plate itself (sized to its content and
 * centred in the pane, not the pane), and its text announced as the one focused node.
 *
 * @need show an empty message with nothing to press that still takes focus on a TV screen
 * @group tv
 */
@Composable
fun FocusableMessagePlate(
    body: String,
    focus: FocusRequester,
    modifier: Modifier = Modifier,
    icon: ImageVector = DEFAULT_EMPTY_ICON,
) {
    var focused by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TvMessagePlate(
            body = body,
            icon = icon,
            alignment = Alignment.Center,
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .height(IntrinsicSize.Min)
                .focusRequester(focus)
                .onFocusChanged { focused = it.isFocused }
                .focusable()
                .tvFocusIndicator(isFocused = focused, shape = BingeShapes.MediaCard)
                .semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
        )
    }
}
