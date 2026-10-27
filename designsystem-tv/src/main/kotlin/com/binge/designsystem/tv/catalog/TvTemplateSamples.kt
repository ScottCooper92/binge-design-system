@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.DecisionCopy
import com.binge.designsystem.DecisionPoint
import com.binge.designsystem.ErrorKind
import com.binge.designsystem.SaveStatus
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.template.ScreenAction
import com.binge.designsystem.tv.component.TvButtonSurface
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvQrCode
import com.binge.designsystem.tv.component.TvSaveStatus
import com.binge.designsystem.tv.nav.LocalTvContentInset
import com.binge.designsystem.tv.template.TvBoard
import com.binge.designsystem.tv.template.TvDecisionPage
import com.binge.designsystem.tv.template.TvErrorPage
import com.binge.designsystem.tv.template.TvMessagePage
import com.binge.designsystem.tv.template.TvPageHosting
import com.binge.designsystem.tv.template.TvScreenHeading
import com.binge.designsystem.tv.template.TvStepFlow
import com.binge.designsystem.tv.template.TvStepHeading
import com.binge.designsystem.tv.template.TvTwoPaneCopy
import com.binge.designsystem.tv.template.TvTwoPanePage
import com.binge.designsystem.tv.template.TvTwoPaneSplit
import com.binge.designsystem.tv.template.TvTwoPaneStyle
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/*
 * The page templates, each at the size it ships: a whole panel. Focus is shown through the stateless button
 * surface where a frame needs the lit state, so nothing here requests focus to be captured.
 */

private const val SAMPLE_STEP_COUNT = 3
private const val SAMPLE_ROWS = 5
private const val SAMPLE_GRID_COLUMNS = 3

private val SampleNotifications = listOf("New episodes", "Requests approved", "Downloads finished")

/** Sign-in on a television: the pitch on the left, a fixed-width panel carrying the QR hand-off on the right. */
@Composable
fun TvTwoPanePageSignInSample() {
    TvTwoPanePage(
        split = TvTwoPaneSplit.FixedAction(dimensionResource(TvR.dimen.tv_two_pane_fixed_pane_width)),
        hosting = TvPageHosting.Overlay,
        divider = false,
        actionScrolls = false,
        copy = {
            TvTwoPaneCopy(
                headline = "Sign in to sync your lists",
                body = "Scan the code with your phone, approve the request, then come back and press Done.",
                note = "Your watch history stays on this device until you sign in.",
            )
        },
        action = {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m), Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TvQrCode(content = "https://example.com/approve/sample", contentDescription = "Approval code")
                TvButtonSurface("Done", TvButtonStyle.Primary, enabled = true, isFocused = true)
            }
        },
    )
}

/** Setup as a form: centred copy at a fixed width, and the fields in a scrolling column beside it. */
@Composable
fun TvTwoPanePageFormSample() {
    TvTwoPanePage(
        split = TvTwoPaneSplit.FixedCopy(dimensionResource(TvR.dimen.tv_two_pane_fixed_pane_width)),
        hosting = TvPageHosting.PreShell,
        divider = false,
        copyAlignment = Alignment.CenterHorizontally,
        copy = {
            TvTwoPaneCopy(
                headline = "Connect your server",
                body = "Enter the address you use in a browser.",
                illustration = Icons.Filled.Dns,
                alignment = Alignment.CenterHorizontally,
            )
        },
        action = {
            SampleField("Server address", "media.example.com", focused = true)
            SampleField("Port", "5055", focused = false)
            TvButtonSurface("Continue", TvButtonStyle.Primary, enabled = true, isFocused = false)
        },
    )
}

/** A shell destination's list beside what its focused row describes, under the page title. */
@Composable
fun TvTwoPanePageListDetailSample() {
    InsetHosted {
        TvTwoPanePage(
            title = "Settings",
            split = TvTwoPaneSplit.ListDetail,
            hosting = TvPageHosting.Destination,
            actionScrolls = false,
            copy = {
                repeat(SAMPLE_ROWS) { index ->
                    TvButtonSurface("Setting ${index + 1}", TvButtonStyle.Secondary, enabled = true, isFocused = index == 0)
                }
            },
            action = {
                TvTwoPaneCopy(
                    headline = "Setting 1",
                    body = "What the focused row changes, and what each of its options means.",
                )
            },
        )
    }
}

