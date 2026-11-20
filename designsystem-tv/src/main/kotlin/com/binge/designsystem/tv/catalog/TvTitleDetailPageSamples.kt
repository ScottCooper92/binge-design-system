@file:SelfDescribing

package com.binge.designsystem.tv.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.binge.designsystem.catalog.SelfDescribing
import com.binge.designsystem.tv.R
import com.binge.designsystem.tv.component.TvButton
import com.binge.designsystem.tv.component.TvIconButton
import com.binge.designsystem.tv.template.TV_DETAIL_LOADING_TITLE
import com.binge.designsystem.tv.template.TvDetailIconPlate
import com.binge.designsystem.tv.template.TvDetailLoading
import com.binge.designsystem.tv.template.TvDetailTab
import com.binge.designsystem.tv.template.TvDetailTitle
import com.binge.designsystem.tv.template.TvTitleDetailPage
import com.binge.designsystem.tv.template.tvDetailLoadingTabs

private val DetailSampleTitle = TvDetailTitle(
    title = "The Long Way Home",
    logoUrl = null,
    backdropUrl = null,
    overview = "A washed-up bounty hunter takes one last job, and the job takes the rest of what he had left to lose. " +
        "Pursued across three countries, he finds the only way out is back through everyone he left behind.",
    genres = listOf("Drama", "Thriller"),
    facts = listOf("2024", "2h 11m"),
    rating = 8.1f,
    certification = "12A",
)

private const val DETAILS = "details"
private const val CAST = "cast"
private const val VIDEOS = "videos"

@Composable
private fun TabBody(text: String) {
    Box(Modifier.fillMaxSize().padding(dimensionResource(R.dimen.tv_sample_content_inset))) {
        Text(text = text, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun SamplePage(
    initialExpanded: Boolean,
    initialTab: String? = null,
    initialFocusedTab: String? = null,
    loading: Boolean = false,
) {
    val actionsFocus = remember { FocusRequester() }
    val tabs = if (loading) {
        tvDetailLoadingTabs(DETAILS to "Details", CAST to "Cast", VIDEOS to "Videos")
    } else {
        listOf(
            TvDetailTab(DETAILS, "Details") { TabBody("The Details tab's own content goes here.") },
            TvDetailTab(CAST, "Cast") { TabBody("The Cast tab's own content goes here.") },
            TvDetailTab(VIDEOS, "Videos") { TabBody("The Videos tab's own content goes here.") },
        )
    }
    TvTitleDetailPage(
        title = if (loading) TV_DETAIL_LOADING_TITLE else DetailSampleTitle,
        tabs = tabs,
        actionsFocus = actionsFocus,
        hasActions = true,
        initialExpanded = initialExpanded,
        initialTabKey = initialTab,
        initialFocusedTabKey = initialFocusedTab,
        loading = TvDetailLoading.takeIf { loading },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.tv_media_row_card_gap)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // While loading, the primary is the real button drawn busy, so the node the user is on is the node that becomes the real
            // one; the secondary is a plate until its button replaces it.
            TvButton(
                label = "Play trailer",
                onClick = {},
                leadingIcon = Icons.Filled.PlayArrow,
                loading = loading,
                modifier = Modifier.focusRequester(actionsFocus),
            )
            if (loading) {
                TvDetailIconPlate(compact = false)
            } else {
                TvIconButton(icon = Icons.Filled.StarBorder, label = "Rate", onClick = {})
            }
        }
    }
}

/** A title's page at rest: the copy bottom-left over the backdrop, the action row, and the tabs along the foot. */
@Composable
fun TvTitleDetailPageSample() {
    SamplePage(initialExpanded = false)
}

/** The page expanded onto a tab: the tabs move to the top over the tab's content, and Up or Back folds it. */
@Composable
fun TvTitleDetailPageExpandedSample() {
    SamplePage(initialExpanded = true, initialTab = CAST)
}

/** The page while its title loads: the copy as plates, the primary busy and the other action a plate, and the tabs the loaded page reuses. */
@Composable
fun TvTitleDetailPageLoadingSample() {
    SamplePage(initialExpanded = false, loading = true)
}

/** The page expanded onto a tab with another tab focused, as the tab strip draws it: the focused tab filled, the open tab tinted. */
@Composable
fun TvTitleDetailPageTabFocusedSample() {
    SamplePage(initialExpanded = true, initialTab = CAST, initialFocusedTab = VIDEOS)
}
