@file:OnePerScreen(fullScreen = true)
@file:CatalogGroup("Detail pages")

package com.binge.designsystem.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.component.BingeBarIconAction
import com.binge.designsystem.component.DetailOverviewSection
import com.binge.designsystem.component.DetailStatsRows
import com.binge.designsystem.preview.ScreenshotTheme
import com.binge.designsystem.template.MediaHeroDetailPage
import com.binge.designsystem.template.MediaWideHeader

/**
 * A film's page at a phone's width: the hero full-bleed with the title, the genres and the tagline, a share action in the bar,
 * and the stats and the synopsis in the body. Without a [MediaWideHeader] the single-column hero stays at every width, so the
 * tablet frame shows it too.
 */
@Composable
fun MediaHeroDetailPageSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MediaHeroDetailPage(
            title = MEDIA_PAGE_TITLE,
            backdropUrl = null,
            metaText = "",
            onBack = LocalDemoBack.current,
            genres = MEDIA_PAGE_GENRES,
            tagline = MEDIA_PAGE_TAGLINE,
            topBarActions = { glassBackgroundAlpha ->
                BingeBarIconAction(Icons.Filled.Share, "Share", onClick = {}, glassBackgroundAlpha = glassBackgroundAlpha)
            },
        ) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
            // The sections sit in a column that spaces them, so the stat rows, which are one row each, are spaced too.
            Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_m))) {
                DetailStatsRows(MediaPageStats)
                DetailOverviewSection(heading = "Overview", text = MEDIA_PAGE_OVERVIEW)
            }
        }
    }
}

/**
 * The same page with a [MediaWideHeader]: at the expanded width the cinematic header takes the hero's place and carries the
 * poster, the synopsis and the stats, so the body keeps only what the header does not. Below the expanded width the
 * single-column hero stays, as on a phone.
 */
@Composable
fun MediaHeroDetailPageWideSample() {
    ScreenshotTheme(modifier = Modifier.fillMaxSize()) {
        MediaHeroDetailPage(
            title = MEDIA_PAGE_TITLE,
            backdropUrl = null,
            metaText = "",
            onBack = LocalDemoBack.current,
            genres = MEDIA_PAGE_GENRES,
            tagline = MEDIA_PAGE_TAGLINE,
            wideHeader = MediaWideHeader(posterUrl = null, synopsis = MEDIA_PAGE_OVERVIEW, stats = MediaPageStats),
            topBarActions = { glassBackgroundAlpha ->
                BingeBarIconAction(Icons.Filled.Share, "Share", onClick = {}, glassBackgroundAlpha = glassBackgroundAlpha)
            },
        ) {
            Spacer(Modifier.height(dimensionResource(R.dimen.padding_m)))
            SampleCopy()
        }
    }
}