/** The default split, copy and action sharing the page evenly — what a page gets when it names no split. */
@Composable
fun TvTwoPanePageBalancedSample() {
    TvTwoPanePage(
        split = TvTwoPaneSplit.Balanced,
        hosting = TvPageHosting.PreShell,
        copy = {
            TvTwoPaneCopy(
                headline = "Notification settings",
                body = "Copy and choices sit side by side, each taking half the page.",
            )
        },
        action = {
            repeat(SAMPLE_ROWS) { index ->
                TvButtonSurface("Option ${index + 1}", TvButtonStyle.Secondary, enabled = true, isFocused = index == 0)
            }
        },
    )
}

/** A picker board: the heading and a running count top-aligned beside the grid, the divider halving the gap. */
@Composable
fun TvTwoPanePageBoardSample() {
    TvTwoPanePage(
        split = TvTwoPaneSplit.ListDetail,
        style = TvTwoPaneStyle.Board,
        hosting = TvPageHosting.Overlay,
        actionScrolls = false,
        copy = {
            TvScreenHeading(title = "Services")
            Text(
                text = "Pick what you subscribe to, and lists show where each title streams.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "2 selected",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
                repeat(SAMPLE_ROWS) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
                        repeat(SAMPLE_GRID_COLUMNS) { column ->
                            val index = row * SAMPLE_GRID_COLUMNS + column
                            TvButtonSurface(
                                "Service ${index + 1}",
                                TvButtonStyle.Secondary,
                                enabled = true,
                                isFocused = index == 0,
                            )
                        }
                    }
                }
            }
        },
    )
}

/** Settings that save as made: no Save button, and the copy pane saying the last change was saved. */
@Composable
fun TvTwoPanePageSaveAsMadeSample() {
    val settings = remember { mutableStateListOf(true, false, true) }
    TvTwoPanePage(
        split = TvTwoPaneSplit.Balanced,
        style = TvTwoPaneStyle.Board,
        hosting = TvPageHosting.Overlay,
        copy = {
            TvTwoPaneCopy(headline = "Notification settings", body = "Each change saves as you make it.")
            TvSaveStatus(status = SaveStatus.Saved, onRetry = {})
        },
        action = {
            SampleNotifications.forEachIndexed { index, label ->
                TvCheckboxRow(
                    label = label,
                    checked = settings[index],
                    onCheckedChange = { settings[index] = it },
                    initiallyFocused = index == 0,
                )
            }
        },
    )
}

/** Choices lead and the explanation follows, with the commit pinned under the choices so it stays reachable. */
@Composable
fun TvTwoPanePageActionFirstSample() {
    TvTwoPanePage(
        split = TvTwoPaneSplit.ActionLed,
        hosting = TvPageHosting.PreShell,
        actionFirst = true,
        pinnedAction = {
            TvButtonSurface("Skip", TvButtonStyle.Secondary, enabled = true, isFocused = false)
            TvButtonSurface("Continue", TvButtonStyle.Primary, enabled = true, isFocused = true)
        },
        copy = {
            TvTwoPaneCopy(
                headline = "Pick your services",
                body = "Choose what you subscribe to, and lists show where each title streams.",
            )
        },
        action = {
            repeat(SAMPLE_ROWS) { index ->
                TvButtonSurface("Service ${index + 1}", TvButtonStyle.Secondary, enabled = true, isFocused = false)
            }
        },
    )
}

