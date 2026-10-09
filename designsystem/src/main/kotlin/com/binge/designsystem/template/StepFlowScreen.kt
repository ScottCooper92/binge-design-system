package com.binge.designsystem.template

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.centredReadingColumn
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.isLandscape
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion
import kotlin.coroutines.cancellation.CancellationException

/** How long a step takes to slide across: long enough to read as a direction, short enough not to wait on. */
private const val STEP_SLIDE_MILLIS = 280

/**
 * A multi-step flow on a phone, foldable or tablet: back and a step read-out at the top, the step below, and
 * its commit in a [footer] pinned under it. For onboarding, setup, and a consent prompt.
 *
 * Portrait stacks the [aside] (an illustration), the [heading] and the [content] in one scroll. A landscape or
 * expanded window has the width and not the height, so the aside, heading and footer go on one side and the
 * content scrolls on the other. Back shows from the second step, and BACK, predictive back included, steps
 * back too. [loading] replaces the step with a centred indicator. The read-out is hidden for a single-step flow.
 *
 * Each slot is handed the step it draws, and should branch on that rather than on [currentStep]: changing step
 * slides the whole step across, and the outgoing one must keep drawing itself while it leaves. A step whose
 * content scrolls itself, a lazy grid, returns false from [contentScrolls] and gets the remaining height instead.
 * The same shape as the TV step flow, so a flow ports between form factors by swapping the template.
 */
@Composable
fun StepFlowScreen(
    stepCount: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    loading: Boolean = false,
    contentScrolls: (step: Int) -> Boolean = { true },
    heading: (@Composable (step: Int) -> Unit)? = null,
    aside: (@Composable ColumnScope.(step: Int) -> Unit)? = null,
    footer: (@Composable ColumnScope.(step: Int) -> Unit)? = null,
    content: @Composable ColumnScope.(step: Int) -> Unit,
) {
    val back = onBack.takeIf { currentStep > 0 }
    StepBackHandler(back)
    // systemBars + displayCutout rather than safeDrawing, so a step raising the keyboard does not shift the
    // chrome: a step with a text field owns its own imePadding, and a footer rides above the keyboard.
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
    ) {
        StepChrome(stepCount = stepCount, currentStep = currentStep, onBack = back)
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            if (loading) {
                LoadingMessageScreen()
            } else {
                StepTransition(currentStep) { step ->
                    val slots = StepSlots(step, heading, aside, footer, content, scrolls = contentScrolls(step))
                    if (isLandscape() || isExpandedLayout()) SplitStep(slots) else StackedStep(slots)
                }
            }
        }
    }
}

/** The kicker, title and subtitle a step opens with. */
@Composable
fun StepHeading(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    subtitle: String? = null,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_sm))) {
        kicker?.let {
            Text(text = it.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Text(text = title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onSurface)
        subtitle?.let {
            Text(text = it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** One step's slots, already bound to the step they draw. */
private class StepSlots(
    val step: Int,
    val heading: (@Composable (step: Int) -> Unit)?,
    val aside: (@Composable ColumnScope.(step: Int) -> Unit)?,
    val footer: (@Composable ColumnScope.(step: Int) -> Unit)?,
    val content: @Composable ColumnScope.(step: Int) -> Unit,
    val scrolls: Boolean,
)

/**
 * Forward slides the next step in from the end edge, back from the start; reduced motion swaps it in place.
 * The chrome stays put above it, so the read-out moves without the back button sliding away with the step.
 */
@Composable
private fun StepTransition(currentStep: Int, step: @Composable (Int) -> Unit) {
    val reduceMotion = LocalReduceMotion.current
    AnimatedContent(
        targetState = currentStep,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (reduceMotion) {
                EnterTransition.None togetherWith ExitTransition.None
            } else {
                val direction = if (targetState > initialState) SlideDirection.Start else SlideDirection.End
                slideIntoContainer(direction, tween(STEP_SLIDE_MILLIS)) togetherWith
                    slideOutOfContainer(direction, tween(STEP_SLIDE_MILLIS))
            }
        },
        label = "step-flow",
    ) { target -> step(target) }
}

/** BACK, and the predictive back gesture, steps back once the gesture commits; a cancelled gesture does nothing. */
@Composable
private fun StepBackHandler(onBack: (() -> Unit)?) {
    val currentOnBack by rememberUpdatedState(onBack)
    PredictiveBackHandler(enabled = onBack != null) { progress ->
        try {
            progress.collect { }
            currentOnBack?.invoke()
        } catch (_: CancellationException) {
            // The gesture was abandoned, which is not a step back.
        }
    }
}

@Composable
private fun StackedStep(slots: StepSlots) {
    Column(
        modifier =
            Modifier
                .centredReadingColumn(dimensionResource(R.dimen.content_max_width))
                .then(if (slots.footer != null) Modifier.imePadding() else Modifier)
                .padding(horizontal = dimensionResource(R.dimen.padding_l)),
    ) {
        Column(
            modifier = Modifier.weight(1f).then(if (slots.scrolls) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
        ) {
            slots.aside?.invoke(this, slots.step)
            slots.heading?.invoke(slots.step)
            if (slots.scrolls) {
                slots.content(this, slots.step)
            } else {
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) { slots.content(this, slots.step) }
            }
        }
        slots.footer?.let { footer -> StepFooter { footer(slots.step) } }
    }
}

@Composable
private fun SplitStep(slots: StepSlots) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight().then(if (slots.footer != null) Modifier.imePadding() else Modifier)) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
            ) {
                slots.aside?.invoke(this, slots.step)
                slots.heading?.invoke(slots.step)
            }
            slots.footer?.let { footer -> StepFooter { footer(slots.step) } }
        }
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (slots.scrolls) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
        ) { slots.content(this, slots.step) }
    }
}

@Composable
private fun StepFooter(footer: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_m)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        content = footer,
    )
}

/** Back on the start edge, the dots centred, and an empty box on the end edge so the dots stay centred. */
@Composable
private fun StepChrome(
    stepCount: Int,
    currentStep: Int,
    onBack: (() -> Unit)?,
) {
    val slot = dimensionResource(R.dimen.step_chrome_height)
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = slot)
                .padding(horizontal = dimensionResource(R.dimen.step_chrome_padding_h), vertical = dimensionResource(R.dimen.padding_sm)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(slot)) {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_navigate_back))
                }
            }
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.step_dot_spacing), Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (stepCount > 1) repeat(stepCount) { index -> StepDot(reached = index <= currentStep, current = index == currentStep) }
        }
        Box(Modifier.size(slot))
    }
}

@Composable
private fun StepDot(reached: Boolean, current: Boolean) {
    val size = dimensionResource(R.dimen.step_dot_size)
    Box(
        Modifier
            .height(size)
            .width(if (current) dimensionResource(R.dimen.step_dot_active_width) else size)
            .clip(BingeShapes.Pill)
            .background(if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}
