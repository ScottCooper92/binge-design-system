package com.binge.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Binge loading indicator: Material 3 Expressive's shape-morphing [LoadingIndicator], not the
 * classic circular spinner — this is the one place that choice lives, so every screen-level
 * loading state that routes through this composable picks it up without its own edit.
 *
 * @param modifier Modifier to apply to the indicator
 * @param color Tint for the indicator. Defaults to the theme's primary colour; a caller drawing
 * against a non-default background (e.g. a filled button's own foreground colour) can override it
 * so the spinner stays legible there instead of silently taking primary regardless of context.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BingeLoadingIndicator(modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    LoadingIndicator(
        modifier = modifier,
        color = color,
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewBingeLoadingIndicator() {
    BingeExpressiveTheme {
        BingeLoadingIndicator()
    }
}
