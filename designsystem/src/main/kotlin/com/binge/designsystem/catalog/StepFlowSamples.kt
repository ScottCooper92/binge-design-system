@file:OnePerScreen(fullScreen = true)

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.component.BingeFilledButton
import com.binge.designsystem.component.BingeTextButton
import com.binge.designsystem.component.DecisionHero
import com.binge.designsystem.component.DecisionPointsCard
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.DecisionHeading
import com.binge.designsystem.template.StepFlowScreen
import com.binge.designsystem.template.StepHeading

private const val CONNECTION_STEPS = 2
private const val DECISION_STEPS = 3

/** A flow opened from inside the app: its own bar carries a title, and its first step has a Back that leaves. */
@Composable
fun StepFlowScreenTitledSample() {
    var step by remember { mutableIntStateOf(0) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        ConnectionFlow(step = step, onStep = { step = it }, onExit = LocalDemoBack.current)
    }
}

/** The same flow in the end pane of a wide window. The pane is taller than wide, so the step stacks. */
@Composable
fun StepFlowScreenInPaneSample() {
    var step by remember { mutableIntStateOf(0) }
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceContainer))
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                ConnectionFlow(step = step, onStep = { step = it }, onExit = LocalDemoBack.current)
            }
        }
    }
}

/**
 * A decision as one step of a flow, split-aware (#638): the hero in the flow's aside, the heading in its heading slot
 * and the points in its content, so a space wider than tall puts the hero and heading beside the points.
 */
@Composable
fun DecisionInStepFlowSplitSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        StepFlowScreen(
            stepCount = DECISION_STEPS,
            currentStep = 1,
            onBack = {},
            aside = { DecisionHero(icon = Icons.Filled.BarChart, badge = Icons.Filled.Lock) },
            heading = {
                DecisionHeading(
                    DecisionCopy(
                        kicker = "Usage data",
                        title = "Help make the app better",
                        subtitle = "Share anonymous usage data, so problems are found and fixed sooner.",
                    ),
                )
            },
            footer = {
                BingeFilledButton(label = "Share usage data", onClick = {}, modifier = Modifier.fillMaxWidth())
                BingeTextButton(label = "Not now", onClick = {}, modifier = Modifier.fillMaxWidth())
            },
        ) {
            DecisionPointsCard(
                listOf(
                    DecisionPoint(Icons.Filled.BarChart, "What is shared", "Which screens you open and how long they take to load."),
                    DecisionPoint(Icons.Filled.VisibilityOff, "What never is", "Your server, your account and what you watch."),
                    DecisionPoint(Icons.Filled.ToggleOff, "Change it any time", "Turn it off in Settings, and it stops at once."),
                ),
            )
        }
    }
}

@Composable
private fun ConnectionFlow(
    step: Int,
    onStep: (Int) -> Unit,
    onExit: () -> Unit,
) {
    StepFlowScreen(
        stepCount = CONNECTION_STEPS,
        currentStep = step,
        onBack = { onStep(step - 1) },
        title = "Edit connection",
        onExit = onExit,
        heading = { StepHeading(title = "Server address", subtitle = "Where the app finds your server.") },
        footer = {
            BingeFilledButton(
                label = "Continue",
                onClick = { onStep((step + 1).coerceAtMost(CONNECTION_STEPS - 1)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
    ) {}
}
