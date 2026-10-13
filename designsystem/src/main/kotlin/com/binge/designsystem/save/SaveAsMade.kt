package com.binge.designsystem.save

import com.binge.designsystem.SaveStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** How long a page that saves as it changes waits after the last change, so a burst of changes is one write. */
const val SAVE_AS_MADE_DELAY_MILLIS = 600L

/**
 * The engine behind a page or a row that saves as it changes: each change writes the draft a moment after the last one,
 * with no Save to press. It owns the timing and the failure rules, and nothing about how a draft is held or stored, so a
 * form over a server record and a single switch use it the same way.
 *
 * The write runs on [appScope], so leaving the page cannot cut it off half-sent, and a change still waiting out its delay
 * when the page goes is sent then ([cleared]). A change made while a write is in flight goes out after it, from what the
 * write answered. A write that fails [transiently][isTransient] keeps the change, unsent, and says so through [failed],
 * which stays true until a write succeeds (a later change does not clear it, only its success does); [now] sends it again
 * at once. A write that fails any other way would only fail again, so it is not kept: [refused] takes the failure, once,
 * and the page puts its saved value back and says why. [status] follows the writes, for an inline "Saving… / Saved"
 * line; it is [SaveStatus.Idle], [SaveStatus.Saving] or [SaveStatus.Saved], and a failure is [failed], not a status of its
 * own, so the line and the Retry can come and go separately.
 *
 * It is not thread-safe on purpose: everything but the write itself runs on [scope]'s thread (the main thread for a
 * ViewModel), so a caller on another thread must hop first.
 */
class SaveAsMade<T>(
    private val scope: CoroutineScope,
    private val appScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    /** What would be written now, or null while there is nothing to write. */
    private val draft: () -> T?,
    /** Whether [draft] may be sent; a change it holds back is held, not lost. */
    private val canSave: (T) -> Boolean,
    private val write: suspend (T) -> T,
    /** Takes the answer to [sent]; true if the draft has moved on since, so it needs writing too. */
    private val adopt: (sent: T, adopted: T) -> Boolean,
    private val failed: (Boolean) -> Unit,
    /** Whether a failure is worth another try (the network, say) rather than a refusal that would only repeat. */
    private val isTransient: (Throwable) -> Boolean,
    private val status: (SaveStatus) -> Unit = {},
    /** A write that failed for good: nothing is owed, and the page puts its saved value back and says why. */
    private val refused: suspend (Throwable) -> Unit = {},
) {
    // All of this is touched on [scope]'s thread only; just the write itself leaves it.
    private var pending: Job? = null
    private var inFlight: Deferred<Result<T>>? = null
    private var unsent = false

    // The latest write, cleared only by [discard]: cancelling [scope] ends send()'s wait on it, but the write itself is still running.
    private var lastWrite: Deferred<Result<T>>? = null

    /** The draft changed: write it once the changes stop. */
    fun changed() = schedule(SAVE_AS_MADE_DELAY_MILLIS)

    /** Write the draft now: a retry after a failure. */
    fun now() = schedule(0)

    private fun schedule(wait: Long) {
        unsent = true
        // A write in flight is never cancelled or overlapped: its answer is adopted, and what changed meanwhile goes next.
        if (inFlight != null) return
        pending?.cancel()
        pending =
            scope.launch {
                delay(wait)
                send()
            }
    }

    private suspend fun send() {
        // Nothing it may send: the last change is held, not saved, so the line must not keep saying Saving.
        val sent = draft()?.takeIf(canSave) ?: return status(SaveStatus.Idle)
        unsent = false
        status(SaveStatus.Saving)
        // Caught inside the write, so a failure stays this save's and does not reach the application's scope.
        val write = appScope.async(dispatcher) { attempt { write(sent) } }
        inFlight = write
        lastWrite = write
        val result =
            try {
                write.await()
            } finally {
                inFlight = null
            }
        result
            .onSuccess { adopted ->
                failed(false)
                // More to send: the line keeps saying Saving until the last write lands.
                if (adopt(sent, adopted) || unsent) changed() else status(SaveStatus.Saved)
            }.onFailure { failure ->
                status(SaveStatus.Idle)
                if (isTransient(failure)) {
                    unsent = true
                    failed(true)
                } else {
                    // Nothing owed: a later change rode on the refused record, and settle() must not send it again.
                    unsent = false
                    lastWrite = null
                    failed(false)
                    refused(failure)
                }
            }
    }

    /**
     * The page is about to read its record again: a change still waiting, or one whose write failed, is sent first, after
     * any write already running, so the read sees it. Called before the page drops its draft, which is captured here.
     * Answers the draft if its write failed, so the page can keep it on top of what it reads, and null otherwise.
     */
    fun settle(): Deferred<T?> {
        pending?.cancel()
        val owed = unsent
        // Owed here now, so the running write's answer does not schedule another send of the draft about to be replaced.
        unsent = false
        val kept = draft()
        val earlier = lastWrite
        return appScope.async(dispatcher) {
            val earlierError = earlier?.await()?.exceptionOrNull()
            if (earlierError != null && !isTransient(earlierError)) {
                // The write send() was waiting on, which this settle cancelled: its refusal is reported here, and nothing rides on it.
                report(earlierError)
                return@async null
            }
            val left = kept?.takeIf { (owed || earlierError != null) && canSave(it) } ?: return@async null
            val error = attempt { write(left) }.exceptionOrNull() ?: return@async null
            if (isTransient(error)) {
                left
            } else {
                report(error)
                null
            }
        }
    }

    /** A failure [settle] met, told on [scope] because [refused] touches the page's state, which only that thread does. */
    private fun report(error: Throwable) {
        scope.launch { refused(error) }
    }

    /**
     * The record was replaced from elsewhere: a change still waiting, or one whose write failed, is dropped rather than
     * written over it. A write already running is left to finish, but its answer is not taken: [pending] is the send
     * waiting on it, so cancelling it drops the answer, and nothing it would have adopted or queued again reaches the page.
     */
    fun discard() {
        pending?.cancel()
        unsent = false
        lastWrite = null
        status(SaveStatus.Idle)
        failed(false)
    }

    /** The page has read its record again; [keptUnsent] says whether it kept a change [settle] could not send. */
    fun reloaded(keptUnsent: Boolean) {
        scope.launch { unsent = keptUnsent }
    }

    /**
     * The page is going: a change still waiting is sent now, where the page's own scope can no longer cancel it, after
     * any write already running. That includes a change whose write failed, so leaving with the failure showing retries
     * it once, and a second failure is not reported.
     */
    fun cleared() {
        val left = draft()?.takeIf(canSave) ?: return
        if (!unsent) return
        pending?.cancel()
        val earlier = lastWrite
        appScope.launch(dispatcher) {
            earlier?.await()
            attempt { write(left) }
        }
    }
}

/**
 * [runCatching] catches everything, cancellation included, which would swallow the cancellation a cancelled scope sends.
 * This lets that one back out and treats the rest as a failure for the caller to classify.
 */
@Suppress("TooGenericExceptionCaught")
private inline fun <T> attempt(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
