package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.paging.LoadState
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.PagedAppendFooter

/** The footer while the next page loads: a spinner under the last row. */
@Composable
fun PagedAppendFooterLoadingSample() {
    ScreenshotTheme {
        Column {
            PagedAppendFooter(state = LoadState.Loading, retryLabel = "Tap to retry", onRetry = {})
        }
    }
}

/** The footer after the next page failed: a tappable line. Pressing it starts loading again, so the control works. */
@Composable
fun PagedAppendFooterRetrySample() {
    ScreenshotTheme {
        var failed by rememberSaveable { mutableStateOf(true) }
        Column {
            PagedAppendFooter(
                state = if (failed) LoadState.Error(IllegalStateException("offline")) else LoadState.Loading,
                retryLabel = "Couldn't load more. Tap to retry",
                onRetry = { failed = false },
            )
        }
    }
}
