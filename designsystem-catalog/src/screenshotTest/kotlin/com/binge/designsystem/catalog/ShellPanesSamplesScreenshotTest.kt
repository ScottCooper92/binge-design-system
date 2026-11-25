package com.binge.designsystem.catalog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.preview.ScreenWidthPreviews

/** Screenshot coverage for [com.binge.designsystem.pane.ShellPanes]: three panes with an item, with its placeholder, and two with the default page. */
class ShellPanesSamplesScreenshotTest {
    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun threePanes() = ShellPanesSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun placeholder() = ShellPanesPlaceholderSample()

    @PreviewTest
    @ScreenWidthPreviews
    @Composable
    fun defaultPage() = ShellPanesDefaultPageSample()
}
