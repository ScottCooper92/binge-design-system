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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.LocalPaneDepth
import com.binge.designsystem.R
import com.binge.designsystem.centredReadingColumn
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.LocalReduceMotion
import com.binge.designsystem.uppercaseLocalised
import kotlin.coroutines.cancellation.CancellationException

/** How long a step takes to slide across: long enough to read as a direction, short enough not to wait on. */
private const val STEP_SLIDE_MILLIS = 280

/**
 * A multi-step flow on a phone, foldable or tablet: the default screen chrome at the top (a transparent bar that
 * scrims in as the step passes under it), the step read-out under the bar, the step below, and its commit in a
 * [footer] pinned under it. For onboarding, setup, and a consent prompt.
 *
 * A tall space stacks the [aside] (an illustration), the [heading] and the [content] in one scroll. A space that is
 * wider than it is tall, or expanded, puts the aside, heading and footer on one side and scrolls the content on the
 * other. The choice reads the space the flow is given, not the window, so a flow in a narrow pane stacks.
 * While the keyboard is showing, or its inset is still animating away, the choice is held, so a host that pads the
 * flow for it does not flip the layout.
 *
 * Back shows from the second step, and BACK, predictive back included, steps back too. A flow opened from inside the
 * app passes [onExit], which gives the first step a Back that leaves, and a [title] for its bar, so the flow has one
 * bar and one Back. With a [title] the bar is the collapsing one; without, it is a single line that holds Back, so
 * the bar does not change kind as Back comes and goes. [loading] replaces the step with a centred indicator. The read-out is hidden for a single-step flow.
 *
 * Each slot is handed the step it draws, and should branch on that rather than on [currentStep]: changing step
 * slides the whole step across, and the outgoing one must keep drawing itself while it leaves. A step whose
 * content scrolls itself, a lazy grid, returns false from [contentScrolls] and gets the remaining height instead.
 * [scrollBehavior] is [BingeScreenScaffold]'s, for a caller that owns the bar's state; an untitled flow's bar is
 * pinned by default. Beside a list pane the first step's Back is dropped, as the scaffold does, but a step back always shows. The same shape as the TV step flow, so a flow ports between form factors by swapping the template.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    title: String? = null,
    onExit: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable ColumnScope.(step: Int) -> Unit,
) {
    val back = if (currentStep > 0) onBack else onExit
    StepBackHandler(back)
    // Back to the previous step is not "pop this screen": the scaffold hides a Back where a list pane beside it already
    // offers one, which is right for the first step's exit and wrong for a step back. From the second step the flow's
    // own Back is kept by presenting the entry as past the first one.
    val paneDepth = if (currentStep > 0) maxOf(LocalPaneDepth.current, 2) else LocalPaneDepth.current
    // An untitled flow has a single-line bar that stays put, so Back and the read-out do not scroll away with the step.
    val ownBehavior = if (title == null) TopAppBarDefaults.pinnedScrollBehavior() else null
    CompositionLocalProvider(LocalPaneDepth provides paneDepth) {
        // The scaffold's bar is the chrome: a collapsing bar when the flow has a title, a single line when it has none, so
        // the bar does not change kind as Back comes and goes. The read-out sits under it as the scaffold's header, and a
        // step scrolls under both. The scaffold does not consume the keyboard, so a step with a text field owns its own
        // imePadding and a footer rides above the keyboard, as before.
        BingeScreenScaffold(
            title = title.orEmpty(),
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
            onBack = back,
            bar = if (title != null) ScreenBar.Collapsing else ScreenBar.Small,
            scrollBehavior = scrollBehavior ?: ownBehavior,
            header = if (stepCount > 1) ({ StepReadOut(stepCount, currentStep) }) else null,
        ) { padding ->
            ScreenBody(padding) { inner ->
                val expandedWidth = dimensionResource(R.dimen.content_inset_expanded_breakpoint)
                // The bottom clears the navigation bar here, and is consumed so a footer's imePadding adds only the excess.
                val bottom = PaddingValues(bottom = inner.calculateBottomPadding())
                BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(bottom).consumeWindowInsets(bottom)) {
                    // A keyboard shrinks the space a host pads for it; the layout must not flip under a focused field.
                    val keyboardUp = keyboardUp()
                    val memory = remember { SplitMemory() }
                    val split = heldWhileIme(splitsStep(maxWidth, maxHeight, expandedWidth), keyboardUp, memory.last)
                    if (!keyboardUp || memory.last == null) memory.last = split
                    if (loading) {
                        Box(Modifier.padding(top = inner.calculateTopPadding())) { LoadingMessageScreen() }
                    } else {
                        StepTransition(currentStep) { step ->
                            val slots = StepSlots(step, heading, aside, footer, content, scrolls = contentScrolls(step))
                            if (split) SplitStep(slots, inner.calculateTopPadding()) else StackedStep(slots, inner.calculateTopPadding())
                        }
                    }
                }
            }
        }
    }
}

/** Whether a step given [width] by [height] splits: when the space is wider than tall, or at least [expandedWidth]. */
internal fun splitsStep(
    width: Dp,
    height: Dp,
    expandedWidth: Dp,
): Boolean = width > height || width >= expandedWidth

