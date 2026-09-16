package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.binge.designsystem.component.BingeActionFooter
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public catalog samples for the containers / chrome group — sheets, dialogs, top bars, scaffolding.
 * Follows the convention on [MediaCardRatedSample]: a no-arg `@Composable` wrapped in [ScreenshotTheme]
 * that both the catalog `@Preview` and the matching screenshot test render. Modal windows don't
 * capture in screenshots, so samples target the stateless `*SheetContent`/`*DialogContent`, not the
 * modal wrapper.
 */
@Composable
fun BingeActionFooterSample() {
    ScreenshotTheme {
        BingeActionFooter(label = "Show results", onClick = {})
    }
}

/**
 * The same footer on a page rather than a sheet: no band of its own, and no bottom spacing where the
 * host's scaffold has already inset. Paired with [BingeActionFooterSample] because the two together
 * are what make the container parameter's job legible — one shows the band, the other its absence.
 */
@Composable
fun BingeActionFooterOnPageSample() {
    ScreenshotTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            BingeActionFooter(
                label = "Save",
                onClick = {},
                containerColor = Color.Transparent,
                bottomPadding = 0.dp,
            )
        }
    }
}
