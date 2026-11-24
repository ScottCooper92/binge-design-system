package com.binge.designsystem.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Keeps one drag to one job inside a sheet. Material hands a sheet whatever a list inside it leaves over, so one swipe down that
 * scrolls a short list back to its top carries on and closes the sheet. Here a drag that began by scrolling the content keeps its
 * leftover, and its fling, to itself; only a drag that began with the content already at its edge moves the sheet.
 */
internal class SheetScrollHandoff : NestedScrollConnection {
    private var gesture = Gesture.Idle
    private var holdFling = false

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (source != NestedScrollSource.UserInput) return Offset.Zero
        if (gesture == Gesture.Idle) gesture = if (consumed.y != 0f) Gesture.ScrolledContent else Gesture.MovesSheet
        return if (gesture == Gesture.ScrolledContent) Offset(0f, available.y) else Offset.Zero
    }

    // The drag ends here, so the next one decides afresh even where no fling follows; only the hold on this fling carries over.
    override suspend fun onPreFling(available: Velocity): Velocity {
        holdFling = gesture == Gesture.ScrolledContent
        gesture = Gesture.Idle
        return Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        val held = holdFling
        holdFling = false
        return if (held) Velocity(0f, available.y) else Velocity.Zero
    }

    private enum class Gesture { Idle, ScrolledContent, MovesSheet }
}
