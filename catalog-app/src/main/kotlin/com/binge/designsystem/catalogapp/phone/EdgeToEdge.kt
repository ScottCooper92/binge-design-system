package com.binge.designsystem.catalogapp.phone

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.paneSideInsets

/**
 * Content padding for a scrolling list that runs edge to edge: [base] on every side, plus the bottom
 * system inset and the pane's side insets, so the list draws under the bars but its first and last
 * items scroll clear of them. The sides follow the design system's pane rule: beside another pane, the
 * shared edge has no bar or cutout to clear. [extraBottom] makes room for anything floating over the
 * end of the list.
 */
@Composable
internal fun edgeToEdgeContentPadding(base: Dp, extraBottom: Dp = Dp(0f)): PaddingValues {
    val bottom = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues()
    val sides = paneSideInsets().asPaddingValues()
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = base + sides.calculateStartPadding(direction),
        top = base,
        end = base + sides.calculateEndPadding(direction),
        bottom = base + bottom.calculateBottomPadding() + extraBottom,
    )
}
