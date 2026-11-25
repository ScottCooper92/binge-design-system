@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeSheetPageHeader
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * The header above a stacked sheet page, three ways: Back and a title, Back with the name of the page it returns to, and a long title that
 * ellipsizes. Tap Back and the count under the first goes up.
 */
@Composable
fun BingeSheetPageHeaderSample() {
    var backs by remember { mutableIntStateOf(0) }
    ScreenshotTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(vertical = dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            BingeSheetPageHeader(onBack = { backs++ }, title = "Seasons")
            Text(
                text = "Back tapped $backs times",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
            )
            BingeSheetPageHeader(onBack = {}, backLabel = "Request")
            BingeSheetPageHeader(onBack = {}, title = "The Lord of the Rings: The Fellowship of the Ring, Extended Edition")
        }
    }
}
