package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.CenteredContent
import com.binge.designsystem.R
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Content capped at the reading width and centred in a window wider than it. The tinted column is the
 * child that received the capped [Modifier]; the margin either side is the page background.
 */
@Composable
fun CenteredContentSample() {
    ScreenshotTheme {
        CenteredContent { capped ->
            Text(
                text = "Capped at the standard reading width and centred, so a full-width screen reads as a " +
                    "tidy column on a tablet or foldable.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = capped
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(dimensionResource(R.dimen.padding_m)),
            )
        }
    }
}
