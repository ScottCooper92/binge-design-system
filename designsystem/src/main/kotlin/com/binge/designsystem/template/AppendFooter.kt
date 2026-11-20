package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeLoadingIndicator

/**
 * The footer under the last row of a paged list or grid: the library's loading indicator while the next page is on its way, or [failedLabel]
 * as a Try again button with a refresh mark once it failed, calling [onRetry]. The paged grid and the search screen both end in it, so a change
 * to the spinner or to the failed footer is made here once.
 */
@Composable
internal fun AppendFooter(
    failed: Boolean,
    failedLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.padding_m)),
        contentAlignment = Alignment.Center,
    ) {
        if (failed) {
            TextButton(onClick = onRetry) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text(
                    failedLabel,
                    modifier = Modifier.padding(start = dimensionResource(R.dimen.padding_s)),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        } else {
            BingeLoadingIndicator()
        }
    }
}
