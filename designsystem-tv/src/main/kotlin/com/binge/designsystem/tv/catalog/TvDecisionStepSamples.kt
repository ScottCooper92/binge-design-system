@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvButtonSurface
import com.binge.designsystem.tv.template.TvDecisionBody
import com.binge.designsystem.tv.template.TvStepFlow
import com.binge.designsystem.tv.theme.TvButtonStyle

private const val DECISION_STEPS = 3

/**
 * A decision as one step of a TV step flow (#638): the flow's read-out above, the copy beside the points card in
 * [TvDecisionBody], and the two answers in the flow's own footer, accept focused.
 */
@Composable
fun TvDecisionInStepFlowSample() {
    TvStepFlow(
        stepCount = DECISION_STEPS,
        currentStep = 1,
        progressLabel = "Step 2 of 3",
        footerActions = {
            TvButtonSurface("Share usage data", TvButtonStyle.Primary, enabled = true, isFocused = true)
            TvButtonSurface("Not now", TvButtonStyle.Secondary, enabled = true, isFocused = false)
        },
    ) {
        TvDecisionBody(
            copy =
                DecisionCopy(
                    kicker = "Usage data",
                    title = "Help make the app better",
                    subtitle = "Share anonymous usage data, so problems are found and fixed sooner.",
                ),
            points =
                listOf(
                    DecisionPoint(Icons.Filled.BarChart, "What is shared", "Which screens you open and how long they take to load."),
                    DecisionPoint(Icons.Filled.VisibilityOff, "What never is", "Your server, your account and what you watch."),
                    DecisionPoint(Icons.Filled.ToggleOff, "Change it any time", "Press Settings, and it stops at once."),
                ),
        )
    }
}
