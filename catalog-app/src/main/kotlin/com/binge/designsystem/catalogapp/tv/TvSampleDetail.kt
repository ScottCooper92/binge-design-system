package com.binge.designsystem.catalogapp.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalogapp.R
import com.binge.designsystem.catalogapp.overrides.FontScalePresets
import com.binge.designsystem.catalogapp.overrides.SampleOverrides
import com.binge.designsystem.catalogapp.overrides.WithOverrides
import com.binge.designsystem.catalogapp.registry.CatalogEntry
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.focus.TvOverlayArrivalFocusEffect
import com.binge.designsystem.tv.focus.tvExitFocusGroup
import com.binge.designsystem.tv.theme.TvButtonStyle
import com.binge.designsystem.tv.R as TvR

/**
 * One sample full screen, laid over the list. It traps focus, so the D-pad cannot reach the list
 * beneath, and takes first focus on open as a user-invoked overlay should. Back is the first control.
 */
@Composable
fun TvSampleDetail(
    entry: CatalogEntry,
    overrides: SampleOverrides,
    onFontScaleChange: (Float) -> Unit,
    onRtlChange: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    val firstControl = remember { FocusRequester() }
    TvOverlayArrivalFocusEffect(target = firstControl, key = entry.id)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .tvExitFocusGroup()
            .padding(
                horizontal = dimensionResource(TvR.dimen.tv_overscan_horizontal),
                vertical = dimensionResource(TvR.dimen.tv_overscan_vertical),
            ),
    ) {
        Text(text = entry.name, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Row(
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.catalog_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.catalog_controls_gap)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TvButton(
                label = stringResource(R.string.tv_detail_back),
                onClick = onBack,
                modifier = Modifier.focusRequester(firstControl),
            )
            FontScalePresets.forEach { scale ->
                TvButton(
                    label = stringResource(R.string.control_font_scale_option, scale.toString()),
                    onClick = { onFontScaleChange(scale) },
                    style = if (overrides.fontScale == scale) TvButtonStyle.Primary else TvButtonStyle.Secondary,
                )
            }
            TvButton(
                label = stringResource(R.string.control_rtl),
                onClick = { onRtlChange(!overrides.rtl) },
                style = if (overrides.rtl) TvButtonStyle.Primary else TvButtonStyle.Secondary,
            )
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WithOverrides(overrides, entry.content)
        }
    }
}
