package com.binge.designsystem.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.R
import com.binge.designsystem.centredReadingColumn
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.DecisionHero
import com.binge.designsystem.component.DecisionPointsCard
import com.binge.designsystem.isLandscape
import com.binge.designsystem.uppercaseLocalised

/** Landscape's two panes: the heading and answers against the points card. */
private const val LANDSCAPE_HEADER_WEIGHT = 2f
private const val LANDSCAPE_CONTENT_WEIGHT = 3f

/**
 * A whole screen that asks one question before the app goes on, such as whether to share usage data: a
 * [hero] plate, the [copy], the [points] the choice rests on, and the two answers.
 *
 * Decline is a quiet text button above accept, so neither is a trap. In portrait the hero, copy and points
 * scroll together and the answers stay pinned below. A wide, short window drops the hero, puts the copy and
 * answers on the start side, and gives the points their own scroll on the end side.
 */
@Composable
fun DecisionScreen(
    copy: DecisionCopy,
    points: List<DecisionPoint>,
    accept: ScreenAction,
    decline: ScreenAction,
    modifier: Modifier = Modifier,
    hero: ImageVector? = null,
    heroBadge: ImageVector? = null,
) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        if (isLandscape()) {
            DecisionLandscape(copy, points, accept, decline)
        } else {
            DecisionPortrait(copy, points, accept, decline, hero, heroBadge)
        }
    }
}

@Composable
private fun DecisionPortrait(
    copy: DecisionCopy,
    points: List<DecisionPoint>,
    accept: ScreenAction,
    decline: ScreenAction,
    hero: ImageVector?,
    heroBadge: ImageVector?,
) {
    Column(
        modifier =
            Modifier
                .centredReadingColumn(dimensionResource(R.dimen.decision_content_max_width))
                .padding(horizontal = dimensionResource(R.dimen.padding_l)),
    ) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            hero?.let {
                DecisionHero(
                    icon = it,
                    badge = heroBadge,
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_s), bottom = dimensionResource(R.dimen.padding_l)),
                )
            }
            DecisionHeading(copy)
            DecisionPointsCard(points, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_l)))
        }
        DecisionFooter(accept, decline)
    }
}

@Composable
private fun DecisionLandscape(
    copy: DecisionCopy,
    points: List<DecisionPoint>,
    accept: ScreenAction,
    decline: ScreenAction,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
    ) {
        Column(modifier = Modifier.weight(LANDSCAPE_HEADER_WEIGHT).fillMaxHeight()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                DecisionHeading(copy, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_l)))
            }
            DecisionFooter(accept, decline)
        }
        Column(
            modifier =
                Modifier
                    .weight(LANDSCAPE_CONTENT_WEIGHT)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(top = dimensionResource(R.dimen.decision_landscape_points_top)),
        ) {
            DecisionPointsCard(points)
        }
    }
}

@Composable
private fun DecisionHeading(copy: DecisionCopy, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_m))) {
        copy.kicker?.let {
            Text(
                text = it.uppercaseLocalised(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_sm)),
            )
        }
        Text(
            text = copy.title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = dimensionResource(R.dimen.padding_sm)).semantics { heading() },
        )
        Text(text = copy.subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        copy.note?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_sm)),
            )
        }
    }
}

@Composable
private fun DecisionFooter(accept: ScreenAction, decline: ScreenAction) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.padding_m), bottom = dimensionResource(R.dimen.padding_l)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BingeTextButton(
            label = decline.label,
            onClick = decline.onClick,
            leadingIcon = decline.leadingIcon,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        )
        BingeFilledButton(
            label = accept.label,
            onClick = accept.onClick,
            leadingIcon = accept.leadingIcon,
            modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_xs)),
        )
    }
}
