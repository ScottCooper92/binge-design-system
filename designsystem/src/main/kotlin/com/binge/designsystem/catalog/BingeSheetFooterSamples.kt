package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.component.BingeSheetFooter
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * Public catalog samples for the containers / chrome group — sheets, dialogs, top bars, scaffolding.
 * Follows the convention on [MediaCardRatedSample]: a no-arg `@Composable` wrapped in [ScreenshotTheme]
 * that both the catalog `@Preview` and the matching screenshot test render. Modal windows don't
 * capture in screenshots, so samples target the stateless `*SheetContent`/`*DialogContent`, not the
 * modal wrapper.
 */
@Composable
fun BingeSheetFooterSample() {
    ScreenshotTheme {
        BingeSheetFooter(label = "Show results", onClick = {})
    }
}
