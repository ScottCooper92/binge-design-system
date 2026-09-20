package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.LocalNavOverlayInsets
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeNavPresentation
import com.binge.designsystem.component.BingeNavSuiteItem
import com.binge.designsystem.component.BingeNavSuiteShell
import com.binge.designsystem.component.BingeSnackbar
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Binge's snackbar pill with an accent action. Targets the stateless [BingeSnackbar] directly: an
 * M3 `SnackbarHost` is empty at frame 0, so `BingeSnackbarHost` itself would screenshot blank.
 */
@Composable
fun BingeSnackbarSample() {
    ScreenshotTheme {
        BingeSnackbar(
            message = "Removed from watchlist",
            actionLabel = "Undo",
            onActionClick = {},
            showDismissAction = false,
            onDismiss = {},
        )
    }
}

/** Message-only with a trailing dismiss icon — the error/no-action form. */
@Composable
fun BingeSnackbarDismissSample() {
    ScreenshotTheme {
        BingeSnackbar(
            message = "Couldn't complete that action. Please try again.",
            actionLabel = null,
            onActionClick = {},
            showDismissAction = true,
            onDismiss = {},
        )
    }
}

private val snackbarSampleTabs = listOf(
    BingeNavSuiteItem(key = "movies", label = "Movies", icon = Icons.Default.Movie),
    BingeNavSuiteItem(key = "search", label = "Search", icon = Icons.Default.Search),
    BingeNavSuiteItem(key = "account", label = "Account", icon = Icons.Default.Person, isAccount = true),
)

/**
 * The pill positioned exactly as [com.binge.designsystem.component.BingeSnackbarHost] offsets it —
 * its fixed gap plus [LocalNavOverlayInsets]'s bottom inset — inside a floating-bar shell, so the
 * offset that clears the bar (#84) is pinned by a baseline rather than only unit-asserted.
 */
@Composable
fun BingeSnackbarOverFloatingBarSample() {
    ScreenshotTheme {
        BingeNavSuiteShell(
            items = snackbarSampleTabs,
            selectedKey = "movies",
            onSelect = {},
            presentation = BingeNavPresentation.FloatingBar,
        ) {
            val navOverlayBottom = LocalNavOverlayInsets.current.calculateBottomPadding()
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                BingeSnackbar(
                    message = "Removed from watchlist",
                    actionLabel = "Undo",
                    onActionClick = {},
                    showDismissAction = false,
                    onDismiss = {},
                    modifier = Modifier.padding(
                        start = dimensionResource(R.dimen.padding_m),
                        end = dimensionResource(R.dimen.padding_m),
                        bottom = dimensionResource(R.dimen.padding_s) + navOverlayBottom,
                    ),
                )
            }
        }
    }
}