/** The middle step of a three-step flow, with a footer hint and its commit. */
@Composable
fun TvStepFlowSample() {
    TvStepFlow(
        stepCount = SAMPLE_STEP_COUNT,
        currentStep = 1,
        progressLabel = "Step 2 of 3",
        footerHint = "You can change this later in Settings.",
        footerActions = {
            TvButtonSurface("Continue", TvButtonStyle.Primary, enabled = true, isFocused = true)
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_l))) {
            TvStepHeading(
                kicker = "Personalise",
                title = "What do you watch most?",
                subtitle = "The app opens on the one you pick.",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
                TvButtonSurface("Movies", TvButtonStyle.Secondary, enabled = true, isFocused = false)
                TvButtonSurface("TV shows", TvButtonStyle.Secondary, enabled = true, isFocused = false)
            }
        }
    }
}

/** A step whose choices are still loading: the same frame, with a plate where the content will land. */
@Composable
fun TvStepFlowLoadingSample() {
    TvStepFlow(
        stepCount = SAMPLE_STEP_COUNT,
        currentStep = 0,
        progressLabel = "Step 1 of 3",
        loadingLabel = "Loading your choices…",
    ) {}
}

/** A full-bleed step: the content draws under the read-out and supplies its own commit. */
@Composable
fun TvStepFlowChromeOverContentSample() {
    TvStepFlow(
        stepCount = SAMPLE_STEP_COUNT,
        currentStep = 2,
        progressLabel = "Step 3 of 3",
        chromeOverContent = true,
    ) {
        TvTwoPanePage(
            split = TvTwoPaneSplit.ActionLed,
            hosting = TvPageHosting.PreShell,
            pinnedAction = {
                TvButtonSurface("Continue", TvButtonStyle.Primary, enabled = true, isFocused = true)
            },
            copy = {
                TvTwoPaneCopy(
                    headline = "Share usage data?",
                    body = "Anonymous counts of which screens are opened. Nothing about what you watch.",
                )
            },
            action = {
                TvButtonSurface("Share", TvButtonStyle.Secondary, enabled = true, isFocused = false)
                TvButtonSurface("Don't share", TvButtonStyle.Secondary, enabled = true, isFocused = false)
            },
        )
    }
}

/** A shell destination's board: the host's start inset cleared, its title in the heading band, the content below. */
@Composable
fun TvBoardSample() {
    InsetHosted {
        TvBoard(
            title = "Requests",
            hosting = TvPageHosting.Destination,
            trailingContent = { TvButtonSurface("Sort", TvButtonStyle.Secondary, enabled = true, isFocused = false) },
        ) {
            Text(text = "Pending", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
            repeat(SAMPLE_ROWS) { index ->
                TvButtonSurface("Request ${index + 1}", TvButtonStyle.Secondary, enabled = true, isFocused = index == 0)
            }
        }
    }
}

/** The same frame pushed as an overlay: no start inset to clear, so every edge takes the overscan margin. */
@Composable
fun TvBoardOverlaySample() {
    TvBoard(title = "Watch providers", hosting = TvPageHosting.Overlay) {
        Text(
            text = "Every edge clears the panel by the overscan margin.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** A gate before the shell: one headline, the reason, and the way forward. */
@Composable
fun TvMessagePageSample() {
    TvMessagePage(
        headline = "Update required",
        body = "This version can no longer reach the server. Update from the Play Store to carry on.",
        icon = Icons.Filled.SystemUpdate,
        hosting = TvPageHosting.PreShell,
        primary = ScreenAction("Update", {}),
        secondary = ScreenAction("Not now", {}),
    )
}

/** A message with nothing to press: the page holds focus itself so it cannot fall through. */
@Composable
fun TvMessagePageNoActionSample() {
    TvMessagePage(
        headline = "Server unreachable",
        body = "Check the server is running, then come back.",
        icon = Icons.Filled.CloudOff,
        hosting = TvPageHosting.Overlay,
    )
}

/** A failure as a whole page: the kind's glyph and copy, and Try again, announced as one. */
@Composable
fun TvErrorPageSample() {
    TvErrorPage(kind = ErrorKind.Server, onRetry = {}, hosting = TvPageHosting.Overlay)
}

/** A failure whose cause is unknown: the generic kind's glyph claims nothing specific, and Try again. */
@Composable
fun TvErrorPageGenericSample() {
    TvErrorPage(kind = ErrorKind.Generic, onRetry = {}, hosting = TvPageHosting.Overlay)
}

/** A failure the caller cannot retry, in the caller's own words: no button, so the page holds focus itself. */
@Composable
fun TvErrorPageNoRetrySample() {
    TvErrorPage(
        kind = ErrorKind.Configuration,
        onRetry = null,
        hosting = TvPageHosting.Overlay,
        title = "No API key",
        message = "Add a key in the server's settings, then open this page again.",
    )
}

/** The frame while a page loads: the plate alone, so the real message replaces it without a reflow. */
@Composable
fun TvMessagePageLoadingSample() {
    TvMessagePage(body = "Loading…", loading = true, hosting = TvPageHosting.Overlay)
}

/** Stands a page in for a shell destination whose host provides a start inset, so the frame shows the page clearing it. */
@Composable
private fun InsetHosted(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTvContentInset provides dimensionResource(TvR.dimen.tv_sample_content_inset),
        content = content,
    )
}

@Composable
private fun SampleField(
    label: String,
    value: String,
    focused: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_s))) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(modifier = Modifier.width(dimensionResource(TvR.dimen.tv_message_plate_max_width))) {
            TvButtonSurface(value, TvButtonStyle.Secondary, enabled = true, isFocused = focused)
        }
    }
}

