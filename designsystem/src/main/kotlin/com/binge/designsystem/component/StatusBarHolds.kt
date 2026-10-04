package com.binge.designsystem.component

import java.util.WeakHashMap

/**
 * Counts the live holders of a status-bar appearance per key — in practice per window — so the
 * appearance is released only when the last holder leaves.
 *
 * A navigation transition composes the outgoing and incoming screens together, and the outgoing one
 * disposes after the incoming one has entered. With one flag per window, the outgoing screen's
 * release would undo what the incoming screen just set. With a count, it only takes its own hold off.
 *
 * Keys are held weakly, so a destroyed window does not stay reachable through a count it never
 * released.
 */
internal class StatusBarHolds<K : Any> {
    private val counts = WeakHashMap<K, Int>()

    /** Adds a hold on [key]. */
    fun acquire(key: K) {
        counts[key] = (counts[key] ?: 0) + 1
    }

    /**
     * Takes one hold off [key]. Returns `true` when that was the last one, which is the caller's cue
     * to restore the default. A release with no hold outstanding also returns `true`.
     */
    fun release(key: K): Boolean {
        val remaining = (counts[key] ?: 0) - 1
        return if (remaining > 0) {
            counts[key] = remaining
            false
        } else {
            counts.remove(key)
            true
        }
    }

    /** How many holds [key] has outstanding. */
    fun count(key: K): Int = counts[key] ?: 0
}
