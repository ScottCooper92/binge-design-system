package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeShapes

/** The [points] a decision rests on, one row each in a card, hairlines between. */
@Composable
fun DecisionPointsCard(points: List<DecisionPoint>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().clip(BingeShapes.ListCard).background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        points.forEachIndexed { index, point ->
            DecisionPointRow(point)
            if (index < points.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun DecisionPointRow(point: DecisionPoint) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
    ) {
        Box(
            modifier =
                Modifier
                    .size(dimensionResource(R.dimen.decision_point_icon_size))
                    .clip(BingeShapes.Large)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = point.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_l)),
            )
        }
        Column {
            Text(text = point.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = point.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_xxs)),
            )
        }
    }
}

/**
 * The decorative plate that heads a decision: [icon] in a hero-shaped plate, with [badge] on its corner. Public so a
 * decision that is one step of a `StepFlowScreen` can put it in the flow's aside, where a split step keeps it beside
 * the heading rather than in the content's scroll (#638).
 */
@Composable
fun DecisionHero(
    icon: ImageVector,
    badge: ImageVector?,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(dimensionResource(R.dimen.decision_hero_size))) {
        Box(
            modifier = Modifier.fillMaxSize().clip(BingeShapes.Hero).background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(dimensionResource(R.dimen.decision_hero_icon_size)),
            )
        }
        badge?.let {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(dimensionResource(R.dimen.decision_hero_badge_size))
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(dimensionResource(R.dimen.decision_hero_badge_icon_size)),
                )
            }
        }
    }
}
