package com.binge.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/** How far a page slides as it arrives or leaves, as one part in this many of its width: a short shift that reads as direction, not travel. */
private const val SHEET_PAGE_SLIDE_DIVISOR = 8

/** The page a sheet shows and how deep the stack is, which is all a transition needs to know which way it is going. */
@Stable
internal data class SheetPageState(
    val page: String,
    val depth: Int,
)

/**
 * Swaps a task sheet's page with a transition (#1272): a pushed page slides in from the end edge as the old one slides a little the
 * other way, a popped one the reverse, mirrored in a right-to-left layout, and both cross-fade. The slide and the height follow the
 * theme's spatial spring. Under reduced motion nothing slides and the swap is a short cross-fade.
 *
 * [content] draws the page for the state it is given, so the page that is leaving is still drawn as itself while it goes. [onSettled]
 * runs each time a transition has finished and the leaving page is gone, which is when the owner of the stack lets go of a page
 * that was popped: dropping its state sooner would pull it out from under a page still on screen.
 */
@Composable
internal fun AnimatedSheetPages(
    state: SheetPageState,
    modifier: Modifier = Modifier,
    onSettled: () -> Unit = {},
    content: @Composable (SheetPageState) -> Unit,
) {
    val motion = rememberSheetMotion()
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    val transition = updateTransition(state, label = "sheet-pages")
    val latestSettled by rememberUpdatedState(onSettled)
    LaunchedEffect(transition.currentState, transition.targetState) {
        if (transition.currentState == transition.targetState) latestSettled()
    }
    transition.AnimatedContent(
        modifier = modifier,
        transitionSpec = {
            val forward = targetState.depth >= initialState.depth
            val fadeIn = fadeIn(motion.fade())
            val fadeOut = fadeOut(motion.fade())
            if (motion.reduceMotion) {
                ContentTransform(fadeIn, fadeOut, sizeTransform = null)
            } else {
                // The page that is arriving draws above the one it replaces on a push, and below the one it reveals on a pop.
                val enterFrom = if (forward) direction else -direction
                ContentTransform(
                    targetContentEnter = slideInHorizontally(motion.spatial()) { enterFrom * it / SHEET_PAGE_SLIDE_DIVISOR } + fadeIn,
                    initialContentExit = slideOutHorizontally(motion.spatial()) { -enterFrom * it / SHEET_PAGE_SLIDE_DIVISOR } + fadeOut,
                    targetContentZIndex = if (forward) 1f else -1f,
                    sizeTransform = SizeTransform { _, _ -> motion.spatial() },
                )
            }
        },
        contentKey = { it.page },
        content = { shown -> content(shown) },
    )
}
