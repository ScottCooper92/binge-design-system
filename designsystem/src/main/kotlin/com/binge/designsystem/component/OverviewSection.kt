package com.binge.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.layout.LayoutAnchors
import com.binge.designsystem.layout.layoutAnchor

/**
 * The [heading] over a title's synopsis, and the synopsis in an [ExpandableOverview] under it, as Binge's detail pages have it. Draws
 * nothing where [overview] is null. It carries the [LayoutAnchors.Detail.OVERVIEW] anchor a page's skeleton is checked against.
 *
 * @need show a title's synopsis under a heading, collapsed with a toggle
 * @group detail
 * @replaces a hand-built heading over an ExpandableOverview
 * @sample OverviewSectionSample
 */
@Composable
fun OverviewSection(
    heading: String,
    overview: String?,
    modifier: Modifier = Modifier,
) {
    if (overview == null) return
    Column(
        modifier
            .padding(horizontal = dimensionResource(R.dimen.padding_m))
            .layoutAnchor(LayoutAnchors.section(LayoutAnchors.Detail.OVERVIEW)),
    ) {
        Text(heading, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(dimensionResource(R.dimen.detail_meta_spacing)))
        ExpandableOverview(text = overview)
    }
}
