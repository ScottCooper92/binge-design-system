package com.binge.designsystem.refresh

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reports whether a reload is running, for the apps' view models, so a screen can show `BingePullToRefresh`'s spinner without
 * swapping its content for a skeleton. It owns no content and no trigger: it wraps whatever reload a view model already runs.
 *
 * ```
 * suspend fun refresh() = refreshTracker.track { repository.load() }
 * ```
 *
 * [isRefreshing] is true from the start of a [track]ed block until it ends, however it ends: it returns, throws or is cancelled.
 * Reloads that overlap (a pull while a trigger-driven reload runs) keep it true until the last of them ends. The screen
 * state keeps its last content while it is true, and carries the flag as a field of its data variant, so the screen still has
 * the one state stream. See `docs/pull-to-refresh.md`.
 *
 * Like [com.binge.designsystem.save.SaveAsMade], it has no Compose in it: every app's view models already sit in modules that depend on
 * `designsystem`, so a module of its own would cost each of them a dependency for one small class.
 */
class RefreshTracker {
    private val lock = Any()
    private var running = 0
    private val refreshing = MutableStateFlow(false)

    /** Whether any tracked reload is running now. */
    val isRefreshing: StateFlow<Boolean> = refreshing.asStateFlow()

    /** Runs [block], and reports it as a refresh while it runs. Its result and its failure pass through unchanged. */
    suspend fun <T> track(block: suspend () -> T): T {
        synchronized(lock) {
            running++
            refreshing.value = true
        }
        try {
            return block()
        } finally {
            synchronized(lock) {
                running--
                refreshing.value = running > 0
            }
        }
    }
}
