@file:CatalogGroup("Page templates")

package com.binge.designsystem.tv.catalog

import androidx.compose.runtime.Composable
import com.binge.designsystem.catalog.CatalogGroup
import com.binge.designsystem.tv.template.TvImmersiveHubSkeleton
import com.binge.designsystem.tv.template.TvPageHosting

/** An immersive hub while its rows load: the text band and two rows of poster plates, where the loaded hub puts them. */
@Composable
fun TvImmersiveHubSkeletonSample() {
    TvImmersiveHubSkeleton()
}

/** The same skeleton hosted as an overlay: the loaded hub lands focus on its first row at once, so the rows sit a row gap higher. */
@Composable
fun TvImmersiveHubSkeletonOverlaySample() {
    TvImmersiveHubSkeleton(hosting = TvPageHosting.Overlay)
}
