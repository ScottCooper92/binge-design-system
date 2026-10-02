package com.binge.designsystem.catalogapp.phone

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.registry.CatalogRegistry

/**
 * Two levels, so no navigation library: the list, and one sample full screen. The selection, the
 * search and the three overrides are saveable and so survive rotation; the overrides also stay put
 * as you move between samples, which is what makes comparing them quick.
 */
@Composable
fun CatalogApp() {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    val systemDark = isSystemInDarkTheme()
    var dark by rememberSaveable { mutableStateOf(systemDark) }
    var fontScale by rememberSaveable { mutableFloatStateOf(FontScalePresets.first()) }
    var rtl by rememberSaveable { mutableStateOf(false) }

    val selected = CatalogRegistry.firstOrNull { it.id == selectedId }
    BackHandler(enabled = selected != null) { selectedId = null }

    if (selected == null) {
        CatalogList(
            entries = CatalogRegistry,
            query = query,
            onQueryChange = { query = it },
            onSelect = { selectedId = it.id },
        )
    } else {
        SampleDetail(
            entry = selected,
            overrides = SampleOverrides(dark, fontScale, rtl),
            onDarkChange = { dark = it },
            onFontScaleChange = { fontScale = it },
            onRtlChange = { rtl = it },
            onBack = { selectedId = null },
        )
    }
}
