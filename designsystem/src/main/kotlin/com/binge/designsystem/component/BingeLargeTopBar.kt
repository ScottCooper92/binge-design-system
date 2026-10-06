package com.binge.designsystem.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R

/**
 * A large collapsing bar for a screen whose heading carries more than its title: a [subtitle] line under it
 * (a summary of what the screen shows) and a [titleTrailing] control at the end of the title row. Both show
 * only while the bar is expanded; a control that should take over once it collapses goes in [actions], faded
 * on the [scrollBehavior]'s collapsed fraction. Otherwise it is [BingePaneTopBar]'s two-row form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeLargeTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    containerColor: Color = Color.Unspecified,
    scrimFraction: Float = 0f,
    foregroundScrimFraction: Float = scrimFraction,
    scrimColor: Color = MaterialTheme.colorScheme.background,
    scrimForegroundColor: Color = MaterialTheme.colorScheme.onBackground,
    titleTrailing: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.(glassBackgroundAlpha: Float) -> Unit = {},
) {
    TwoRowTopBar(
        title = title,
        modifier = modifier,
        onBack = onBack,
        scrollBehavior = scrollBehavior,
        containerColor = containerColor,
        scrimFraction = scrimFraction,
        foregroundScrimFraction = foregroundScrimFraction,
        scrimColor = scrimColor,
        scrimForegroundColor = scrimForegroundColor,
        actions = actions,
        expandedHeight = dimensionResource(R.dimen.large_top_bar_expanded_height),
        subtitle = subtitle,
        titleTrailing = titleTrailing,
    )
}
