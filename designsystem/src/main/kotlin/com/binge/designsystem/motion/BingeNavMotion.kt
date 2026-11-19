package com.binge.designsystem.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.binge.designsystem.theme.LocalReduceMotion

/** How far the incoming screen of a forward navigation starts from its resting place, as a fraction of the width. */
const val NAV_FORWARD_ENTER_OFFSET_FRACTION = 5

/** How far the outgoing screen of a forward navigation travels, as a fraction of the width: a tenth, so it trails the new screen. */
const val NAV_FORWARD_EXIT_OFFSET_FRACTION = 10

/** The duration of a forward navigation's slide and fade. */
const val NAV_FORWARD_DURATION_MS = 280

/** The duration of a pop's fade. Short, so a popped screen is gone by the time the screen under it settles. */
const val NAV_POP_DURATION_MS = 90

/** The scale a popped screen shrinks to, for a pop that is a button press or a completed back gesture. */
const val NAV_POP_SCALE = 0.9f

/** The alpha a screen being dragged away by a predictive back gesture fades towards: low enough to read as leaving, high enough to peek. */
const val NAV_PREDICTIVE_POP_ALPHA = 0.9f

/** The scale of the lighter predictive pop for a drill-down inside a tab, where the screen under is the same page. */
const val NAV_TAB_PREDICTIVE_POP_SCALE = 0.96f

/** The alpha a drill-down inside a tab fades towards under a predictive back gesture. */
const val NAV_TAB_PREDICTIVE_POP_ALPHA = 0.96f

/**
 * The duration of a tab switch's cross-fade. It is Navigation 3's own default scene transition, which is what a tab switch gets in an app
 * that draws every tab's back stack in one navigation display, so an app that draws one tab at a time matches it.
 */
const val NAV_TAB_SWITCH_DURATION_MS = 700

/**
 * The five screen transitions apps built on this design system share, so a push, a pop and a predictive back gesture look the same in
 * each of them. They carry no data: hand them to a `NavDisplay` as `transitionSpec`, `popTransitionSpec` and `predictivePopTransitionSpec`.
 *
 * Why they look as they do:
 * - **Forward** slides the new screen in from a fifth of the width with a fade, over [NAV_FORWARD_DURATION_MS], while the old one slides out a
 *   tenth of the width with a fade, so the new screen seems to push the old one back.
 * - **Pop** has no enter: the screen under is already there. The popped screen *scales down and fades* rather than slides, because a
 *   directional slide fights the inward edge drag of the back gesture, and its fade is short ([NAV_POP_DURATION_MS]).
 * - **Predictive pop** is the same motion with a fade target of [NAV_PREDICTIVE_POP_ALPHA], so the preview under the finger is a subtle peek.
 * - **Tab predictive pop** is the same at a lighter [NAV_TAB_PREDICTIVE_POP_SCALE] scale and [NAV_TAB_PREDICTIVE_POP_ALPHA] alpha, for a drill-down inside a tab, which does not
 *   cover the window.
 * - **Tab switch** cross-fades the outgoing tab into the incoming one over [NAV_TAB_SWITCH_DURATION_MS]. Nothing slides or scales, so reduce motion changes nothing.
 *
 * Under reduce motion nothing slides or scales: the forward transition is a crossfade, and each pop is a fade-out of the popped screen over the same duration. Use [rememberBingeNavMotion] from
 * composition, or the builders directly with the flag a caller already holds.
 */
@Immutable
class BingeNavMotion internal constructor(
    val detailForward: ContentTransform,
    val detailPop: ContentTransform,
    val detailPredictivePop: ContentTransform,
    val tabPredictivePop: ContentTransform,
    val tabSwitch: ContentTransform,
)

