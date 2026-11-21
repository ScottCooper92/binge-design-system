@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BottomBarScrim
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.theme.BingeShapes

private const val SCRIM_SAMPLE_ROWS = 30

/**
 * Public sample for [BottomBarScrim]: rows scrolling under a floating element at the bottom (a composer, here a plain pill). The scrim is the
 * first child of the box the element sits in, so the rows fade out above the element and behind it instead of running sharp to the edge. A screen
 * whose content only sometimes runs under the element passes `fraction` to bring the scrim in once it does.
 */
@Composable
fun BottomBarScrimSample() {
    ScreenshotTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.catalog_bottom_bar_scrim_height)),
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items((1..SCRIM_SAMPLE_ROWS).toList()) { row ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimensionResource(R.dimen.padding_m), vertical = dimensionResource(R.dimen.padding_xs))
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                BingeShapes.ElementSmall,
                            ).padding(dimensionResource(R.dimen.padding_m)),
                    ) {
                        Text(
                            text = "Row $row",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                BottomBarScrim()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.padding_m))
                        .height(dimensionResource(R.dimen.catalog_bottom_bar_scrim_bar_height))
                        .background(
                            MaterialTheme.colorScheme.surfaceContainerHigh,
                            BingeShapes.Medium,
                        ),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = "Add a comment",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)),
                    )
                }
            }
        }
    }
}
