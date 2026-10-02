package com.binge.designsystem.catalogapp.phone

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.LocalIsSinglePaneNav
import com.binge.designsystem.PaneContent
import com.binge.designsystem.PaneEdge
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleLocale
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.CatalogRegistry
import com.binge.designsystem.catalogapp.registry.variantsOf
import com.binge.designsystem.component.HintCard
import com.binge.designsystem.isExpandedLayout

/**
 * Two levels, so no navigation library: the grid of components, and one component's page showing a
 * variant full screen. On a phone they take turns; from the expanded width up they sit side by side
 * ([CatalogListDetail]). The selection is a variant, so the page knows its component from it. The
 * selection, the search, the grid's scroll position and the overrides are saveable and so survive
 * rotation. The grid's state lives here rather than in the grid, which leaves composition while a
 * component is open, so coming back lands where you left. The overrides also stay put as you move
 * between variants, which is what makes comparing them quick.
 */
@Composable
fun CatalogApp() {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val systemDark = isSystemInDarkTheme()
    var dark by rememberSaveable { mutableStateOf(systemDark) }
    var fontScale by rememberSaveable { mutableFloatStateOf(FontScalePresets.first()) }
    var rtl by rememberSaveable { mutableStateOf(false) }
    var locale by rememberSaveable { mutableStateOf(SampleLocale.System) }
    val gridState = rememberLazyGridState()

    val selected = CatalogRegistry.firstOrNull { it.id == selectedId }
    BackHandler(enabled = selected != null) { selectedId = null }

    val list: @Composable () -> Unit = {
        CatalogList(
            entries = CatalogRegistry,
            query = query,
            onQueryChange = { query = it },
            onSelect = { selectedId = it.id },
            gridState = gridState,
            selectedGroup = selected?.group,
        )
    }
    val detail: @Composable (CatalogEntry, (() -> Unit)?) -> Unit = { entry, onBack ->
        SampleDetail(
            entry = entry,
            variants = CatalogRegistry.variantsOf(entry.group),
            onVariantChange = { selectedId = it.id },
            overrides = SampleOverrides(dark, fontScale, rtl, locale),
            onDarkChange = { dark = it },
            onFontScaleChange = { fontScale = it },
            onRtlChange = { rtl = it },
            onLocaleChange = { locale = it },
            onBack = onBack,
        )
    }

    if (isExpandedLayout()) {
        CatalogListDetail(list = list, detail = selected?.let { entry -> { detail(entry, null) } })
    } else if (selected == null) {
        list()
    } else {
        detail(selected) { selectedId = null }
    }
}

/**
 * The list-detail layout for a tablet or an unfolded foldable: the grid in a fixed-width pane, the
 * open component beside it, or a hint until one is picked. Each side is a [PaneContent], so the design
 * system's components inside read the pane's own width and the edge it shares with the other pane.
 */
@Composable
private fun CatalogListDetail(list: @Composable () -> Unit, detail: (@Composable () -> Unit)?) {
    CompositionLocalProvider(LocalIsSinglePaneNav provides false) {
        Row(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            PaneContent(
                innerEdge = PaneEdge.End,
                modifier = Modifier.width(dimensionResource(R.dimen.catalog_list_pane_width)).fillMaxHeight(),
            ) {
                list()
            }
            VerticalDivider()
            PaneContent(innerEdge = PaneEdge.Start, modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (detail != null) {
                    detail()
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        HintCard(
                            text = stringResource(R.string.catalog_pick_component),
                            icon = Icons.Filled.TouchApp,
                            modifier = Modifier.padding(dimensionResource(R.dimen.catalog_padding)),
                        )
                    }
                }
            }
        }
    }
}
