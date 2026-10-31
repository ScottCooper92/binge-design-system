package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.tv.component.LocalTvSideSheetPage
import com.binge.designsystem.tv.component.TvSideSheetBack
import com.binge.designsystem.tv.component.TvSideSheetPage
import com.binge.designsystem.tv.component.TvSideSheetPanel
import com.binge.designsystem.tv.component.TvSideSheetRow
import com.binge.designsystem.tv.component.TvSideSheetRows
import com.binge.designsystem.tv.component.tvSideSheetWash
import com.binge.designsystem.R as DesR

private val SampleWash = Color(0xFF2E7D6B)

/**
 * A page opened over another whose header washes the sheet: the app bar's back above it, the header block on a wash of its
 * own colour that runs the panel's width from the top of the screen and fades into the panel, and the rows below on the
 * panel's own colour.
 */
@Composable
fun TvSideSheetWashSample() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
        TvSideSheetPanel {
            CompositionLocalProvider(LocalTvSideSheetPage provides TvSideSheetPage(parentTitle = "Companions", onTitle = {})) {
                TvSideSheetBack()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvSideSheetWash(
                            SampleWash,
                        ).padding(vertical = dimensionResource(DesR.dimen.padding_m)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(DesR.dimen.padding_xs)),
                ) {
                    Text("Home Server", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
                    Text("Ready", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                }
                TvSideSheetRows {
                    TvSideSheetRow(label = "Request", onClick = {}, initiallyFocused = true)
                    TvSideSheetRow(label = "Open in Home Server", onClick = {})
                }
            }
        }
    }
}
