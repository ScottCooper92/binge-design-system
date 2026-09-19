package com.binge.designsystem.tv.testing

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/** Size of the built-in sibling stand-in — arbitrary, only large enough to be a real focus target. */
private val SIBLING_SIZE = 80.dp

/** Default test tag for [TvLateTarget]'s own sibling; a screen-level fixture composing several instances overrides it. */
const val TV_LATE_TARGET_SIBLING_TAG = "tv-late-target-sibling"

/**
 * The adverse-schedule fixture (#2521): a focusable sibling stand-in ([siblingTag]) — modelling the outgoing
 * screen/overlay a handoff waits on — that composes until [dispose] flips true, at which point [content] — the real target a
 * handoff's retry loop is aimed at — is composed only after [delayFrames] `withFrameNanos` ticks have passed.
 *
 * A fixture, not a parameterised test per screen: every handoff under test (`offerFocusToContent`,
 * `contentHandoffInFlight`, `offerTvArrivalFocus`) already retries every frame against whatever the target
 * requester resolves to, so delaying *composition* of [content] — rather than merely its focus attachment —
 * is the most adverse schedule that retry loop can be asked to survive: the requester has nothing to attach to
 * at all until the delay elapses. [delayFrames] is a frame count, not a duration, matching the production
 * loops it exercises (see `TvNavRailFocus.kt`'s header comment).
 *
 * `dispose` is a caller-driven `Boolean` rather than an internal timer so a test can hold it with
 * `mainClock.autoAdvance = false` and step frames between the flip and the delayed composition — the ordering
 * the wall-clock-budget cases need.
 */
@Composable
fun TvLateTarget(
    delayFrames: Int,
    dispose: Boolean,
    modifier: Modifier = Modifier,
    siblingTag: String = TV_LATE_TARGET_SIBLING_TAG,
    content: @Composable () -> Unit,
) {
    if (!dispose) {
        Box(modifier.testTag(siblingTag).size(SIBLING_SIZE).focusable())
        return
    }
    var attached by remember(delayFrames) { mutableStateOf(delayFrames <= 0) }
    if (delayFrames > 0) {
        LaunchedEffectFrames(delayFrames) { attached = true }
    }
    if (attached) content()
}

/** `LaunchedEffect(Unit)` that ticks [frames] `withFrameNanos` waits before running [onElapsed] once. */
@Composable
private fun LaunchedEffectFrames(frames: Int, onElapsed: () -> Unit) {
    LaunchedEffect(Unit) {
        repeat(frames) { withFrameNanos { } }
        onElapsed()
    }
}
