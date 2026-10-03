package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeOutlinedButton
import com.binge.designsystem.component.BingeSnackbarHost
import com.binge.designsystem.component.SnackbarMessageKind
import com.binge.designsystem.component.showSnackbar
import com.binge.designsystem.preview.ScreenshotTheme
import kotlinx.coroutines.launch

/**
 * The snackbar host for real: a button per message kind, each shown through [BingeSnackbarHost] for
 * the time its kind sets, which a frame of the pill cannot show. A confirmation leaves on its own, an
 * error offers Retry and stays longer, and an in-progress message stays until dismissed. The line
 * below the buttons reports how the last one ended.
 */
@Composable
fun BingeSnackbarHostDemo() {
    ScreenshotTheme {
        val host = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var outcome by remember { mutableStateOf("Nothing shown yet") }

        fun show(
            message: String,
            kind: SnackbarMessageKind,
            actionLabel: String? = null,
        ) {
            host.currentSnackbarData?.dismiss()
            scope.launch {
                val result = host.showSnackbar(message, kind, actionLabel)
                outcome = if (result == SnackbarResult.ActionPerformed) "Action tapped" else "Dismissed or timed out"
            }
        }
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(dimensionResource(R.dimen.padding_m)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
            ) {
                BingeOutlinedButton(
                    label = "Confirmation",
                    onClick = { show("Added to your watchlist", SnackbarMessageKind.Confirmation) },
                )
                BingeOutlinedButton(
                    label = "Error with Retry",
                    onClick = { show("Couldn't save your rating", SnackbarMessageKind.Error, actionLabel = "Retry") },
                )
                BingeOutlinedButton(
                    label = "In progress",
                    onClick = { show("Downloading images…", SnackbarMessageKind.InProgress) },
                )
                Text(outcome)
            }
            BingeSnackbarHost(host, Modifier.align(Alignment.BottomCenter))
        }
    }
}
