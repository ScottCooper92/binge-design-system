package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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

/** Enough rows to run off the bottom of every sample canvas, so the overlap is always visible. */
private const val SAMPLE_ROW_COUNT = 20

/** Poster-grid stand-in: enough cells to fill any sample canvas, at the poster aspect the app uses. */
private const val SAMPLE_ARTWORK_COLUMNS = 3
private const val SAMPLE_ARTWORK_CELLS = 24
private const val SAMPLE_POSTER_RATIO = 2f / 3f

private enum class FloatingSampleTab {
    Movies,
    TvShows,
    Discover,
    Search,
    Account,
}

private fun floatingSampleTabs(showDiscover: Boolean): List<BingeNavSuiteItem> =
    listOfNotNull(
        floatingSampleTab(FloatingSampleTab.Movies, "Movies", Icons.Default.Movie),
        floatingSampleTab(FloatingSampleTab.TvShows, "TV shows", Icons.Default.Tv),
        if (showDiscover) floatingSampleTab(FloatingSampleTab.Discover, "Discover", BingeIcons.Discover) else null,
        floatingSampleTab(FloatingSampleTab.Search, "Search", Icons.Default.Search),
        floatingSampleTab(
            FloatingSampleTab.Account,
            "Account",
            Icons.Default.Person,
            NavSuiteBadge.Label("3"),
            "Ada Lovelace",
        ),
    )

private fun floatingSampleTab(
    tab: FloatingSampleTab,
    label: String,
    icon: ImageVector,
    badge: NavSuiteBadge = NavSuiteBadge.None,
    avatarName: String? = null,
): BingeNavSuiteItem =
    BingeNavSuiteItem(
        key = tab,
        label = label,
        icon = icon,
        badge = badge,
        avatarName = avatarName,
        isAccount = tab == FloatingSampleTab.Account,
    )

/**
 * Scrolling list behind the nav, not a centred "Content" placeholder: the floating pill overlays
 * content rather than reserving height for it, so the sample has to show real rows running under the
 * bar — that overlap is the thing to judge.
 */
