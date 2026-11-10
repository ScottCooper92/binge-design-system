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

    /** The start avatar and five tabs at 1.3x and 2x font: every label stays whole, as the bar has the room for them. */
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun topBarHeaderTabs() = Frame { TvTopBarHeaderTabsSample() }

    /** The footer as a label pill, which is wider than the gear: the tabs must not run under it. */
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun topBarLabelFooter() = Frame { TvTopBarLabelFooterSample() }

    /** Content in the trailing slot: the tabs stay centred, and the ends are the caller's. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun topBarTrailing() = Frame { TvTopBarTrailingSample() }

    /** No tabs and no start item: the avatar and the gear alone, at the end, with the bar keeping its height at 1.3x and 2x. */
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun topBarNoRail() = Frame { TvTopBarNoRailSample() }

    /** The account menu closed in the trailing slot: the avatar and the gear. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun accountMenuClosed() = Frame { TvAccountMenuSample() }

    /** The menu open over the avatar: the header, the quota line and the rows, Sign out last. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun accountMenuOpen() = Frame { TvAccountMenuOpenSample() }

    /** The open menu at 1.3x and 2x font: the rows stay on one line and nothing is clipped. */
    @PreviewTest
    @TvWorstCaseFontPreviews
    @Composable
    fun accountMenuOpenLargeFont() = Frame { TvAccountMenuOpenSample() }

    /** The menu anchored at the start: the avatar leads the header and the panel grows toward the end. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun accountMenuStart() = Frame { TvAccountMenuStartSample() }

    /** The avatar as the bar's current destination: its ring is drawn faintly. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun accountMenuSelected() = Frame { TvAccountMenuSelectedSample() }

    /** The current destination with the menu open over it. */
    @PreviewTest
    @TvPreviews
    @Composable
    fun accountMenuSelectedOpen() = Frame { TvAccountMenuSelectedOpenSample() }

    @Composable
    private fun Frame(content: @Composable () -> Unit) {
        TvScreenshotTheme(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
