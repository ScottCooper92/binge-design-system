package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TvIllustration
import com.binge.designsystem.tv.component.TvVerticalDivider
import com.binge.designsystem.tv.focus.TvStableFocusScroll
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/** The copy pane's share of a weighted [TvTwoPaneSplit]: the copy and the controls side by side, 1:1. */
private const val BALANCED_COPY_SHARE = 0.5f

/** A narrow copy pane beside a wide one: a list beside what it describes, or a short blurb beside a grid. */
private const val LIST_DETAIL_COPY_SHARE = 0.3f

/** A copy pane a little under half: enough room for a step's explanation beside a grid of choices. */
private const val ACTION_LED_COPY_SHARE = 0.4f

/** How [TvTwoPanePage] divides the width between its copy pane and its action pane. */
@Immutable
sealed interface TvTwoPaneSplit {
    /** Both panes weighted; the copy pane takes [copyShare] of the width left after the gap. */
    data class Weighted(
        val copyShare: Float,
    ) : TvTwoPaneSplit

    /** The copy pane fixed at [width], the action pane taking the rest — a form beside its explanation. */
    data class FixedCopy(
        val width: Dp,
    ) : TvTwoPaneSplit

    /** The action pane fixed at [width], the copy pane taking the rest — a sign-in panel beside its pitch. */
    data class FixedAction(
        val width: Dp,
    ) : TvTwoPaneSplit

    companion object {
        val Balanced: TvTwoPaneSplit = Weighted(BALANCED_COPY_SHARE)
        val ListDetail: TvTwoPaneSplit = Weighted(LIST_DETAIL_COPY_SHARE)
        val ActionLed: TvTwoPaneSplit = Weighted(ACTION_LED_COPY_SHARE)
    }
}

/** How [TvTwoPanePage] places its copy and spaces its panes. */
enum class TvTwoPaneStyle {
    /** Copy centred beside a form or panel, the panes held well apart: sign-in, setup, a step. */
    Form,

    /** Copy top-aligned beside a list or grid, the divider splitting a narrower gap: settings, a picker board. */
    Board,
}

/**
 * A TV page in two panes: [copy] says what this page is and what to do, [action] holds what the remote walks
 * through. The template for sign-in, setup, settings and onboarding steps.
 *
 * The action pane scrolls when [actionScrolls] (a column of fields), or hands its whole height to a lazy
 * child that scrolls itself when not. [pinnedAction] sits under the action pane as its unweighted child, so a
 * commit stays reachable however long the pane gets. [actionFirst] mirrors the panes for a page whose
 * choices lead (a grid of services, then why). [style] picks the form or the board shape. Focus entry lands
 * on [entry]; see [TvBoard] for [hosting].
 */
@Composable
fun TvTwoPanePage(
    modifier: Modifier = Modifier,
    title: String? = null,
    split: TvTwoPaneSplit = TvTwoPaneSplit.Balanced,
    style: TvTwoPaneStyle = TvTwoPaneStyle.Form,
    hosting: TvPageHosting = currentTvPageHosting(),
    entry: FocusRequester? = null,
    arrivalEnabled: Boolean = true,
    divider: Boolean = true,
    actionFirst: Boolean = false,
    actionScrolls: Boolean = true,
    copyAlignment: Alignment.Horizontal = Alignment.Start,
    pinnedAction: (@Composable RowScope.() -> Unit)? = null,
    copy: @Composable ColumnScope.() -> Unit,
    action: @Composable ColumnScope.() -> Unit,
) {
    TvBoard(
        title = title,
        modifier = modifier,
        hosting = hosting,
        entry = entry,
        arrivalEnabled = arrivalEnabled,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(style.paneGap)),
        ) {
            val copyPane: @Composable RowScope.() -> Unit = {
                CopyPane(
                    modifier = paneModifier(split, isCopy = true),
                    style = style,
                    alignment = copyAlignment,
                    content = copy,
                )
            }
            val actionPane: @Composable RowScope.() -> Unit = {
                ActionPane(
                    modifier = paneModifier(split, isCopy = false),
                    scrolls = actionScrolls,
                    pinnedAction = pinnedAction,
                    content = action,
                )
            }
            if (actionFirst) actionPane() else copyPane()
            if (divider) TvVerticalDivider()
            if (actionFirst) copyPane() else actionPane()
        }
    }
}

/**
 * The copy a [TvTwoPanePage] usually opens with: an optional illustration, a headline, a body and a note,
 * aligned with the pane. A page whose copy is anything else (a QR panel's pitch with a button in it) builds
 * its own column in the slot instead.
 */
@Composable
fun TvTwoPaneCopy(
    headline: String,
    body: String,
    modifier: Modifier = Modifier,
    illustration: ImageVector? = null,
    note: String? = null,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    val textAlign = if (alignment == Alignment.CenterHorizontally) TextAlign.Center else TextAlign.Start
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
        horizontalAlignment = alignment,
    ) {
        illustration?.let { TvIllustration(icon = it) }
        Text(
            text = headline,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = textAlign,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign,
        )
        note?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
            )
        }
    }
}

/** The gap between neighbouring panes, so with a divider the gap on each side of the rule. */
private val TvTwoPaneStyle.paneGap: Int
    get() = when (this) {
        TvTwoPaneStyle.Form -> TvR.dimen.tv_two_pane_gap
        TvTwoPaneStyle.Board -> TvR.dimen.tv_two_pane_board_gap
    }

@Composable
private fun CopyPane(
    style: TvTwoPaneStyle,
    alignment: Alignment.Horizontal,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val arrangement = when (style) {
        TvTwoPaneStyle.Form -> Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l), Alignment.CenterVertically)
        TvTwoPaneStyle.Board -> Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s), Alignment.Top)
    }
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = arrangement,
        horizontalAlignment = alignment,
        content = content,
    )
}

/**
 * The column scrolls rather than fits: a column that cannot scroll starves its children, and a starved field
 * is still focusable, so ↓ lands on a sliver the user cannot read.
 */
@Composable
private fun ActionPane(
    scrolls: Boolean,
    pinnedAction: (@Composable RowScope.() -> Unit)?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m)),
    ) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (scrolls) {
                ScrollingActionColumn(content = content)
            } else {
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(), content = content)
            }
        }
        pinnedAction?.let {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_sm), Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = it,
            )
        }
    }
}

/**
 * A short form centres beside its copy; a long one scrolls from the top. The inner column's minimum height is
 * the viewport's, which is what lets the arrangement centre inside a scroll that measures unbounded.
 */
@Composable
private fun ScrollingActionColumn(content: @Composable ColumnScope.() -> Unit) {
    val bleed = dimensionResource(TvR.dimen.tv_focus_ring_bleed)
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val viewport = maxHeight
        TvStableFocusScroll {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(min = viewport).padding(vertical = bleed),
                    verticalArrangement =
                        Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_two_pane_action_gap), Alignment.CenterVertically),
                    content = content,
                )
            }
        }
    }
}

private fun RowScope.paneModifier(split: TvTwoPaneSplit, isCopy: Boolean): Modifier =
    when (split) {
        is TvTwoPaneSplit.Weighted -> Modifier.weight(if (isCopy) split.copyShare else 1f - split.copyShare)
        is TvTwoPaneSplit.FixedCopy -> if (isCopy) Modifier.width(split.width) else Modifier.weight(1f)
        is TvTwoPaneSplit.FixedAction -> if (isCopy) Modifier.weight(1f) else Modifier.width(split.width)
    }
