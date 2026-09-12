package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.BingeIcons
import com.binge.designsystem.component.BingeNavFloatingTone
import com.binge.designsystem.component.BingeNavPresentation
import com.binge.designsystem.component.BingeNavSuiteItem
import com.binge.designsystem.component.BingeNavSuiteShell
import com.binge.designsystem.component.NavSuiteBadge
import com.binge.designsystem.preview.ScreenshotTheme

private enum class SampleTab {
    Movies,
    TvShows,
    Discover,
    Search,
    Account,
}

private fun sampleTabs(accountName: String?, showDiscover: Boolean): List<BingeNavSuiteItem> =
    listOfNotNull(
        sampleTab(SampleTab.Movies, "Movies", Icons.Default.Movie),
        sampleTab(SampleTab.TvShows, "TV shows", Icons.Default.Tv),
        if (showDiscover) sampleTab(SampleTab.Discover, "Discover", BingeIcons.Discover) else null,
        sampleTab(SampleTab.Search, "Search", Icons.Default.Search),
        sampleTab(SampleTab.Account, "Account", Icons.Default.Person, NavSuiteBadge.Label("3"), accountName, isAccount = true),
    )

private fun sampleTab(
    tab: SampleTab,
    label: String,
    icon: ImageVector,
    badge: NavSuiteBadge = NavSuiteBadge.None,
    avatarName: String? = null,
    isAccount: Boolean = false,
): BingeNavSuiteItem =
    BingeNavSuiteItem(
        key = tab,
        label = label,
        icon = icon,
        badge = badge,
        avatarName = avatarName,
        isAccount = isAccount,
    )

@Composable
private fun ShellSample(
    presentation: BingeNavPresentation,
    accountName: String?,
    showDiscover: Boolean,
) {
    ScreenshotTheme {
        BingeNavSuiteShell(
            items = sampleTabs(accountName, showDiscover),
            selectedKey = SampleTab.Movies,
            onSelect = {},
            presentation = presentation,
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Content", style = MaterialTheme.typography.headlineMedium)
            }
        }
    }
}

/**
 * Compact-portrait shell: the bottom bar, with the account as a signed-in initials avatar. Discover
 * is omitted — it's a tablet-only destination (`sw600dp`), absent on phone tiers.
 */
@Composable
fun BingeNavSuiteShellBottomBarSample() {
    ShellSample(BingeNavPresentation.BottomBar, accountName = "Ada Lovelace", showDiscover = false)
}

/** Compact-portrait shell, signed out: the account falls back to the Person icon. */
@Composable
fun BingeNavSuiteShellBottomBarSignedOutSample() {
    ShellSample(BingeNavPresentation.BottomBar, accountName = null, showDiscover = false)
}

/**
 * Portrait-tablet shell: the bottom bar with the 5-item tablet set (Discover included). A portrait
 * tablet is `sw600dp` (Discover shown) yet < 840dp wide (bottom bar, not the custom rail), so this
 * is the real portrait-tablet nav — the wide bar spreads all five items.
 */
@Composable
fun BingeNavSuiteShellTabletPortraitBarSample() {
    ShellSample(BingeNavPresentation.BottomBar, accountName = "Ada Lovelace", showDiscover = true)
}

/**
 * Tablet shell: the bespoke custom rail — browse items with the account avatar pinned to the bottom.
 * Discover is included — the tablet (`sw600dp`) tier shows it.
 */
@Composable
fun BingeNavSuiteShellTabletRailSample() {
    ShellSample(BingeNavPresentation.CustomRail, accountName = "Ada Lovelace", showDiscover = true)
}

/**
 * The expanded rail over artwork: the case the overlay exists for — a hub's still reaching the panel
 * edge and passing under the glass, rather than starting after a solid 96dp strip. The scrim is drawn
 * in the background colour, so this is the only sample where it is visible at all.
 */
@Composable
fun BingeNavSuiteShellTabletRailOverArtworkSample() {
    FloatingSample(
        BingeNavPresentation.CustomRail,
        BingeNavFloatingTone.AlwaysDark,
        showDiscover = true,
        overArtwork = true,
    )
}

/**
 * The same rail over the same artwork, in an RTL layout direction — the frame that proves the glass
 * ramp mirrors with the panel. The rail moves to the physical right edge on its own (the shell's
 * `Alignment.TopStart` and the rail's `WindowInsetsSides.Start` padding are both direction-aware), so
 * an absolute left-to-right gradient would leave the near-solid stop on the side facing the content
 * and the fade under the glyphs. #2022.
 */
@Composable
fun BingeNavSuiteShellTabletRailOverArtworkRtlSample() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        BingeNavSuiteShellTabletRailOverArtworkSample()
    }
}

/**
 * The expanded rail over a plain scrolling list — the wholesale-inset half of the overlay contract:
 * rows pad themselves clear of the rail through `navOverlayPadding` instead of bleeding under it.
 */
@Composable
fun BingeNavSuiteShellTabletRailOverListSample() {
    FloatingSample(BingeNavPresentation.CustomRail, BingeNavFloatingTone.AlwaysDark, showDiscover = true)
}
