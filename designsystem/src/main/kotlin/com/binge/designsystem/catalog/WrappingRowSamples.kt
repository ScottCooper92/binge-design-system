@file:CatalogGroup("Tags and badges")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeTag
import com.binge.designsystem.component.WrappingRow
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * [WrappingRow] in a narrow column whose parent sizes itself from `IntrinsicSize.Min`, the way `ListRow`'s card does.
 * The tags wrap onto two lines and the line of text below them is still in view, which `FlowRow` in the same place
 * would clip.
 */
@Composable
fun WrappingRowSample() {
    ScreenshotTheme {
        // The box loosens the frame's tight constraints so the row can take its own, narrower width.
        Box {
            Row(
                Modifier
                    .width(
                        dimensionResource(R.dimen.wrapping_row_sample_width),
                    ).height(IntrinsicSize.Min)
                    .padding(dimensionResource(R.dimen.padding_s)),
            ) {
                Column(Modifier.fillMaxHeight()) {
                    WrappingRow {
                        BingeTag(label = "Movie", icon = Icons.Filled.Movie)
                        BingeTag(label = "Series", icon = Icons.Filled.Tv)
                        BingeTag(label = "4K")
                        BingeTag(label = "Anime")
                    }
                    Text(text = "Below the wrapped tags", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
