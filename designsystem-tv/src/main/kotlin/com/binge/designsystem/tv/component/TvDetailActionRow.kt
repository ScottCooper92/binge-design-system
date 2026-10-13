package com.binge.designsystem.tv.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.component.WrappingRow
import com.binge.designsystem.tv.focus.tvFocusGroup
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The detail action row, at the foot of the hero band and reused by any TV detail page. Each page supplies its
 * own [actions]; at most one is [TvDetailAction.isPrimary], and that one renders as the row's single labelled
 * [TvButton] with the wider min width — everything else is a circular [TvIconButton], unless it asks for a
 * label with [TvDetailAction.showLabel]. A row too long for the width it is given wraps onto a further line. Empty
 * [actions] renders nothing. [entryFocus] is where page entry lands.
 */
@Composable
fun TvDetailActionRow(
    actions: List<TvDetailAction>,
    entryFocus: FocusRequester,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return
    // Centred as one block; a row too long for the width wraps to a second line rather than leaving its last action no room.
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .focusRequester(entryFocus)
                .tvFocusGroup(),
        contentAlignment = Alignment.Center,
    ) {
        WrappingRow(
            horizontalSpacing = dimensionResource(DesR.dimen.padding_m),
            verticalSpacing = dimensionResource(DesR.dimen.padding_m),
        ) {
            actions.forEach { action ->
                val target = action.focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier
                if (action.isPrimary) {
                    TvButton(
                        label = action.label,
                        onClick = action.onClick,
                        style = TvButtonStyle.Primary,
                        leadingIcon = action.icon,
                        initiallyFocused = action.initiallyFocused,
                        modifier = target.widthIn(min = dimensionResource(TvR.dimen.tv_detail_page_action_primary_min_width)),
                    )
                } else if (action.showLabel) {
                    TvButton(
                        label = action.label,
                        onClick = action.onClick,
                        style = action.style,
                        leadingIcon = action.icon,
                        initiallyFocused = action.initiallyFocused,
                        modifier = target,
                    )
                } else {
                    TvIconButton(
                        icon = action.icon,
                        label = action.label,
                        onClick = action.onClick,
                        style = action.style,
                        initiallyFocused = action.initiallyFocused,
                        modifier = target,
                    )
                }
            }
        }
    }
}

/**
 * One action in a [TvDetailActionRow]. [isPrimary] marks the row's one emphasised lead action — a filled, labelled
 * pill; every other action is an icon button with [label] as its accessible name, or a labelled button when
 * [showLabel] is set. [focusRequester] lets a caller hand
 * focus back to this action after a sheet it opened closes.
 */
data class TvDetailAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isPrimary: Boolean = false,
    val initiallyFocused: Boolean = false,
    val focusRequester: FocusRequester? = null,
    /** A secondary action as a labelled button rather than an icon: for an action a bare glyph would not name clearly enough. */
    val showLabel: Boolean = false,
    /**
     * The role of a secondary action; the primary is always [TvButtonStyle.Primary]. Destructive actions go last in
     * the row: a reversible one (block) takes [TvButtonStyle.DestructiveOutline], an irreversible one (remove,
     * delete) takes [TvButtonStyle.Destructive].
     */
    val style: TvButtonStyle = TvButtonStyle.Secondary,
)
