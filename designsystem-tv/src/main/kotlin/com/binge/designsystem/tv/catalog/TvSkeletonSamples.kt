@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.template.TvDetailPageSkeleton
import com.binge.designsystem.tv.template.TvImmersiveHubSkeleton

/** An immersive hub while its rows load: the text band and two rows of poster plates, where the loaded hub puts them. */
@Composable
fun TvImmersiveHubSkeletonSample() {
    TvImmersiveHubSkeleton()
}

/** A detail page while it loads: the poster, the copy lines, the action pills and one row of cards. */
@Composable
fun TvDetailPageSkeletonSample() {
    TvDetailPageSkeleton()
}
