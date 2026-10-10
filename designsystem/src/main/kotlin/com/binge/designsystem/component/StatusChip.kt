package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeSentiment
import com.binge.designsystem.theme.BingeShapes
import com.binge.designsystem.theme.accent
import com.binge.designsystem.theme.fill
import com.binge.designsystem.theme.tonalContainer

/** The ring around the dot: the same fill at low alpha, so a solid dot nests in a soft halo. */
private const val STATUS_CHIP_DOT_HALO_ALPHA = 0.30f

/**
 * A small pill saying where something stands: a request, an issue, a job. The [label] names the state and the
 * [sentiment] colours it, so colour reinforces the words rather than carrying them alone. The label and the wash take
 * the sentiment's accent; the dot, which is graphical and needs only 3:1, takes its brighter fill inside a soft halo.
 * [showDot] false leaves the label and wash alone, for a row that already shows a state glyph of its own.
 *
 * The consumer maps its own states onto [BingeSentiment], one line per state, so the same state reads the same in
 * every app.
 *
 * The wash is lighter in a light theme, where the accents are darker for contrast, so every label stays clear of WCAG AA.
 */
@Composable
fun StatusChip(
    label: String,
    sentiment: BingeSentiment,
    modifier: Modifier = Modifier,
    showDot: Boolean = true,
) {
    val accent = sentiment.accent()
    Row(
        modifier =
            modifier
                .clip(BingeShapes.Pill)
                .background(accent.tonalContainer())
                .padding(
                    horizontal = dimensionResource(R.dimen.status_chip_padding_h),
                    vertical = dimensionResource(R.dimen.status_chip_padding_v),
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.status_chip_dot_spacing)),
    ) {
        if (showDot) StatusDot(sentiment.fill())
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = accent)
    }
}

@Composable
private fun StatusDot(fill: Color) {
    Box(
        modifier =
            Modifier
                .size(dimensionResource(R.dimen.status_chip_dot_halo_size))
                .background(fill.copy(alpha = STATUS_CHIP_DOT_HALO_ALPHA), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            Modifier
                .size(dimensionResource(R.dimen.status_chip_dot_size))
                .background(fill, CircleShape),
        )
    }
}
