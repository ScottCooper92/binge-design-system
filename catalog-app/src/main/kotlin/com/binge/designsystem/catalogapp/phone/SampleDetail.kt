package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.catalog.LocalDemoBack
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleLocale
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.overrides.WithOverrides
import com.binge.designsystem.catalogapp.registry.CatalogComponent
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.component.BingeTopBar
import com.binge.designsystem.component.HintCard
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.theme.BingeExpressiveTheme
import com.binge.designsystem.R as DesR

/**
 * One component's page, in one of two layouts. Most components list every variant, one under the
 * next, so they compare at a glance. A component a screen holds one of ([CatalogComponent.onePerScreen])
 * shows one variant full screen instead, and its variant is chosen in the tweaks sheet with the
 * overrides; the top bar names the variant on show.
 *
 * The overrides sit behind a floating tweaks button, on every page, so the samples keep the screen. The
 * button carries a dot while any override is off its default, since the sheet is shut. A component that is
 * screen chrome itself ([CatalogComponent.fullScreen]) gets none of this page's chrome: see
 * [FullScreenVariant].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleDetail(
    entry: CatalogEntry,
    variants: List<CatalogEntry>,
    onVariantChange: (CatalogEntry) -> Unit,
    overrides: SampleOverrides,
    onDarkChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onRtlChange: (Boolean) -> Unit,
    onLocaleChange: (SampleLocale) -> Unit,
    onBack: (() -> Unit)?,
) {
    var showTweaks by rememberSaveable { mutableStateOf(false) }
    val tweaked = overrides != SampleOverrides(dark = isSystemInDarkTheme(), fontScale = FontScalePresets.first(), rtl = false)
    val component = CatalogComponent(entry.group, entry.groupName, variants)
    val onePerScreen = component.onePerScreen
    if (component.fullScreen) {
        FullScreenVariant(entry, overrides, onBack) { TweaksFab(tweaked, onClick = { showTweaks = true }) }
    } else {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.surface,
            topBar = {
                BingeTopBar(
                    title = if (onePerScreen && variants.size > 1) "${entry.groupName} · ${entry.name}" else entry.groupName,
                    onBack = onBack,
                )
            },
            floatingActionButton = { TweaksFab(tweaked, onClick = { showTweaks = true }) },
        ) { padding ->
            // Edge to edge: only the top bar's height pads the page. The variants draw under the
            // navigation bar, and a list pads its own end clear of it.
            val body = Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())
            SampleStage(overrides, body) {
                if (onePerScreen) {
                    SingleVariant(entry, overrides, modifier = Modifier.fillMaxSize())
                } else {
                    VariantList(variants, selected = entry, overrides = overrides, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
    if (showTweaks) {
        TweaksSheet(
            overrides = overrides,
            onDarkChange = onDarkChange,
            onFontScaleChange = onFontScaleChange,
            onRtlChange = onRtlChange,
            onLocaleChange = onLocaleChange,
            onDismiss = { showTweaks = false },
            variants = if (onePerScreen && variants.size > 1) variants else emptyList(),
            selected = entry,
            onVariantChange = onVariantChange,
        )
    }
}

/** The one way into the tweaks, on every page; the dot says an override is off its default. */
@Composable
private fun TweaksFab(
    tweaked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        BadgedBox(badge = { if (tweaked) Badge() }) {
            Icon(Icons.Filled.Tune, contentDescription = stringResource(R.string.detail_tweaks))
        }
    }
}

/**
 * What the variants stand on: the surface of the theme they render in, light or dark as the overrides
 * say. Every sample wraps itself in `ScreenshotTheme`, whose `Surface` is sized to the sample, so a
 * stage in any other colour shows each one as a box. The page's own labels and hint cards on the stage
 * take the same theme, so they read against it; only the colours change, not the font scale or
 * direction, which stay the sample's own overrides.
 */
@Composable
private fun SampleStage(
    overrides: SampleOverrides,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BingeExpressiveTheme(darkTheme = overrides.dark, dynamicColor = false) {
        Surface(modifier) { content() }
    }
}

/**
 * The variant edge to edge with none of the app's chrome, since it is screen chrome itself: a top bar
 * only reads right at the real top of the window. Its own back button leaves, through [LocalDemoBack],
 * and the [fab] sits at the bottom end, clear of the system bars.
 */
@Composable
private fun FullScreenVariant(
    entry: CatalogEntry,
    overrides: SampleOverrides,
    onBack: (() -> Unit)?,
    fab: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        SampleStage(overrides, Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalDemoBack provides (onBack ?: {})) {
                key(entry.id) { WithOverrides(overrides, entry.content) }
            }
        }
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .safeDrawingPadding()
                .padding(dimensionResource(R.dimen.catalog_padding)),
        ) { fab() }
    }
}

/**
 * The variant in a box that fills the pane and centres, so a screen sized sample fills it and a content
 * sized one is centred. Switching variant starts the new one fresh, so a demo never inherits the last
 * one's state.
 *
 * The description floats over the top of the variant, so it reads as part of the page rather than a
 * band under the top bar, and dismisses for that variant once read.
 */
@Composable
private fun SingleVariant(
    entry: CatalogEntry,
    overrides: SampleOverrides,
    modifier: Modifier = Modifier,
) {
    var dismissedFor by rememberSaveable { mutableStateOf<String?>(null) }
    Box(modifier, contentAlignment = Alignment.Center) {
        key(entry.id) { WithOverrides(overrides, entry.content) }
        if (dismissedFor != entry.id) {
            VariantDescription(
                entry,
                onDismiss = { dismissedFor = entry.id },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = dimensionResource(R.dimen.catalog_padding_small)),
            )
        }
    }
}

/** Every variant under its name and description, opened at [selected], which a search may have picked. */
@Composable
private fun VariantList(
    variants: List<CatalogEntry>,
    selected: CatalogEntry,
    overrides: SampleOverrides,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = variants.indexOf(selected).coerceAtLeast(0))
    // The extra bottom padding lets the last variant scroll clear of the navigation bar and the tweaks button.
    val contentPadding = edgeToEdgeContentPadding(
        base = dimensionResource(DesR.dimen.zero),
        extraBottom = dimensionResource(R.dimen.catalog_fab_clearance),
    )
    LazyColumn(modifier, state = state, contentPadding = contentPadding) {
        items(variants, key = { it.id }) { variant ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.catalog_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_padding_small)),
            ) {
                SectionHeader(title = variant.name, horizontalPadding = dimensionResource(R.dimen.catalog_padding))
                VariantDescription(variant)
                // Inset like the page's other content, so a full-width variant's copy is not hard against the edge.
                Box(
                    Modifier.fillMaxWidth().padding(horizontal = dimensionResource(R.dimen.catalog_padding)),
                    contentAlignment = Alignment.Center,
                ) {
                    WithOverrides(overrides, variant.content)
                }
            }
            HorizontalDivider()
        }
    }
}

/** The variant's KDoc sentence, in the design system's own hint card; the top bar names the variant. */
@Composable
private fun VariantDescription(
    entry: CatalogEntry,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    if (entry.description.isBlank() || entry.selfDescribing) return
    HintCard(
        text = entry.description,
        icon = Icons.Outlined.Info,
        onDismiss = onDismiss,
        modifier = modifier.padding(horizontal = dimensionResource(R.dimen.catalog_padding)),
    )
}
