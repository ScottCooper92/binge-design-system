@file:CatalogGroup("Text")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.WholeWordText
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * [WholeWordText] in a narrow column. The first line is made of short words, so it wraps at its spaces. The second holds a word
 * wider than the column, so it stays on one line and ends in an ellipsis, where `Text` would break the word across lines.
 */
@Composable
fun WholeWordTextSample() {
    ScreenshotTheme {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.padding_s)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_s)),
        ) {
            WholeWordText(
                text = "Living room server name",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(dimensionResource(R.dimen.whole_word_sample_width)),
            )
            WholeWordText(
                text = "Fitzgerald-Montgomery Alexandria",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(dimensionResource(R.dimen.whole_word_sample_width)),
            )
        }
    }
}