@Composable
internal fun FloatingSample(
    presentation: BingeNavPresentation,
    tone: BingeNavFloatingTone,
    showDiscover: Boolean,
    style: BingeNavFloatingStyle = BingeNavFloatingStyle.Stacked,
    overArtwork: Boolean = false,
    overEmptyBackground: Boolean = false,
    scrolledToEnd: Boolean = false,
) {
    ScreenshotTheme {
        BingeNavSuiteShell(
            items = floatingSampleTabs(showDiscover),
            selectedKey = FloatingSampleTab.Movies,
            onSelect = {},
            presentation = presentation,
            floatingTone = tone,
            floatingStyle = style,
        ) {
            if (overArtwork) {
                SampleArtworkGrid()
                return@BingeNavSuiteShell
            }
            if (overEmptyBackground) {
                SampleEmptyBackground()
                return@BingeNavSuiteShell
            }
            LazyColumn(
                state = rememberLazyListState(
                    initialFirstVisibleItemIndex = if (scrolledToEnd) SAMPLE_ROW_COUNT else 0,
                ),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_row_spacing)),
                // The consumer side of the fix: without navOverlayPadding the last rows sit under the
                // pill with no way to scroll them clear. Visible only from the end of the list, which
                // is what the scrolled sample captures.
                contentPadding = navOverlayPadding(PaddingValues(dimensionResource(R.dimen.list_row_gap))),
            ) {
                items((1..SAMPLE_ROW_COUNT).toList()) { index ->
                    Column(Modifier.padding(dimensionResource(R.dimen.list_row_padding))) {
                        Text("Row $index", style = MaterialTheme.typography.titleMedium)
                        Text("Subtitle text", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/**
 * Stands in for a poster grid: full-bleed cells in rotating theme colours. The plain-list samples
 * flatter the bar — over artwork the container has to hold its own against whatever is behind it,
 * which is the case that decides the tone.
 */
@Composable
internal fun SampleArtworkGrid() {
    val scheme = MaterialTheme.colorScheme
    val swatches = listOf(
        scheme.primaryContainer,
        scheme.tertiaryContainer,
        scheme.secondaryContainer,
        scheme.surfaceVariant,
        scheme.errorContainer,
        scheme.inversePrimary,
    )
    LazyVerticalGrid(columns = GridCells.Fixed(SAMPLE_ARTWORK_COLUMNS), modifier = Modifier.fillMaxSize()) {
        items(SAMPLE_ARTWORK_CELLS) { index ->
            Box(
                Modifier
                    .aspectRatio(SAMPLE_POSTER_RATIO)
                    .background(swatches[index % swatches.size]),
            )
        }
    }
}

/**
 * A bare themed background with one centred line, standing in for Account or an error screen — the
 * case where the bar has nothing but `background` behind it and the container's own tone is all the
 * separation there is.
 */
@Composable
internal fun SampleEmptyBackground() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Something went wrong", style = MaterialTheme.typography.bodyLarge)
    }
}

/** Phone portrait, standard tone — 4 items, the phone tab set. */
@Composable
fun BingeNavFloatingBarPhoneSample() {
    FloatingSample(BingeNavPresentation.FloatingBar, BingeNavFloatingTone.Standard, showDiscover = false)
}

/** Phone portrait, vibrant tone — the louder primary-tinted container. */
@Composable
fun BingeNavFloatingBarPhoneVibrantSample() {
    FloatingSample(BingeNavPresentation.FloatingBar, BingeNavFloatingTone.Vibrant, showDiscover = false)
}

/** Phone landscape — the case where a bottom pill spends scarce vertical space. */
@Composable
fun BingeNavFloatingBarPhoneLandscapeSample() {
    FloatingSample(BingeNavPresentation.FloatingBar, BingeNavFloatingTone.Standard, showDiscover = false)
}

/** Tablet portrait — 5 items, the widest the horizontal pill has to get. */
@Composable
fun BingeNavFloatingBarTabletPortraitSample() {
    FloatingSample(BingeNavPresentation.FloatingBar, BingeNavFloatingTone.Standard, showDiscover = true)
}

/** Phone portrait, label-primary: the icon appears only on the selected destination. */
@Composable
fun BingeNavFloatingBarPhoneTextFirstSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.Standard,
        showDiscover = false,
        style = BingeNavFloatingStyle.TextFirst,
    )
}

/** Tablet portrait, label-primary — the 5-item set, the widest this arrangement gets horizontally. */
@Composable
fun BingeNavFloatingBarTabletTextFirstSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.Standard,
        showDiscover = true,
        style = BingeNavFloatingStyle.TextFirst,
    )
}

/** Phone portrait, icon-first: unselected destinations are icon-only, the selected one expands. */
@Composable
fun BingeNavFloatingBarPhoneIconFirstSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.Standard,
        showDiscover = false,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
    )
}

/** Phone landscape, icon-first — the bucket that used to get the standard rail. */
@Composable
fun BingeNavFloatingBarPhoneLandscapeIconFirstSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.Standard,
        showDiscover = false,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
    )
}

/** Tablet portrait, icon-first — the 5-item set. */
@Composable
fun BingeNavFloatingBarTabletIconFirstSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.Standard,
        showDiscover = true,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
    )
}

/** Scrolled to the end of the list: the last row must clear the pill, not sit under it. */
@Composable
fun BingeNavFloatingBarScrolledToEndSample() {
    FloatingSample(
        BingeNavPresentation.FloatingBar,
        BingeNavFloatingTone.AlwaysDark,
        showDiscover = false,
        style = BingeNavFloatingStyle.IconWithSelectedLabel,
        scrolledToEnd = true,
    )
}
