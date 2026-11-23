@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.preview.TaskSheetPreviewFrame

/**
 * A short task-sheet page under [TaskSheetTopClearance]. The foldable cell is a side sheet, where the page keeps the
 * `padding_m` gap above its title; the phone cell is a bottom sheet, whose drag handle stands there, and adds none.
 */
@Composable
fun TaskSheetTopClearanceSample() {
    TaskSheetPreviewFrame {
        Text(
            text = "Edit request",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.padding_m)),
        )
    }
}
