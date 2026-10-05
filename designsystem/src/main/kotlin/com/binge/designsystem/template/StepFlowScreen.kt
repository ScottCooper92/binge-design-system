package com.binge.designsystem.template

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.R
import com.binge.designsystem.centredReadingColumn
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.isLandscape

/**
 * A multi-step flow on a phone, foldable or tablet: back and a step read-out at the top, the step below, and
 * its commit in a [footer] pinned under it. For onboarding, setup, and a consent prompt.
 *
 * Portrait stacks the [aside] (an illustration), the [heading] and the [content] in one scroll. A landscape or
 * expanded window has the width and not the height, so the aside, heading and footer go on one side and the
 * content scrolls on the other. Back shows from the second step, and BACK steps back too. [loading] replaces
 * the step with a centred indicator. The read-out is hidden for a single-step flow. The same shape as the TV
 * step flow, so a flow ports between form factors by swapping the template.
 */
@Composable
fun StepFlowScreen(
    stepCount: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    loading: Boolean = false,
    heading: (@Composable () -> Unit)? = null,
    aside: (@Composable ColumnScope.() -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val back = onBack.takeIf { currentStep > 0 }
    BackHandler(enabled = back != null) { back?.invoke() }
    // systemBars + displayCutout rather than safeDrawing, so a step raising the keyboard does not shift the
    // whole flow: a step with a text field owns its own imePadding.
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
    ) {
        StepChrome(stepCount = stepCount, currentStep = currentStep, onBack = back)
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            when {
                loading -> LoadingMessageScreen()
                isLandscape() || isExpandedLayout() -> SplitStep(heading, aside, footer, content)
                else -> StackedStep(heading, aside, footer, content)
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

@Composable
private fun StackedStep(
    heading: (@Composable () -> Unit)?,
    aside: (@Composable ColumnScope.() -> Unit)?,
    footer: (@Composable ColumnScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            Modifier
                .centredReadingColumn(dimensionResource(R.dimen.content_max_width))
                .padding(horizontal = dimensionResource(R.dimen.padding_l)),
    ) {
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
        ) {
            aside?.invoke(this)
            heading?.invoke()
            content()
        }
        footer?.let { StepFooter(it) }
    }
}

@Composable
private fun SplitStep(
    heading: (@Composable () -> Unit)?,
    aside: (@Composable ColumnScope.() -> Unit)?,
    footer: (@Composable ColumnScope.() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize().padding(horizontal = dimensionResource(R.dimen.padding_l)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
    ) {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
            ) {
                aside?.invoke(this)
                heading?.invoke()
            }
            footer?.let { StepFooter(it) }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_l)),
            content = content,
        )
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
            .clip(CircleShape)
            .background(if (reached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh),
    )
}
