@file:OnePerScreen
@file:CatalogGroup("Navigation bars")

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.BingeIcons
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeNavFloatingStyle
import com.binge.designsystem.component.BingeNavFloatingTone
import com.binge.designsystem.component.BingeNavPresentation
import com.binge.designsystem.component.BingeNavSuiteItem
import com.binge.designsystem.component.BingeNavSuiteShell
import com.binge.designsystem.component.NavSuiteBadge
import com.binge.designsystem.navOverlayPadding
import com.binge.designsystem.preview.ScreenshotTheme

private const val DEMO_ROW_COUNT = 60
private const val DEMO_ARTWORK_EVERY = 6
private const val DEMO_ARTWORK_CELLS = 3
private const val DEMO_POSTER_RATIO = 2f / 3f

private enum class DemoTab { Movies, TvShows, Discover, Search, Account }

private fun demoTabs(accountName: String?, showDiscover: Boolean): List<BingeNavSuiteItem> =
    listOfNotNull(
        BingeNavSuiteItem(key = DemoTab.Movies, label = "Movies", icon = Icons.Default.Movie),
        BingeNavSuiteItem(key = DemoTab.TvShows, label = "TV shows", icon = Icons.Default.Tv),
        if (showDiscover) BingeNavSuiteItem(key = DemoTab.Discover, label = "Discover", icon = BingeIcons.Discover) else null,
        BingeNavSuiteItem(key = DemoTab.Search, label = "Search", icon = Icons.Default.Search),
        BingeNavSuiteItem(
            key = DemoTab.Account,
            label = "Account",
            icon = Icons.Default.Person,
            badge = NavSuiteBadge.Label("3"),
            avatarName = accountName,
            isAccount = true,
        ),
    )

/**
 * The real shell over a long list that alternates text rows with bands of poster artwork, so the bar
 * is judged against both the backdrops the tone samples split apart, and the rows pad clear of it
 * through `navOverlayPadding` at the end. Tapping a destination selects it.
 */
@Composable
private fun NavDemoHost(
    presentation: BingeNavPresentation,
    tone: BingeNavFloatingTone = BingeNavFloatingTone.AlwaysDark,
    style: BingeNavFloatingStyle = BingeNavFloatingStyle.IconWithSelectedLabel,
    accountName: String? = "Ada Lovelace",
) {
    var selected by rememberSaveable { mutableStateOf(DemoTab.Movies) }
    ScreenshotTheme {
        BingeNavSuiteShell(
            items = demoTabs(accountName, showDiscover = presentation == BingeNavPresentation.CustomRail),
            selectedKey = selected,
            onSelect = { selected = it as DemoTab },
            presentation = presentation,
            floatingTone = tone,
            floatingStyle = style,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing)),
                contentPadding = navOverlayPadding(PaddingValues(dimensionResource(R.dimen.list_row_gap))),
            ) {
                items(DEMO_ROW_COUNT) { index ->
                    if (index % DEMO_ARTWORK_EVERY == 0) DemoArtworkBand() else DemoTextRow(index)
                }
            }
        }
    }
}

@Composable
private fun DemoArtworkBand() {
    val scheme = MaterialTheme.colorScheme
    val swatches = listOf(scheme.primaryContainer, scheme.tertiaryContainer, scheme.errorContainer)
    Row(Modifier.fillMaxWidth()) {
        repeat(DEMO_ARTWORK_CELLS) { Box(Modifier.weight(1f).aspectRatio(DEMO_POSTER_RATIO).background(swatches[it])) }
    }
}

@Composable
private fun DemoTextRow(index: Int) {
    Column(Modifier.padding(dimensionResource(R.dimen.list_row_padding))) {
        Text("Row $index", style = MaterialTheme.typography.titleMedium)
        Text("Subtitle text", style = MaterialTheme.typography.bodyMedium)
    }
}

/** The floating pill in the shell's defaults: always-dark, icon-first with the selected label. */
@Composable
fun FloatingBarDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar)
}

/** The floating pill, icon over label on every destination. */
@Composable
fun FloatingBarStackedDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar, style = BingeNavFloatingStyle.Stacked)
}

/** The floating pill, label-primary: the icon appears only on the selected destination. */
@Composable
fun FloatingBarTextFirstDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar, style = BingeNavFloatingStyle.TextFirst)
}

/** The floating pill in the standard tone, the low-contrast case over artwork. */
@Composable
fun FloatingBarStandardDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar, tone = BingeNavFloatingTone.Standard)
}

/** The floating pill in the vibrant tone, the louder primary-tinted container. */
@Composable
fun FloatingBarVibrantDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar, tone = BingeNavFloatingTone.Vibrant)
}

/** The floating pill in the high-contrast tone, an inverted surface whatever is behind it. */
@Composable
fun FloatingBarHighContrastDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar, tone = BingeNavFloatingTone.HighContrast)
}

/** The floating pill in the outlined tone, lifted a step with a hairline edge. */
@Composable
fun FloatingBarOutlinedDemo() {
    NavDemoHost(BingeNavPresentation.FloatingBar, tone = BingeNavFloatingTone.Outlined)
}

/** The docked bottom bar, with the account as a signed-in initials avatar. */
@Composable
fun BottomBarDemo() {
    NavDemoHost(BingeNavPresentation.BottomBar)
}

/** The docked bottom bar signed out: the account falls back to the person icon. */
@Composable
fun BottomBarSignedOutDemo() {
    NavDemoHost(BingeNavPresentation.BottomBar, accountName = null)
}

/** The tablet rail, with Discover and the account pinned to the bottom, the list passing under its glass. */
@Composable
fun NavRailDemo() {
    NavDemoHost(BingeNavPresentation.CustomRail)
}
