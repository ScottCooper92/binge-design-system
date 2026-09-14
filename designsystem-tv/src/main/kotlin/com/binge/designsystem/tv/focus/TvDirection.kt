package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * The D-pad key that travels toward the layout's **start** edge — physical ← under LTR, → under RTL.
 *
 * Compose's containers mirror on their own (alignments, `Row` order, `start`/`end` padding), but a key
 * handler written against [Key.DirectionLeft] does not. That split is the defect this exists to stop: the side sheet's
 * panel is `end`-aligned, so under RTL it renders on the left while ← — now the key pointing *into* it —
 * still dismissed it. A panel moved to the other side of the screen, and the button that closed it did not.
 *
 * Resolve direction here rather than at each call site, the way `startHorizontalGradient` resolves a
 * gradient: there were seven physical-direction handlers, and seven copies of a mirror is seven places to
 * get it wrong once and never notice, since nothing in an LTR locale renders differently either way.
 */
@Composable
@ReadOnlyComposable
fun tvStartDirectionKey(): Key = tvStartDirectionKey(LocalLayoutDirection.current)

/** The D-pad key that travels toward the layout's **end** edge — physical → under LTR, ← under RTL. */
@Composable
@ReadOnlyComposable
fun tvEndDirectionKey(): Key = tvEndDirectionKey(LocalLayoutDirection.current)

/**
 * The focus-search direction toward the layout's **end** edge.
 *
 * [FocusDirection] has no `Start`/`End` of its own — `Left`/`Right` are physical, like the keys — so a
 * `moveFocus` paired with an end-edge key press has to be mirrored alongside it or the two disagree.
 */
@Composable
@ReadOnlyComposable
fun tvEndFocusDirection(): FocusDirection = tvEndFocusDirection(LocalLayoutDirection.current)

internal fun tvStartDirectionKey(layoutDirection: LayoutDirection): Key =
    when (layoutDirection) {
        LayoutDirection.Ltr -> Key.DirectionLeft
        LayoutDirection.Rtl -> Key.DirectionRight
    }

internal fun tvEndDirectionKey(layoutDirection: LayoutDirection): Key =
    when (layoutDirection) {
        LayoutDirection.Ltr -> Key.DirectionRight
        LayoutDirection.Rtl -> Key.DirectionLeft
    }

internal fun tvEndFocusDirection(layoutDirection: LayoutDirection): FocusDirection =
    when (layoutDirection) {
        LayoutDirection.Ltr -> FocusDirection.Right
        LayoutDirection.Rtl -> FocusDirection.Left
    }
