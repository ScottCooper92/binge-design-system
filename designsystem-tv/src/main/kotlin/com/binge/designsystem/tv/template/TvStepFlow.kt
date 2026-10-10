package com.binge.designsystem.tv.template

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.tv.component.TvMessagePlate
import com.binge.designsystem.uppercaseLocalised
import com.binge.designsystem.tv.R as TvR

/**
 * A multi-step flow on TV: a step read-out at the top, the step's [content], and a footer band of
 * [footerActions] with an optional [footerHint]. For onboarding before the shell and for setup or sign-in.
 *
 * [currentStep] is zero-based; [progressLabel] is the consumer's own "Step 2 of 3". BACK steps back through
 * [onBack] when it is non-null and falls through when it is null, and there is no on-screen Back: the remote's
 * key is the one every TV user knows. Focus lands on [entry] each time the step changes. [loading] replaces
 * the content with a plate carrying that label, so a step never shows an empty frame while it fills.
 * [chromeOverContent] lets a full-bleed step draw under the read-out and supply its own footer.
 */
@Composable
fun TvStepFlow(
    stepCount: Int,
    currentStep: Int,
    progressLabel: String,
    modifier: Modifier = Modifier,
    hosting: TvPageHosting = TvPageHosting.PreShell,
    entry: FocusRequester? = null,
    arrivalEnabled: Boolean = true,
    onBack: (() -> Unit)? = null,
    loading: String? = null,
    footerHint: String? = null,
    footerActions: (@Composable RowScope.() -> Unit)? = null,
    chromeOverContent: Boolean = false,
    content: @Composable () -> Unit,
) {
    BackHandler(enabled = onBack != null) { onBack?.invoke() }
    val padding = tvPagePadding(hosting)
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .then(tvPageArrival(hosting, entry, arrivalEnabled && loading == null, key = currentStep)),
    ) {
        Column(modifier = Modifier.fillMaxSize().then(if (chromeOverContent) Modifier else Modifier.padding(padding))) {
            if (!chromeOverContent) TvStepProgress(stepCount, currentStep, progressLabel)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .then(
                            if (chromeOverContent) {
                                Modifier
                            } else {
                                Modifier.padding(vertical = dimensionResource(TvR.dimen.tv_step_band_spacing))
                            },
                        ),
            ) {
                if (loading != null) {
                    TvMessagePlate(body = loading, alignment = Alignment.Center)
                } else {
                    content()
                }
            }
            if (!chromeOverContent && footerActions != null) {
                TvStepFooter(hint = footerHint, actions = footerActions)
            }
        }
        if (chromeOverContent) {
            Box(modifier = Modifier.padding(padding)) {
                TvStepProgress(stepCount, currentStep, progressLabel)
            }
        }
    }
}

/**
 * The kicker, title and subtitle a step opens with. None of it is focusable: a heading holds nothing to press,
 * so ↑ from the content below is absorbed rather than parking the ring on inert text.
 */
@Composable
fun TvStepHeading(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_step_heading_gap)),
    ) {
        kicker?.let {
            Text(
                text = it.uppercaseLocalised(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * "Step 2 of 3" beside a dot row. At ten feet three small dots are a smudge rather than a position, so the
 * words carry it. Never focusable: a read-out that does nothing when pressed is worse than no target.
 */
@Composable
private fun TvStepProgress(
    stepCount: Int,
    currentStep: Int,
    label: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_step_progress_gap)),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_step_dot_gap)),
        ) {
            repeat(stepCount) { index -> TvStepDot(isCurrent = index == currentStep) }
        }
    }
}

@Composable
private fun TvStepDot(isCurrent: Boolean) {
    val size = dimensionResource(TvR.dimen.tv_step_dot_size)
    Box(
        modifier =
            Modifier
                .height(size)
                .then(if (isCurrent) Modifier.width(dimensionResource(TvR.dimen.tv_step_dot_active_width)) else Modifier.size(size))
                .clip(BingeShapes.Pill)
                .background(if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
    )
}

/** The footer band: the hint at the start, the step's commit at the end. */
@Composable
private fun TvStepFooter(hint: String?, actions: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_step_footer_gap)),
    ) {
        hint?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(modifier = Modifier.weight(1f))
        actions()
    }
}