/** The shared transitions for [LocalReduceMotion] and the current layout direction, remembered so a recomposition keeps the same instances. */
@Composable
fun rememberBingeNavMotion(layoutDirection: LayoutDirection = LocalLayoutDirection.current): BingeNavMotion {
    val reduceMotion = LocalReduceMotion.current
    return remember(reduceMotion, layoutDirection) {
        BingeNavMotion(
            detailForward = bingeDetailForward(reduceMotion, layoutDirection),
            detailPop = bingeDetailPop(reduceMotion),
            detailPredictivePop = bingeDetailPredictivePop(reduceMotion),
            tabPredictivePop = bingeTabPredictivePop(reduceMotion),
            tabSwitch = bingeTabSwitch(),
        )
    }
}

/**
 * A screen pushed on top of another: see [BingeNavMotion]. The slide runs towards the end edge in [layoutDirection], so it reverses in a
 * right-to-left layout. The new screen draws above the old one.
 */
fun bingeDetailForward(reduceMotion: Boolean, layoutDirection: LayoutDirection = LayoutDirection.Ltr): ContentTransform {
    val spec = tween<Float>(NAV_FORWARD_DURATION_MS, easing = FastOutSlowInEasing)
    if (reduceMotion) return ContentTransform(fadeIn(spec), fadeOut(spec))
    val direction = if (layoutDirection == LayoutDirection.Rtl) -1 else 1
    val slide = tween<IntOffset>(NAV_FORWARD_DURATION_MS, easing = FastOutSlowInEasing)
    return ContentTransform(
        targetContentEnter = slideInHorizontally(slide) { direction * it / NAV_FORWARD_ENTER_OFFSET_FRACTION } + fadeIn(spec),
        initialContentExit = slideOutHorizontally(slide) { -direction * it / NAV_FORWARD_EXIT_OFFSET_FRACTION } + fadeOut(spec),
    )
}

/** A screen popped by a button press or a completed back gesture: see [BingeNavMotion]. The popped screen draws above the one it reveals. */
fun bingeDetailPop(reduceMotion: Boolean): ContentTransform = pop(reduceMotion, scale = NAV_POP_SCALE, alpha = 0f)

/** A screen being dragged away by a predictive back gesture: [bingeDetailPop] with a fade target of [NAV_PREDICTIVE_POP_ALPHA]. */
fun bingeDetailPredictivePop(reduceMotion: Boolean): ContentTransform =
    pop(reduceMotion, scale = NAV_POP_SCALE, alpha = NAV_PREDICTIVE_POP_ALPHA)

/** A drill-down inside a tab being dragged away: the lighter [NAV_TAB_PREDICTIVE_POP_SCALE] scale and [NAV_TAB_PREDICTIVE_POP_ALPHA] alpha. */
fun bingeTabPredictivePop(reduceMotion: Boolean): ContentTransform =
    pop(reduceMotion, scale = NAV_TAB_PREDICTIVE_POP_SCALE, alpha = NAV_TAB_PREDICTIVE_POP_ALPHA)

/**
 * Switching to another tab: the old tab fades out as the new one fades in, over [NAV_TAB_SWITCH_DURATION_MS]. A cross-fade moves
 * nothing, so it is the same under reduce motion and the builder takes no flag.
 */
fun bingeTabSwitch(): ContentTransform {
    val spec = tween<Float>(NAV_TAB_SWITCH_DURATION_MS)
    return ContentTransform(fadeIn(spec), fadeOut(spec))
}

private fun pop(
    reduceMotion: Boolean,
    scale: Float,
    alpha: Float,
): ContentTransform {
    val spec = tween<Float>(NAV_POP_DURATION_MS)
    val exit: ExitTransition = if (reduceMotion) {
        fadeOut(spec, targetAlpha = alpha)
    } else {
        scaleOut(spec, targetScale = scale) + fadeOut(spec, targetAlpha = alpha)
    }
    // The popped screen is the initial content: draw it above the screen it reveals.
    return ContentTransform(EnterTransition.None, exit, targetContentZIndex = POP_Z_INDEX)
}

private const val POP_Z_INDEX = -1f
