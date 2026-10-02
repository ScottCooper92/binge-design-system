package com.binge.designsystem.catalogapp.tv

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.tv.material3.MaterialTheme
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.TvCatalogRegistry
import com.binge.designsystem.tv.focus.TvArrivalFocusEffect
import com.binge.designsystem.tv.focus.rememberTvArrivalFocus
import com.binge.designsystem.tv.focus.rememberTvOverlayCloser

/**
 * The list stays composed beneath the open sample, which is what lets focus return to the row that
 * opened it: the sample is an overlay, closed through the design system's overlay closer rather than
 * by a bare `requestFocus()`. The theme is dark-only on TV, so only font scale and RTL are offered.
 */
@Composable
fun TvCatalogApp(entries: List<CatalogEntry> = TvCatalogRegistry) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var fontScale by rememberSaveable { mutableFloatStateOf(FontScalePresets.first()) }
    var rtl by rememberSaveable { mutableStateOf(false) }
    val rowRequesters = remember { TvRowRequesters() }
    val arrival = rememberTvArrivalFocus()
    val startedOnList = remember { selectedId == null }
    val selected = entries.firstOrNull { it.id == selectedId }
    val closer = rememberTvOverlayCloser(
        restoreTo = selected?.let { rowRequesters[it.id] } ?: arrival.requester,
        onClose = { selectedId = null },
    )

    TvArrivalFocusEffect(arrival, enabled = startedOnList)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TvCatalogList(
            entries = entries,
            rowRequesters = rowRequesters,
            arrival = arrival,
            onOpen = { selectedId = it.id },
        )
        if (selected != null) {
            BackHandler { closer.close() }
            TvSampleDetail(
                entry = selected,
                overrides = SampleOverrides(dark = true, fontScale = fontScale, rtl = rtl),
                onFontScaleChange = { fontScale = it },
                onRtlChange = { rtl = it },
                onBack = closer::close,
            )
        }
    }
}
