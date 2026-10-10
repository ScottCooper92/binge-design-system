package com.binge.designsystem.tv.testing

import android.content.pm.PackageManager.FEATURE_LEANBACK
import androidx.compose.ui.test.junit4.ComposeTestRule
import org.junit.rules.ExternalResource
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

/**
 * Past the immersive pages' 90ms settled-focus debounce (`FOCUS_SETTLE_MILLIS`), with room for the frames after it.
 * Pass it to [settle] after a move that a settled-focus effect reacts to.
 */
const val SETTLED_FOCUS_WAIT_MILLIS = 300L

/**
 * Past the immersive pages' 200ms anchor-keeper coalesce window (`ANCHOR_COALESCE_MILLIS`), with room for a burst
 * that restarts it. Pass it to [settle] after content arrives that an anchor keeper re-pins.
 */
const val ANCHOR_COALESCE_WAIT_MILLIS = 1_000L

/**
 * Makes the package manager declare `FEATURE_LEANBACK`, as a TV's does. Robolectric reports no system features, and
 * leanback is what switches `LocalBringIntoViewSpec` to the TV pivot spec: without it an already-visible item requests
 * no scroll, so a pivot, anchor or skeleton-geometry test asserts nothing. Apply it before the compose rule.
 */
class LeanbackRule : ExternalResource() {
    override fun before() {
        shadowOf(RuntimeEnvironment.getApplication().packageManager).setSystemFeature(FEATURE_LEANBACK, true)
    }
}

/**
 * Advances the main clock by [advanceMillis], then waits for idle. A settled-focus debounce or an anchor keeper's
 * coalesce window is a coroutine `delay`, which `waitForIdle` alone does not pump. Pass [SETTLED_FOCUS_WAIT_MILLIS]
 * or [ANCHOR_COALESCE_WAIT_MILLIS].
 */
fun ComposeTestRule.settle(advanceMillis: Long) {
    mainClock.advanceTimeBy(advanceMillis)
    waitForIdle()
}
