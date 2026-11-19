package com.binge.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier

/**
 * Swaps what a sheet shows when its state changes, cross-fading the old content into the new and easing the height
 * between them, so a list that finishes loading, or an error that replaces it, does not pop in.
 *
 * [content] is drawn for [targetState]. The swap runs when [contentKey] of the state changes, not on every new value:
 * the default is the state itself, so a caller whose state carries data that changes within one kind of content (a list
 * of rows being filtered, say) passes a key that names the kind, and only a change of kind animates.
 *
 * Only the changing region goes in here. The sheet's title, its status line and its primary footer stay outside, so
 * they hold still while the middle changes. A sheet that is a bottom sheet already follows a change of height; this
 * eases the region itself, so the two together read as one motion.
 *
 * Under reduced motion nothing animates and the old content is gone at once: only the new state is ever composed.
 *
 * @param targetState what to show now.
 * @param contentKey names the kind of content [targetState] is, so a change inside one kind does not animate.
 * @need swap a sheet's content when its state changes, without a jump
 * @group sheets
 * @replaces a hand-built when over a sheet's state with no transition
 */
@Composable
fun <S> BingeSheetStateSwitch(
    targetState: S,
    modifier: Modifier = Modifier,
    contentKey: (S) -> Any? = { it },
    content: @Composable (S) -> Unit,
) {
    val motion = rememberSheetMotion()
    if (motion.reduceMotion) {
        Box(modifier) { key(contentKey(targetState)) { content(targetState) } }
        return
    }
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            ContentTransform(
                targetContentEnter = fadeIn(motion.fade()),
                initialContentExit = fadeOut(motion.fade()),
                sizeTransform = SizeTransform(clip = false) { _, _ -> motion.spatial() },
            )
        },
        contentKey = contentKey,
        label = "BingeSheetStateSwitch",
        content = { content(it) },
    )
}
