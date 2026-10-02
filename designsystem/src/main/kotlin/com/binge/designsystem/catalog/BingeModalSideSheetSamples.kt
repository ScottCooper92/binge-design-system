@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSideSheetPanel
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * The side sheet's panel pinned to the end edge, with a title and a line of body copy.
 * `BingeModalSideSheet` wraps it in a `Dialog`, a modal window that doesn't capture, so this renders
 * the panel's own resting chrome — container, start corners, width — the same stateless-content
 * shape [BingeBottomSheetSample] uses. The scrim and slide are not framed.
 */
@Composable
fun BingeModalSideSheetSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.CenterEnd,
        ) {
            BingeSideSheetPanel {
                Text(
                    text = "Filters",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                )
                Text(
                    text = "Narrow the list without leaving it.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
                )
            }
        }
    }
}
