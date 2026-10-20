package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.tv.preview.TvPreviews
import com.binge.designsystem.tv.preview.TvScreenshotTheme
import com.binge.designsystem.tv.preview.TvWorstCaseFontPreviews

/** Screenshot coverage for the TV top bar: at rest, with each kind of item focused, and with long labels at larger fonts. */
class TvTopBarSamplesScreenshotTest {
    @PreviewTest
    @TvPreviews
    @Composable
    fun topBar() = Frame { TvTopBarSample() }

    /** A tab focused: a light pill with dark text, and the tabs at full size. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun topBarTabFocused() = Frame { TvTopBarSample(items = TopBarSampleItems, focusedKey = "tv") }

    @PreviewTest
    @TvPreviews
    @Composable
    fun topBarAvatarFocused() = Frame { TvTopBarSample(items = TopBarSampleItems, focusedKey = "account") }

    @PreviewTest
    @TvPreviews
    @Composable
    fun topBarEndIconFocused() = Frame { TvTopBarSample(items = TopBarSampleItems, focusedKey = "settings") }

    /** Long labels at 1.3x and 2x: every tab stays on one line and the bar keeps its height. */
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun topBarLongLabels() = Frame { TvTopBarLongLabelsSample() }

    /** The footer as a label pill, which is wider than the gear: the tabs must not run under it. */
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun topBarLabelFooter() = Frame { TvTopBarLabelFooterSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
