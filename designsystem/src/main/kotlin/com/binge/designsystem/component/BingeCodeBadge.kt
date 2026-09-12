package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.theme.labelSmallEmphasis

/**
 * Small bordered badge for short codes — ISO language/country codes, certifications, etc.
 * The label is rendered as-is (callers uppercase when appropriate).
 */
@Composable
fun BingeCodeBadge(label: String, modifier: Modifier = Modifier) {
    val shape = MaterialTheme.shapes.extraSmall
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(dimensionResource(R.dimen.hairline_thickness), MaterialTheme.colorScheme.outlineVariant, shape)
                .padding(
                    horizontal = dimensionResource(R.dimen.padding_s),
                    vertical = dimensionResource(R.dimen.padding_xxs),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmallEmphasis,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PreviewBingeCodeBadgeLanguage() {
    BingeExpressiveTheme {
        BingeCodeBadge(label = "EN")
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PreviewBingeCodeBadgeCountry() {
    BingeExpressiveTheme {
        BingeCodeBadge(label = "US")
    }
}
