package com.binge.designsystem.template

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.binge.designsystem.component.BingeFastScrollbar

/**
 * Turns on the fast scrollbar a template draws over its own results grid, which an app cannot reach to overlay itself. Pass it as the template's
 * `fastScrollbar`; null, the default, draws none. [bubbleText] is handed to [BingeFastScrollbar] as it is, so the app owns the words.
 */
@Immutable
class FastScrollbarOptions(
    val bubbleText: ((firstVisibleIndex: Int) -> String?)? = null,
)

/**
 * Draws [grid] and, when [options] is set, a [BingeFastScrollbar] over it for [state], inset by [contentPadding]'s top and bottom so the track
 * runs neither under the bar nor under the navigation bar. With null [options] it draws [grid] alone, handed [modifier] as it is.
 */
@Composable
internal fun FastScrollbarOverlay(
    options: FastScrollbarOptions?,
    state: LazyGridState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    grid: @Composable (Modifier) -> Unit,
) {
    if (options == null) {
        grid(modifier)
        return
    }
    Box(modifier) {
        grid(Modifier.fillMaxSize())
        BingeFastScrollbar(
            state = state,
            bubbleText = options.bubbleText,
            modifier = Modifier.padding(top = contentPadding.calculateTopPadding(), bottom = contentPadding.calculateBottomPadding()),
        )
    }
}
