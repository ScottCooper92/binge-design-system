@file:OnePerScreen(fullScreen = true)
@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.HeroDetailScreen

/**
 * A detail page being refreshed by a pull: the spinner rests below the overlay bar, over the hero, and the page stays.
 * A still frame cannot pull, so it is given the state a refresh rests at.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDetailScreenRefreshingSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailScreen(
            title = "A title",
            onBack = LocalDemoBack.current,
            onRefresh = {},
            isRefreshing = true,
            pullState = RestingAtThreshold,
            hero = {
                DetailHero(
                    title = "A title",
                    backdropUrl = null,
                    tagline = "The line a poster would carry.",
                    metaText = "2026 · 1h 52m",
                    onBack = LocalDemoBack.current,
                    showChrome = false,
                )
            },
        ) {
            SampleCopy()
        }
    }
}
