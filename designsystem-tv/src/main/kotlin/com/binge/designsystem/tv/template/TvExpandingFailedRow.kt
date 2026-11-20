package com.binge.designsystem.tv.template

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TvCompactButton
import com.binge.designsystem.tv.focus.tvShellArrivalTarget
import com.binge.designsystem.tv.layout.tvLayoutAnchor
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.tv.template.TvHubRowFailure
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

/**
 * An expanding-hub row whose titles could not be read: its heading, then the [failure]'s message and retry where the posters
 * would be. The slot is at least a resting row tall, so the row does not jump when the retry brings its titles in. The retry is
 * the row's one focus stop, so ↓, ↑ and entry land on it, as on the immersive hub's failed row.
 */
@Composable
internal fun TvExpandingFailedRow(
    title: String,
    failure: TvHubRowFailure,
    rowIndex: Int,
    focusable: Boolean,
    entryRequester: FocusRequester?,
    onFocused: () -> Unit,
    onEntryPlaced: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap))) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal))
                .tvLayoutAnchor(TvExpandingAnchors.heading(rowIndex)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = dimensionResource(TvR.dimen.tv_expanding_row_height))
                .padding(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
                Text(
                    text = failure.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TvCompactButton(
                    label = failure.retryLabel,
                    onClick = failure.onRetry,
                    style = TvButtonStyle.Primary,
                    modifier = Modifier
                        .then(
                            entryRequester?.let { Modifier.tvShellArrivalTarget(it).onGloballyPositioned { onEntryPlaced() } } ?: Modifier,
                        ).focusProperties { canFocus = focusable }
                        .onFocusChanged { if (it.isFocused) onFocused() },
                )
            }
        }
    }
}
