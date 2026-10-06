@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.template.TvMessagePage
import com.binge.designsystem.tv.template.TvPageAction
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvStepFlow
import com.binge.designsystem.tv.template.TvStepHeading
import com.binge.designsystem.tv.template.TvTwoPaneCopy
import com.binge.designsystem.tv.template.TvTwoPanePage
import com.binge.designsystem.tv.template.TvTwoPaneSplit
import com.binge.designsystem.tv.theme.TvButtonStyle
import kotlinx.coroutines.delay
import com.binge.designsystem.R as DesR

private const val DEMO_STEP_COUNT = 3
private const val DEMO_LOAD_MILLIS = 1_500L
private const val DEMO_FIELD_COUNT = 12

private val DemoStepTitles = listOf("What do you watch most?", "Where do you watch?", "Share usage data?")

/**
 * A three-step flow running live: Continue moves on and focus lands on the new step's first choice, BACK steps
 * back from the second step on, and Continue on the last step starts again.
 */
@Composable
fun TvStepFlowDemo() {
    var step by remember { mutableIntStateOf(0) }
    val entry = remember { FocusRequester() }
    TvStepFlow(
        stepCount = DEMO_STEP_COUNT,
        currentStep = step,
        progressLabel = "Step ${step + 1} of $DEMO_STEP_COUNT",
        entry = entry,
        onBack = if (step > 0) ({ step-- }) else null,
        footerHint = "BACK returns to the step before.",
        footerActions = {
            TvButton(
                label = if (step < DEMO_STEP_COUNT - 1) "Continue" else "Start again",
                onClick = { step = (step + 1) % DEMO_STEP_COUNT },
                style = TvButtonStyle.Primary,
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l))) {
            TvStepHeading(kicker = "Step ${step + 1}", title = DemoStepTitles[step], subtitle = "Pick one; Continue is below.")
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
                TvButton(label = "First choice", onClick = {}, modifier = Modifier.focusRequester(entry))
                TvButton(label = "Second choice", onClick = {})
            }
        }
    }
}

/** A page that loads, then says it failed: focus moves from the loading plate to Retry, and Retry loads again. */
@Composable
fun TvMessagePageDemo() {
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(loading) {
        if (loading) {
            delay(DEMO_LOAD_MILLIS)
            loading = false
        }
    }
    TvMessagePage(
        headline = if (loading) null else "Server unreachable",
        body = if (loading) "Loading…" else "Check the server is running, then try again.",
        icon = if (loading) null else Icons.Filled.CloudOff,
        hosting = TvPageHosting.Overlay,
        loading = loading,
        primary = TvPageAction("Retry") { loading = true },
    )
}

/** A form longer than the pane: the fields scroll under focus and Save stays one press below whichever is focused. */
@Composable
fun TvTwoPanePagePinnedActionDemo() {
    TvTwoPanePage(
        split = TvTwoPaneSplit.Balanced,
        hosting = TvPageHosting.Overlay,
        pinnedAction = { TvButton(label = "Save", onClick = {}, style = TvButtonStyle.Primary) },
        copy = {
            TvTwoPaneCopy(
                headline = "Notification settings",
                body = "Move down through the fields: the pane scrolls, and Save stays where it is.",
            )
        },
        action = {
            repeat(DEMO_FIELD_COUNT) { index -> TvButton(label = "Field ${index + 1}", onClick = {}) }
        },
    )
}
