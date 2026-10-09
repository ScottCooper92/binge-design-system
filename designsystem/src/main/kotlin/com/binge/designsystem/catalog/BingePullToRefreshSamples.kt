@file:OnePerScreen(fullScreen = true)
@file:ScreenshotOnly

package com.binge.designsystem.catalog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.binge.designsystem.component.BingePullToRefresh

/**
 * A list refreshing under a transparent small bar: the spinner rests below the bar, given the scaffold's top padding,
 * not behind it. A still frame cannot pull, so [BingePullToRefreshDemo] stands in for this on a device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingePullToRefreshSample() {
    PullToRefreshScreen(refreshing = true, onRefresh = {}, state = remember { RestingAtThreshold })
}

/**
 * Where [BingePullToRefresh] rests while it refreshes. Its own state animates there from a coroutine, which a still
 * frame never runs, so the frame would show no spinner at all.
 */
@OptIn(ExperimentalMaterial3Api::class)
private object RestingAtThreshold : PullToRefreshState {
    override val distanceFraction: Float = 1f
    override val isAnimating: Boolean = false

    override suspend fun animateToThreshold() = Unit

    override suspend fun animateToHidden() = Unit

    override suspend fun snapTo(targetValue: Float) = Unit
}
