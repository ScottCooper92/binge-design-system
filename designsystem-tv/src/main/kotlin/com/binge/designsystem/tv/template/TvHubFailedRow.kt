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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvSectionTitle
import com.binge.designsystem.tv.nav.tvContentGutterStart
import com.binge.designsystem.R as DesR
import com.binge.designsystem.tv.R as TvR

private const val POSTER_RATIO = 2f / 3f

/**
 * A hub row whose items could not be read: its title, then the [failure]'s message and retry where the cards would be.
 * The slot is at least a card tall, so the row does not jump when the retry brings its items in. The retry is the row's
 * one focus stop, so entering the row lands on it.
 */
@Composable
internal fun TvHubFailedRow(
    title: String,
    failure: TvHubRowFailure,
    cardWidth: Dp,
    entryFocusRequester: FocusRequester?,
    initiallyFocused: Boolean,
    onFocused: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(TvR.dimen.tv_media_row_header_gap)),
    ) {
        TvSectionTitle(text = title)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = cardWidth / POSTER_RATIO)
                    .padding(start = tvContentGutterStart(), end = dimensionResource(TvR.dimen.tv_overscan_horizontal)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_m))) {
                Text(
                    text = failure.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TvButton(
                    label = failure.retryLabel,
                    onClick = failure.onRetry,
                    initiallyFocused = initiallyFocused,
                    modifier =
                        Modifier
                            .then(entryFocusRequester?.let { Modifier.focusRequester(it) } ?: Modifier)
                            .onFocusChanged { if (it.isFocused) onFocused() },
                )
            }
        }
    }
}
