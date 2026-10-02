package com.binge.designsystem.catalogapp

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.catalogapp.registry.CatalogEntry

/**
 * One sample, full screen. The sample sits in a box that fills the pane and centres, so a screen
 * sized sample fills it and a content sized one is centred, with no per-sample configuration.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SampleDetail(
    entry: CatalogEntry,
    overrides: SampleOverrides,
    onDarkChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onRtlChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(entry.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.detail_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OverrideControls(overrides, onDarkChange, onFontScaleChange, onRtlChange)
            HorizontalDivider()
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                WithOverrides(overrides, entry.content)
            }
        }
    }
}

@Composable
private fun OverrideControls(
    overrides: SampleOverrides,
    onDarkChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onRtlChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.catalog_padding)),
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_controls_gap)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = overrides.dark,
            onClick = { onDarkChange(!overrides.dark) },
            label = { Text(stringResource(R.string.control_dark)) },
        )
        FilterChip(
            selected = overrides.rtl,
            onClick = { onRtlChange(!overrides.rtl) },
            label = { Text(stringResource(R.string.control_rtl)) },
        )
        Text(stringResource(R.string.control_font_scale))
        FontScalePresets.forEach { scale ->
            FilterChip(
                selected = overrides.fontScale == scale,
                onClick = { onFontScaleChange(scale) },
                label = { Text(stringResource(R.string.control_font_scale_option, scale.toString())) },
            )
        }
    }
}
