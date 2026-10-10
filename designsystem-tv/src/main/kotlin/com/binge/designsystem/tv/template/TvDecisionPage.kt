package com.binge.designsystem.tv.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * The phone's decision screen on a television, on [TvTwoPanePage]: the [copy] beside a read-out card of
 * the [points], and the two answers pinned under it. Nothing but the answers takes focus.
 *
 * Accept is the primary and takes the page's entry focus. Decline sits beside it, one press away. [hosting]
 * says whether the page claims that focus on arrival (see [TvPageHosting]). [acceptInitiallyFocused] draws
 * accept focused, for a frame.
 */
@Composable
fun TvDecisionPage(
    copy: DecisionCopy,
    points: List<DecisionPoint>,
    acceptLabel: String,
    declineLabel: String,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = currentTvPageHosting(),
    acceptInitiallyFocused: Boolean = false,
) {
    val accept = remember { FocusRequester() }
    TvTwoPanePage(
        modifier = modifier,
        style = TvTwoPaneStyle.Board,
        hosting = hosting,
        entry = accept,
        divider = false,
        pinnedAction = {
            TvButton(
                label = acceptLabel,
                onClick = onAccept,
                style = TvButtonStyle.Primary,
                initiallyFocused = acceptInitiallyFocused,
                modifier = Modifier.focusRequester(accept),
            )
            TvButton(label = declineLabel, onClick = onDecline)
        },
        copy = { DecisionCopyColumn(copy) },
        action = { DecisionPointsCard(points) },
    )
}

@Composable
private fun DecisionCopyColumn(copy: DecisionCopy) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_decision_copy_gap))) {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_decision_heading_gap))) {
            copy.kicker?.let {
                Text(text = it.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }
            Text(text = copy.title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(text = copy.subtitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        copy.note?.let {
            Text(text = it, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DecisionPointsCard(points: List<DecisionPoint>) {
    Column(modifier = Modifier.fillMaxWidth().clip(BingeShapes.ListCard).background(MaterialTheme.colorScheme.surfaceVariant)) {
        points.forEachIndexed { index, point ->
            DecisionPointRow(point)
            if (index < points.lastIndex) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(DesR.dimen.hairline_thickness))
                            .background(MaterialTheme.colorScheme.borderVariant),
                )
            }
        }
    }
}

@Composable
private fun DecisionPointRow(point: DecisionPoint) {
    Row(
        modifier =
            Modifier.fillMaxWidth().padding(
                horizontal = dimensionResource(TvR.dimen.tv_decision_point_padding_h),
                vertical = dimensionResource(TvR.dimen.tv_decision_point_padding_v),
            ),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_decision_point_gap)),
    ) {
        Box(
            modifier =
                Modifier
                    .size(dimensionResource(TvR.dimen.tv_decision_point_badge))
                    .clip(BingeShapes.Large)
                    .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = point.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(TvR.dimen.tv_decision_point_icon)),
            )
        }
        Column {
            Text(text = point.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = point.detail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimensionResource(TvR.dimen.tv_decision_point_text_gap)),
            )
        }
    }
}
