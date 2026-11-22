@file:OnePerScreen
@file:SelfDescribing

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.SettingsPageSkeleton
import com.binge.designsystem.preview.ScreenshotTheme

/**
 * [SettingsPageSkeleton] standing in for a settings page that opens with an intro: the sentence is real text, and five
 * placeholders follow, each as tall as a settings row. The same frame at the large fonts shows the rows growing with the text.
 */
@Composable
fun SettingsPageSkeletonSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        SettingsPageSkeleton(
            contentPadding = PaddingValues(),
            rows = SAMPLE_ROWS,
            intro = "Choose how this works. Changes apply as you make them.",
            description = "Loading",
        )
    }
}

/** [SettingsPageSkeleton] for a page that opens straight on its first group: no intro, rows of a label alone. */
@Composable
fun SettingsPageSkeletonPlainSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        SettingsPageSkeleton(contentPadding = PaddingValues(), rows = SAMPLE_ROWS, formInset = true, rowDetail = false)
    }
}

private const val SAMPLE_ROWS = 5
