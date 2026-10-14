@file:ScreenshotOnly

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.ScreenshotOnly
import com.binge.designsystem.tv.component.TvCheckboxRow
import com.binge.designsystem.tv.component.TvChoiceRow
import com.binge.designsystem.tv.component.TvChoiceUi
import com.binge.designsystem.tv.component.TvDetailAction
import com.binge.designsystem.tv.component.TvDetailActionRow
import com.binge.designsystem.tv.component.TvOptionRow
import com.binge.designsystem.tv.template.TvHubFailedRow
import com.binge.designsystem.tv.template.TvHubRowFailure
import com.binge.designsystem.tv.template.TvListPaneBoard
import com.binge.designsystem.tv.template.TvPaneAction
import com.binge.designsystem.tv.template.TvPaneGroup
import com.binge.designsystem.tv.template.TvPaneOption
import com.binge.designsystem.tv.template.TvPaneRow
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/*
 * The shared TV rows at their worst: every optional line filled and long text. They are the fixtures for the
 * no-clipping test, which renders each at font scales 1.3 and 2.0 and fails if any text is cut off by its parent, and
 * for the frames that show the same at a glance. On the TV a label is held to one line with an ellipsis, so these are
 * where a row's height has to keep up with a supporting line and an icon at a larger font.
 */

private const val LONG_LABEL = "A server name far longer than any row has room for, so that the label has to end in an ellipsis"
private const val LONG_SUPPORT = "Connects without encryption, so anyone on the same network can read what is sent. Use it only at home."

/** Option rows with an icon, a long label, a long supporting line, a trailing value and the chosen tick. */
@Composable
fun TvOptionRowWorstCaseSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)).width(dimensionResource(TvR.dimen.tv_text_field_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        TvOptionRow(
            label = LONG_LABEL,
            selected = true,
            onClick = {},
            icon = Icons.Filled.Folder,
            supportingText = LONG_SUPPORT,
            trailingContent = { Text("1,284", style = MaterialTheme.typography.labelMedium) },
        )
        TvOptionRow(label = LONG_LABEL, selected = false, onClick = {}, icon = Icons.Filled.Dns, supportingText = LONG_SUPPORT)
    }
}

/** Checkbox rows with an icon, a long label, a long supporting line, and one ruled out. */
@Composable
fun TvCheckboxRowWorstCaseSample() {
    Column(
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)).width(dimensionResource(TvR.dimen.tv_text_field_width)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_option_row_gap)),
    ) {
        TvCheckboxRow(label = LONG_LABEL, checked = true, onCheckedChange = {}, icon = Icons.Filled.Lock, supportingText = LONG_SUPPORT)
        TvCheckboxRow(
            label = LONG_LABEL,
            checked = false,
            onCheckedChange = {},
            icon = Icons.Filled.Dns,
            supportingText = LONG_SUPPORT,
            excluded = true,
        )
    }
}

/** A pill choice row whose labels are long, with an icon, the chosen one ticked. */
@Composable
fun TvChoiceRowWorstCaseSample() {
    TvChoiceRow(
        choices =
            listOf(
                TvChoiceUi(key = "a", label = "Everything on this server", icon = Icons.Filled.Dns),
                TvChoiceUi(key = "b", label = "Only what I have requested myself", icon = Icons.Filled.Folder),
                TvChoiceUi(key = "c", label = "Nothing at all"),
            ),
        selectedKey = "a",
        onSelect = {},
        modifier = Modifier.padding(dimensionResource(DesR.dimen.padding_l)),
    )
}

/** The most a detail page can offer: a primary and six labelled actions, more than a line holds at a large font. */
@Composable
fun TvDetailActionRowWorstCaseSample() {
    TvDetailActionRow(
        actions =
            listOf(
                TvDetailAction("Approve", Icons.Filled.Check, onClick = {}, isPrimary = true),
                TvDetailAction("Decline", Icons.Filled.Close, onClick = {}, showLabel = true),
                TvDetailAction("Mark as", Icons.Filled.Done, onClick = {}, showLabel = true),
                TvDetailAction("Report an issue", Icons.Filled.ReportProblem, onClick = {}, showLabel = true),
                TvDetailAction("Open in Binge", Icons.AutoMirrored.Filled.OpenInNew, onClick = {}, showLabel = true),
                TvDetailAction("Block", Icons.Filled.Block, onClick = {}, showLabel = true, style = TvButtonStyle.DestructiveOutline),
                TvDetailAction("Remove", Icons.Filled.Delete, onClick = {}, showLabel = true, style = TvButtonStyle.Destructive),
            ),
        entryFocus = remember { FocusRequester() },
        // The page's own side margins, which is the width a detail page gives its row.
        modifier = Modifier.padding(horizontal = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
    )
}

/**
 * A list-and-pane board whose described row fills everything: a long label with a count and a detail in the list, and
 * in the pane a body, options with long labels and an icon, a clear action and a note.
 */
@Composable
fun TvListPaneBoardWorstCaseSample() {
    TvListPaneBoard(
        title = "Settings and everything that decides how a request is sent",
        groups =
            listOf(
                TvPaneGroup(
                    title = "Playback and the quality every new request asks for",
                    note = "Changed from your phone, and it applies to every request made from now on.",
                    rows =
                        listOf(
                            TvPaneRow(
                                key = "quality",
                                label = LONG_LABEL,
                                body = LONG_SUPPORT,
                                detail = LONG_LABEL,
                                badgeCount = 128,
                                options =
                                    listOf(
                                        TvPaneOption(label = LONG_LABEL, selected = true, icon = Icons.Filled.Folder),
                                        TvPaneOption(label = LONG_SUPPORT, icon = Icons.Filled.Dns),
                                    ),
                                clear = TvPaneAction(label = "Clear this and use the server's default", onClick = {}),
                                note = LONG_SUPPORT,
                            ),
                            TvPaneRow(key = "other", label = LONG_LABEL, detail = LONG_SUPPORT, badgeAlert = true),
                        ),
                ),
            ),
        focusedKey = "quality",
        onFocusRow = {},
    )
}

/** A hub row that could not be read, with a long title and a message long enough to wrap at a large font. */
@Composable
fun TvHubFailedRowWorstCaseSample() {
    TvHubFailedRow(
        title = "Everything you have asked for, newest first, across every server",
        failure =
            TvHubRowFailure(
                message = "These requests could not be loaded, because the server did not answer before the connection timed out.",
                retryLabel = "Try loading these requests again",
                onRetry = {},
            ),
        cardWidth = dimensionResource(TvR.dimen.tv_immersive_card_width),
        entryFocusRequester = null,
        initiallyFocused = true,
        onFocused = {},
    )
}
