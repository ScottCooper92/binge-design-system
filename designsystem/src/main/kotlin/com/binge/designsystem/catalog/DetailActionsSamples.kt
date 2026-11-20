@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binge.designsystem.component.BingeStatusButton
import com.binge.designsystem.component.BingeStatusButtonEmphasis
import com.binge.designsystem.component.DetailActions
import com.binge.designsystem.component.DetailActionsSkeleton
import com.binge.designsystem.component.DetailRoundAction
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeSentiment

/**
 * A detail page's action row with a Request primary and three round actions: on a phone the primary takes a row of its own and the round
 * buttons sit centred under it, and at the expanded width they share one row. The Watchlist button flips between its filled and its outlined
 * bookmark when tapped in the catalog, as the page's does.
 */
@Composable
fun DetailActionsSample() {
    ScreenshotTheme {
        var onList by remember { mutableStateOf(false) }
        DetailActions(
            primary = { modifier ->
                BingeStatusButton(
                    label = "Request",
                    onClick = {},
                    sentiment = BingeSentiment.Neutral,
                    modifier = modifier,
                    icon = Icons.Filled.Add,
                    emphasis = BingeStatusButtonEmphasis.Filled,
                )
            },
            roundActions = listOf(
                DetailRoundAction(Icons.Filled.PlayArrow, "Trailer", onClick = {}),
                DetailRoundAction(
                    icon = if (onList) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    label = "Watchlist",
                    onClick = { onList = !onList },
                    contentDescription = if (onList) "Remove from watchlist" else "Add to watchlist",
                ),
                DetailRoundAction(Icons.Filled.ReportProblem, "Report", onClick = {}),
            ),
        )
    }
}

/** The same row with one round action: the primary and the Trailer button share one row on a phone, with the primary taking the rest. */
@Composable
fun DetailActionsSingleSample() {
    ScreenshotTheme {
        DetailActions(
            primary = { modifier ->
                BingeStatusButton(
                    label = "Available",
                    onClick = {},
                    sentiment = BingeSentiment.Positive,
                    modifier = modifier,
                    emphasis = BingeStatusButtonEmphasis.Tonal,
                )
            },
            roundActions = listOf(DetailRoundAction(Icons.Filled.PlayArrow, "Trailer", onClick = {})),
        )
    }
}

/** The action row while the page loads, for a title that will show three round actions: the primary's plate and three round plates, stacked as the loaded row is. */
@Composable
fun DetailActionsSkeletonSample() {
    ScreenshotTheme { DetailActionsSkeleton(buttons = 3) }
}

/**
 * The action row with a Trailer that opens a video outside the app: its label ends in the open-in-new mark, which a screen reader announces
 * after the name as "Opens outside the app". The other round actions are as they were.
 */
@Composable
fun DetailActionsTrailingMarkSample() {
    ScreenshotTheme {
        DetailActions(
            primary = { modifier ->
                BingeStatusButton(
                    label = "Play",
                    onClick = {},
                    sentiment = BingeSentiment.Neutral,
                    modifier = modifier,
                    icon = Icons.Filled.PlayArrow,
                    emphasis = BingeStatusButtonEmphasis.Filled,
                )
            },
            roundActions = listOf(
                DetailRoundAction(
                    icon = Icons.Filled.PlayArrow,
                    label = "Trailer",
                    onClick = {},
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    trailingContentDescription = "Opens outside the app",
                ),
                DetailRoundAction(Icons.Outlined.BookmarkBorder, "Watchlist", onClick = {}),
            ),
        )
    }
}