/** The last split decision made without the keyboard. Not state: it is read and written within one composition. */
internal class SplitMemory {
    var last: Boolean? = null
}

/**
 * Whether the keyboard is up, for a split held while it is: visible, or its inset still animating away. On hide the
 * keyboard stops reporting itself visible while its inset is still shrinking a host's padded space.
 */
@Composable
internal fun keyboardUp(): Boolean = WindowInsets.isImeVisible || WindowInsets.ime.getBottom(LocalDensity.current) > 0

/** The [measured] decision, or the [held] one while the keyboard is showing and there is one to hold. */
internal fun heldWhileIme(
    measured: Boolean,
    imeVisible: Boolean,
    held: Boolean?,
): Boolean = if (imeVisible && held != null) held else measured

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
            Text(text = it.uppercaseLocalised(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() },
        )
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

/**
 * The step stacked in one column. A scrolling step carries [top], the bar and read-out's height, as a spacer inside
 * its scroll, so it passes under them; a step that scrolls itself has nothing to scroll under, and is padded.
 */
@Composable
private fun StackedStep(slots: StepSlots, top: Dp) {
    val gap = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l))
    Column(
        modifier =
            Modifier
                .centredReadingColumn(dimensionResource(R.dimen.content_max_width))
                .then(if (slots.footer != null) Modifier.imePadding() else Modifier)
                .padding(horizontal = dimensionResource(R.dimen.padding_l)),
    ) {
        if (slots.scrolls) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(top))
                Column(verticalArrangement = gap) {
                    slots.aside?.invoke(this, slots.step)
                    slots.heading?.invoke(slots.step)
                    slots.content(this, slots.step)
                }
            }
        } else {
            Column(modifier = Modifier.weight(1f).padding(top = top), verticalArrangement = gap) {
                slots.aside?.invoke(this, slots.step)
                slots.heading?.invoke(slots.step)
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) { slots.content(this, slots.step) }
            }
        }
        slots.footer?.let { footer -> StepFooter { footer(slots.step) } }
    }
}

@Composable
private fun SplitStep(slots: StepSlots, top: Dp) {
    val gap = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l))
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.padding_l)),
        horizontalArrangement = gap,
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight().then(if (slots.footer != null) Modifier.imePadding() else Modifier)) {
            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(top))
                Column(verticalArrangement = gap) {
                    slots.aside?.invoke(this, slots.step)
                    slots.heading?.invoke(slots.step)
                }
            }
            slots.footer?.let { footer -> StepFooter { footer(slots.step) } }
        }
        if (slots.scrolls) {
            Column(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(top))
                Column(verticalArrangement = gap) { slots.content(this, slots.step) }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(top = top),
                verticalArrangement = gap,
            ) { slots.content(this, slots.step) }
        }
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

/** The step dots, centred under the bar: the scaffold's header, so one scrim spans the bar and the read-out. */
@Composable
private fun StepReadOut(stepCount: Int, currentStep: Int) {
    StepDots(
        stepCount,
        currentStep,
        Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.padding_sm)),
    )
}

@Composable
private fun StepDots(
    stepCount: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    val progress = stringResource(R.string.cd_step_progress, currentStep + 1, stepCount)
    Row(
        // The dots draw no text, so the row names the step for a screen reader in their place.
        modifier = modifier.then(if (stepCount > 1) Modifier.semantics { contentDescription = progress } else Modifier),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.step_dot_spacing), Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (stepCount > 1) repeat(stepCount) { index -> StepDot(reached = index <= currentStep, current = index == currentStep) }
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
