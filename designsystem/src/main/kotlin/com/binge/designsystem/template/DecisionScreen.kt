package com.binge.designsystem.template

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
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
import com.binge.designsystem.uppercaseLocalised

/** Landscape's two panes: the heading and answers against the points card. */
private const val LANDSCAPE_HEADER_WEIGHT = 2f
private const val LANDSCAPE_CONTENT_WEIGHT = 3f

/**
 * A whole screen that asks one question before the app goes on, such as whether to share usage data: a
 * [hero] plate, the [copy], the [points] the choice rests on, and the two answers.
 *
 * Decline is a quiet text button above accept, so neither is a trap. In portrait the hero, copy and points
 * scroll together and the answers stay pinned below. A space wider than tall drops the hero, puts the copy and
 * answers on the start side, and gives the points their own scroll on the end side. The space is the one the screen
 * is given, so a pane of a wide window that is taller than wide stays in the portrait layout.
 *
 * It has no top bar, deliberately: a decision is one question that must be answered, so there is no title to
 * collapse and no Back that skips it. The page clears the system bars and the keyboard itself. A decision inside a
 * flow takes the flow's bar instead, as [StepFlowScreen] draws it.
 *
 * [noteContent] replaces `copy.note` in both layouts, as [DecisionHeading] says.
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
    noteContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    // The space it is given, not the window: in a pane of a wide window it is often taller than wide.
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // A keyboard shrinks the space a host pads for it; the layout must not flip while it shows.
        val imeVisible = keyboardUp()
        val memory = remember { SplitMemory() }
        val wide = heldWhileIme(maxWidth > maxHeight, imeVisible, memory.last)
        if (!imeVisible || memory.last == null) memory.last = wide
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            if (wide) {
                DecisionLandscape(copy, points, accept, decline, noteContent)
            } else {
                DecisionPortrait(copy, points, accept, decline, hero, heroBadge, noteContent)
            }
        }
    }
}

/**
 * The decision's content and nothing around it: the optional [hero] plate, the [copy] and the [points] card, stacked.
 * It has no background, insets, scroll or pinned answers, so it can be a page inside a step flow that supplies its
 * own chrome and footer. [DecisionScreen] lays out the same pieces around it. [noteContent] replaces `copy.note`, as
 * [DecisionHeading] says.
 */
@Composable
fun DecisionBody(
    copy: DecisionCopy,
    points: List<DecisionPoint>,
    modifier: Modifier = Modifier,
    hero: ImageVector? = null,
    heroBadge: ImageVector? = null,
    noteContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        hero?.let {
            DecisionHero(
                icon = it,
                badge = heroBadge,
                modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_s), bottom = dimensionResource(R.dimen.padding_l)),
            )
        }
        DecisionHeading(copy, noteContent = noteContent)
        DecisionPointsCard(points, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_l)))
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
    noteContent: (@Composable ColumnScope.() -> Unit)?,
) {
    Column(
        modifier =
            Modifier
                .centredReadingColumn(dimensionResource(R.dimen.decision_content_max_width))
                .padding(horizontal = dimensionResource(R.dimen.padding_l)),
    ) {
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            DecisionBody(copy = copy, points = points, hero = hero, heroBadge = heroBadge, noteContent = noteContent)
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
    noteContent: (@Composable ColumnScope.() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
    ) {
        Column(modifier = Modifier.weight(LANDSCAPE_HEADER_WEIGHT).fillMaxHeight()) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                DecisionHeading(copy, modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_l)), noteContent = noteContent)
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

/**
 * A decision's kicker, title, subtitle and note, as [DecisionScreen] and [DecisionBody] head theirs. Public for a
 * decision that is one step of a [StepFlowScreen]: it goes in the flow's heading slot, [DecisionHero] in its aside and
 * [com.binge.designsystem.component.DecisionPointsCard] in its content, so a split step puts the hero and heading on
 * the start side and the points on the end (#638).
 *
 * [noteContent] replaces `copy.note`, for a note of more than one paragraph or more than one colour (#673). It sits
 * where the note does, and its paragraphs are spaced as the heading's lines are. A bare `Text` in it reads as the note
 * would: the slot provides the note's style and colour, so only a paragraph that differs names its own.
 */
@Composable
fun DecisionHeading(
    copy: DecisionCopy,
    modifier: Modifier = Modifier,
    noteContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
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
        if (noteContent != null) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                Column(
                    modifier = Modifier.padding(top = dimensionResource(R.dimen.padding_sm)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm)),
                    content = noteContent,
                )
            }
        } else {
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
            modifier = Modifier.fillMaxWidth().then(decline.modifier),
        )
        BingeFilledButton(
            label = accept.label,
            onClick = accept.onClick,
            leadingIcon = accept.leadingIcon,
            modifier = Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.padding_xs)).then(accept.modifier),
        )
    }
}
