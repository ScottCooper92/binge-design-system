package com.binge.designsystem.shell

import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Replaces the whole stack with [routes] in one snapshot, writing over it in place: a route equal to the one already at its
 * position keeps that instance, so its entry is not rebuilt, and the stack is never empty part-way, which a `NavDisplay`
 * would throw on.
 *
 * @throws IllegalArgumentException if [routes] is empty: a back stack needs a root.
 *
 * @need swap a navigation back stack's contents in one step without rebuilding unchanged entries
 * @group bars
 * @replaces a hand-built clear-then-add on a back stack
 */
fun NavBackStack<NavKey>.replaceAll(routes: List<NavKey>) {
    require(routes.isNotEmpty()) { "a back stack needs a root" }
    Snapshot.withMutableSnapshot {
        routes.forEachIndexed { index, route ->
            if (index >= size) {
                add(route)
            } else if (this[index] != route) {
                set(index, route)
            }
        }
        while (size > routes.size) removeAt(lastIndex)
    }
}

/**
 * Pops everything above the root, leaving the root's own instance in place. Does nothing when the stack is at its root.
 *
 * @need return a navigation back stack to its root
 * @group bars
 * @replaces a hand-built pop loop to a back stack's root
 */
fun NavBackStack<NavKey>.popToRoot() {
    if (size > 1) replaceAll(listOf(first()))
}
