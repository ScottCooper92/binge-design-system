package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleLocale
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.catalogapp.registry.CatalogKind
import com.binge.designsystem.component.BingeBottomSheet
import com.binge.designsystem.component.BingeFilterChip
import com.binge.designsystem.component.BingeModalSideSheet
import com.binge.designsystem.component.SectionHeader
import com.binge.designsystem.R as DesR

/**
 * The overrides, and for a one-per-screen component its [variants], in the design system's own sheets: a bottom sheet on a phone, a side sheet from
 * [R.bool.catalog_tweaks_side_sheet] up, where the sample stays in view beside it. Each change applies
 * at once, so the sample behind the scrim updates as you tap.
 */
@Composable
fun TweaksSheet(
    overrides: SampleOverrides,
    onDarkChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onRtlChange: (Boolean) -> Unit,
    onLocaleChange: (SampleLocale) -> Unit,
    onDismiss: () -> Unit,
    variants: List<CatalogEntry> = emptyList(),
    selected: CatalogEntry? = null,
    onVariantChange: (CatalogEntry) -> Unit = {},
) {
    val controls: @Composable () -> Unit = {
        TweakControls(overrides, onDarkChange, onFontScaleChange, onRtlChange, onLocaleChange, variants, selected, onVariantChange)
    }
    if (booleanResource(R.bool.catalog_tweaks_side_sheet)) {
        BingeModalSideSheet(onDismissRequest = onDismiss) { controls() }
    } else {
        BingeBottomSheet(onDismissRequest = onDismiss) { controls() }
    }
}

@Composable
private fun TweakControls(
    overrides: SampleOverrides,
    onDarkChange: (Boolean) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onRtlChange: (Boolean) -> Unit,
    onLocaleChange: (SampleLocale) -> Unit,
    variants: List<CatalogEntry>,
    selected: CatalogEntry?,
    onVariantChange: (CatalogEntry) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(dimensionResource(R.dimen.catalog_padding)),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_padding_small)),
    ) {
        Text(stringResource(R.string.detail_tweaks), style = MaterialTheme.typography.titleLarge)
        if (variants.isNotEmpty()) {
            TweakSection(stringResource(R.string.control_variant)) {
                variants.forEach { variant ->
                    BingeFilterChip(
                        label = variant.name,
                        selected = variant.id == selected?.id,
                        onClick = { onVariantChange(variant) },
                        leadingIcon = if (variant.kind == CatalogKind.Demo) Icons.Filled.PlayCircle else null,
                    )
                }
            }
        }
        TweakSection(stringResource(R.string.control_appearance)) {
            BingeFilterChip(
                label = stringResource(R.string.control_dark),
                selected = overrides.dark,
                onClick = { onDarkChange(!overrides.dark) },
            )
            BingeFilterChip(
                label = stringResource(R.string.control_rtl),
                selected = overrides.rtl,
                onClick = { onRtlChange(!overrides.rtl) },
            )
        }
        TweakSection(stringResource(R.string.control_font_scale)) {
            FontScalePresets.forEach { scale ->
                BingeFilterChip(
                    label = stringResource(R.string.control_font_scale_option, scale.toString()),
                    selected = overrides.fontScale == scale,
                    onClick = { onFontScaleChange(scale) },
                )
            }
        }
        TweakSection(stringResource(R.string.control_locale)) {
            SampleLocale.entries.forEach { locale ->
                BingeFilterChip(
                    label = stringResource(locale.labelRes()),
                    selected = overrides.locale == locale,
                    onClick = { onLocaleChange(locale) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TweakSection(title: String, chips: @Composable () -> Unit) {
    SectionHeader(title = title, horizontalPadding = dimensionResource(DesR.dimen.zero))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_controls_gap))) { chips() }
}
