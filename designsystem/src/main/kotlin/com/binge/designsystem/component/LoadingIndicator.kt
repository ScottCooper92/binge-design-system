package com.binge.designsystem.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.theme.BingeExpressiveTheme

/**
 * Binge loading indicator.
 *
 * Uses standard Material 3 [CircularProgressIndicator].
 * Will be upgraded to use shape morphing when Material 3 Expressive becomes available.
 *
 * @param modifier Modifier to apply to the indicator
 */
@Composable
fun BingeLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
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