/** The phone's decision on a television: the copy and a note beside the points, accept focused under them. */
@Composable
fun TvDecisionPageSample() {
    TvDecisionPage(
        copy =
            DecisionCopy(
                kicker = "Usage data",
                title = "Help make the app better",
                subtitle = "Share anonymous usage data, so problems are found and fixed sooner.",
                note = "You can change this at any time in Settings.",
            ),
        points =
            listOf(
                DecisionPoint(Icons.Filled.BarChart, "What is shared", "Which screens you open and how long they take to load."),
                DecisionPoint(Icons.Filled.VisibilityOff, "What never is", "Your server, your account and what you watch."),
                DecisionPoint(Icons.Filled.ToggleOff, "Change it any time", "Press Settings, and it stops at once."),
            ),
        accept = ScreenAction("Share usage data", {}),
        decline = ScreenAction("Not now", {}),
        hosting = TvPageHosting.PreShell,
        acceptInitiallyFocused = true,
    )
}

/** A note in two paragraphs through the note slot: a reassurance in the note's colour, then a quieter follow-up. */
@Composable
fun TvDecisionPageNoteContentSample() {
    TvDecisionPage(
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
        accept = ScreenAction("Share usage data", {}),
        decline = ScreenAction("Not now", {}),
        hosting = TvPageHosting.PreShell,
        acceptInitiallyFocused = true,
        noteContent = {
            Text(text = "You can change this at any time in Settings.")
            Text(
                text = "Nothing is sent until you say yes, and nothing is kept for longer than ninety days.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

/** A decision whose points run past the panel: the card scrolls under the answers, and ↑ walks it point by point. */
@Composable
fun TvDecisionPageTallSample() {
    TvDecisionPage(
        copy =
            DecisionCopy(
                kicker = "Usage data",
                title = "Help make the app better",
                subtitle = "Share anonymous usage data, so problems are found and fixed sooner.",
            ),
        points =
            listOf(
                DecisionPoint(Icons.Filled.BarChart, "What is shared", "Which screens you open and how long they take to load."),
                DecisionPoint(Icons.Filled.Speed, "How fast it is", "How long the server takes to answer, so a slow one shows up."),
                DecisionPoint(Icons.Filled.ErrorOutline, "What goes wrong", "Which requests fail, and with what error."),
                DecisionPoint(Icons.Filled.VisibilityOff, "What never is", "Your server, your account and what you watch."),
                DecisionPoint(Icons.Filled.Schedule, "How long it is kept", "Ninety days, then it is deleted."),
                DecisionPoint(Icons.Filled.ToggleOff, "Change it any time", "Press Settings, and it stops at once."),
            ),
        accept = ScreenAction("Share usage data", {}),
        decline = ScreenAction("Not now", {}),
        hosting = TvPageHosting.PreShell,
        acceptInitiallyFocused = true,
    )
}
