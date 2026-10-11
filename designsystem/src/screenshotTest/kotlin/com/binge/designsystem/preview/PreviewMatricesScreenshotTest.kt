package com.binge.designsystem.preview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.android.tools.screenshot.PreviewTest
import com.binge.designsystem.R

/** One frame per shared window, so each matrix is rendered once and its cells cannot drift unseen. */
class PreviewMatricesScreenshotTest {
    @PreviewTest
    @ListPanePreview
    @Composable
    fun ListPane() = Window("pane: the foldable's window, for a screen inside a pane")

    @PreviewTest
    @LandscapePanesPreview
    @Composable
    fun LandscapePanes() = Window("phone-land: the narrowest window that puts two panes side by side")

    @PreviewTest
    @TallComponentPreviews
    @Composable
    fun TallComponent() = Window("tall component: 1600dp high, so a long section is not clipped")

    @Composable
    private fun Window(label: String) {
        ScreenshotTheme(Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_l))) {
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
