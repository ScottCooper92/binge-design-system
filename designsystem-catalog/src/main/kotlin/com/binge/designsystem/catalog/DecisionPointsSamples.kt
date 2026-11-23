@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.component.DecisionPointsCard
import com.binge.designsystem.preview.ScreenshotTheme

internal val SampleDecisionPoints =
    listOf(
        DecisionPoint(Icons.Filled.BarChart, "What is shared", "Which screens you open and how long they take to load."),
        DecisionPoint(Icons.Filled.VisibilityOff, "What never is", "Your server, your account and what you watch."),
        DecisionPoint(Icons.Filled.ToggleOff, "Change it any time", "Turn it off in Settings, and it stops at once."),
    )

/** The points a decision rests on: an icon, a title and a line of detail each, hairlines between. */
@Composable
fun DecisionPointsCardSample() {
    ScreenshotTheme { DecisionPointsCard(points = SampleDecisionPoints) }
}
