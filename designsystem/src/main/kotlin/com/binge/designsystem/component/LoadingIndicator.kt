package com.binge.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Binge loading indicator: Material 3 Expressive's shape-morphing [LoadingIndicator], not the
 * classic circular spinner — this is the one place that choice lives, so every screen-level
 * loading state that routes through this composable picks it up without its own edit.
 *
 * @param modifier Modifier to apply to the indicator
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BingeLoadingIndicator(modifier: Modifier = Modifier) {
    LoadingIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeLoadingIndicator() {
    BingeExpressiveTheme {
        BingeLoadingIndicator()
    }
}
