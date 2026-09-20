package com.binge.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.binge.designsystem.R
import com.binge.designsystem.theme.BingeExpressiveTheme

@Composable
fun ImagePlaceholder(modifier: Modifier = Modifier, iconAlignment: Alignment = Alignment.Center) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = iconAlignment,
    ) {
        Icon(
            imageVector = Icons.Default.Image,
            contentDescription = null,
            modifier = Modifier.size(dimensionResource(R.dimen.placeholder_icon_size)),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewImagePlaceholder() {
    BingeExpressiveTheme {
        ImagePlaceholder(Modifier.size(dimensionResource(R.dimen.card_width)))
    }
}
