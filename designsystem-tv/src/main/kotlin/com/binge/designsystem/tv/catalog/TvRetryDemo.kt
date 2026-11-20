package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.binge.designsystem.tv.component.TvButton

/**
 * Keeps a failure sample's own state, seeded as failed so its frame is unchanged. Try again moves it to a loaded state, as a real
 * source does, and a button there brings the failure back, so the catalog app can show the retry more than once.
 */
@Composable
internal fun RetryableSample(failure: @Composable (onRetry: () -> Unit) -> Unit) {
    var failed by remember { mutableStateOf(true) }
    if (failed) {
        failure { failed = false }
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            TvButton(label = "Loaded. Show the failure again", onClick = { failed = true })
        }
    }
}
