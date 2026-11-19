@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.R
import com.binge.designsystem.component.OverviewSkeleton
import com.binge.designsystem.component.StatsRowsSkeleton
import com.binge.designsystem.component.TitleHeroSkeleton
import com.binge.designsystem.isExpandedLayout
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.HeroDetailScreen
import com.binge.designsystem.template.readingMargin

/**
 * The page of [MediaHeroDetailPageSample] while it loads, on the same [HeroDetailScreen] frame: the bar with its back control is there
 * from the first frame, and the hero, the stat rows and the overview are plates at the height the loaded page gives them, so the page
 * does not jump when it resolves. At the expanded width the header carries the stats and the synopsis, so only the hero is held.
 */
@Composable
fun MediaPageSkeletonSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        val cinematic = isExpandedLayout()
        HeroDetailScreen(
            title = "",
            onBack = LocalDemoBack.current,
            heroHeight = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_height else R.dimen.detail_hero_height),
            horizontalInset = dimensionResource(if (cinematic) R.dimen.detail_cinematic_header_padding else R.dimen.padding_m),
            contentMaxWidth = Dp.Infinity,
            darkStatusBar = false,
            hero = { TitleHeroSkeleton(cinematic) },
        ) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
            Column(Modifier.clearAndSetSemantics {}, verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                if (!cinematic) {
                    StatsRowsSkeleton(
                        Modifier.layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.STATS)),
                        statCount = MediaPageStats.size,
                    )
                    OverviewSkeleton(Modifier.readingMargin().layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW)))
                }
            }
        }
    }
}
