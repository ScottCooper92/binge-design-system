@file:OnePerScreen(fullScreen = true)
@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.DetailHero
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.HeroDetailScreen
import com.binge.designsystem.template.HeroPullIndicator

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

/**
 * A pulled page with no image under the bar, as a person's is: the hero is only the clearance below the bar, and an eyebrow
 * and the name start the content. The spinner rests in the bar's row, clear of the name; [HeroPullIndicator.BelowBar] would land it on the
 * name's line.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeroDetailScreenNoImageRefreshingSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        HeroDetailScreen(
            title = "Joe Performer",
            onBack = LocalDemoBack.current,
            onRefresh = {},
            isRefreshing = true,
            pullState = RestingAtThreshold,
            pullIndicator = HeroPullIndicator.InBar,
            hero = {
                Spacer(
                    Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .height(dimensionResource(R.dimen.person_profile_top_bar_offset)),
                )
            },
        ) {
            Column(
                Modifier.padding(horizontal = dimensionResource(R.dimen.padding_m)).padding(top = dimensionResource(R.dimen.padding_s)),
            ) {
                Text(
                    text = "ACTING",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(dimensionResource(R.dimen.padding_xs)))
                Text(text = "Joe Performer", style = MaterialTheme.typography.headlineMedium)
            }
            SampleCopy()
        }
    }
}
