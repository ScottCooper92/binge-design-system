package com.binge.designsystem.save

import com.binge.designsystem.SaveStatus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.IOException

/** A failure that would only repeat, as a server's refusal of the record does. */
private object Refusal : Exception("refused")

@OptIn(ExperimentalCoroutinesApi::class)
class SaveAsMadeTest {
    private val sent = mutableListOf<Int>()
    private val gates = mutableListOf<CompletableDeferred<Int>>()
    private var draft = 0
    private var saved = 0
    private var savable = true
    private val failedLog = mutableListOf<Boolean>()
    private val statusLog = mutableListOf<SaveStatus>()
    private val refusedLog = mutableListOf<Throwable>()

    private fun TestScope.saveAsMade(scope: CoroutineScope = this) =
        SaveAsMade(
            scope = scope,
            appScope = this,
            dispatcher = StandardTestDispatcher(testScheduler),
            draft = { draft },
            canSave = { savable },
            write = { value ->
                sent += value
                CompletableDeferred<Int>().also { gates += it }.await()
            },
            adopt = { sentValue, adopted ->
                saved = adopted
                if (draft == sentValue) draft = adopted
                draft != adopted
            },
            failed = { failedLog += it },
            isTransient = { it is IOException },
            status = { statusLog += it },
            refused = { refusedLog += it },
        )

    /** A refusal would only be refused again: it is reported once and owed nothing, so no Retry and no resend. */
    @Test
    fun `a write the server refuses is reported once and never kept for retry`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()

            gates[0].completeExceptionally(Refusal)
            runCurrent()
            val settled = mode.settle()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS * 3)
            runCurrent()
            mode.cleared()
            runCurrent()

            assertEquals(listOf<Throwable>(Refusal), refusedLog)
            assertEquals(listOf(false), failedLog, "never offered for retry")
            assertEquals(listOf(1), sent, "nothing sends it again")
            assertEquals(null, settled.await())
        }

    @Test
    fun `settling while a write is in flight reports its refusal once and sends it no more`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()

            val settled = mode.settle()
            runCurrent()
            gates[0].completeExceptionally(Refusal)
            runCurrent()

            assertEquals(null, settled.await())
            runCurrent()
            assertEquals(listOf(1), sent)
            assertEquals(listOf<Throwable>(Refusal), refusedLog)
            assertEquals(emptyList<Boolean>(), failedLog, "never offered for retry")
        }

    @Test
    fun `a change made during a write waits for it, then goes out from the answer`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            assertEquals(listOf(1), sent)

            draft = 2
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS * 3)
            runCurrent()
            assertEquals(listOf(1), sent, "nothing overlaps the write in flight")

            gates[0].complete(10)
            runCurrent()
            assertEquals(10, saved, "the server's answer is adopted")
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            assertEquals(listOf(1, 2), sent, "exactly one more write, with the newer draft")
            gates[1].complete(2)
            runCurrent()
            assertEquals(2, gates.size)
        }

    /**
     * A discard while a write is running drops that write's answer too: adopting it would queue the discarded change again,
     * and the page's read after the discard would send it over the record the discard was for. It holds because the
     * send waiting on the write is the one the discard cancels.
     */
    @Test
    fun `a write that lands after a discard queues nothing it discarded`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            draft = 2
            mode.changed()

            mode.discard()
            gates[0].complete(1)
            runCurrent()
            val settled = mode.settle()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS * 3)
            runCurrent()
            mode.cleared()
            runCurrent()

            assertEquals(listOf(1), sent, "only the write sent before the discard")
            assertEquals(null, settled.await())
            assertEquals(0, saved, "its answer is not adopted")
        }

    @Test
    fun `settling sends a change still waiting out its delay at once, and only once`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()

            val settled = mode.settle()
            runCurrent()
            assertEquals(listOf(1), sent)
            gates[0].complete(1)
            runCurrent()
            assertEquals(null, settled.await(), "the change landed, so nothing is kept")

            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS * 3)
            runCurrent()
            assertEquals(listOf(1), sent, "the cancelled wait sends nothing more")
        }

    @Test
    fun `settling waits for the write in flight, then sends the change made during it`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            draft = 2
            mode.changed()

            val settled = mode.settle()
            runCurrent()
            assertEquals(listOf(1), sent, "nothing overlaps the write in flight")
            gates[0].complete(1)
            runCurrent()
            assertEquals(listOf(1, 2), sent)
            gates[1].complete(2)
            assertEquals(null, settled.await())

            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS * 3)
            runCurrent()
            assertEquals(listOf(1, 2), sent, "the first write's answer schedules no third")
        }

    @Test
    fun `settling with nothing owed waits for the write in flight and sends nothing`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()

            val settled = mode.settle()
            runCurrent()
            assertEquals(false, settled.isCompleted)
            gates[0].complete(1)
            assertEquals(null, settled.await())
            assertEquals(listOf(1), sent)
        }

    @Test
    fun `settling retries a write that failed, and answers the draft if it fails again`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            gates[0].completeExceptionally(IOException("offline"))
            runCurrent()

            val settled = mode.settle()
            runCurrent()
            assertEquals(listOf(1, 1), sent)
            gates[1].completeExceptionally(IOException("still offline"))
            assertEquals(1, settled.await(), "the page keeps the unsent draft")
        }

    @Test
    fun `leaving after the scope is cancelled still waits for the write in flight`() =
        runTest {
            val viewModelScope = CoroutineScope(StandardTestDispatcher(testScheduler))
            val mode = saveAsMade(scope = viewModelScope)
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            draft = 2
            mode.changed()

            viewModelScope.cancel()
            runCurrent()
            mode.cleared()
            runCurrent()
            assertEquals(listOf(1), sent, "the leftover change waits for the write in flight")

            gates[0].complete(1)
            runCurrent()
            assertEquals(listOf(1, 2), sent)
            gates[1].complete(2)
            runCurrent()
        }

    @Test
    fun `a write says Saving while it runs and Saved when it lands`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            assertEquals(listOf(SaveStatus.Saving), statusLog)

            gates[0].complete(1)
            runCurrent()
            assertEquals(listOf(SaveStatus.Saving, SaveStatus.Saved), statusLog)
            assertEquals(listOf(false), failedLog)
        }

    @Test
    fun `a change the page may not send does not leave the line saying Saving`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()

            draft = 2
            savable = false
            mode.changed()
            gates[0].complete(1)
            runCurrent()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()

            assertEquals(listOf(1), sent)
            assertEquals(SaveStatus.Idle, statusLog.last())
        }

    @Test
    fun `a failure stays reported through a later change until a write succeeds`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            gates[0].completeExceptionally(IOException("offline"))
            runCurrent()
            assertEquals(listOf(true), failedLog)
            assertEquals(SaveStatus.Idle, statusLog.last(), "a failure is not a status of its own")

            // A later change goes out; the failure is not cleared by it starting, so the snackbar does not flicker away.
            draft = 2
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            assertEquals(listOf(1, 2), sent)
            assertEquals(listOf(true), failedLog)

            gates[1].complete(2)
            runCurrent()
            assertEquals(listOf(true, false), failedLog, "cleared by the success")
            assertEquals(SaveStatus.Saved, statusLog.last())
        }

    @Test
    fun `a retry that fails again leaves the failure showing, and one that succeeds clears it`() =
        runTest {
            val mode = saveAsMade()
            draft = 1
            mode.changed()
            advanceTimeBy(SAVE_AS_MADE_DELAY_MILLIS + 1)
            runCurrent()
            gates[0].completeExceptionally(IOException("offline"))
            runCurrent()

            mode.now()
            runCurrent()
            gates[1].completeExceptionally(IOException("still offline"))
            runCurrent()
            assertEquals(listOf(true, true), failedLog, "never cleared in between")

            mode.now()
            runCurrent()
            gates[2].complete(1)
            runCurrent()
            assertEquals(listOf(true, true, false), failedLog)
        }
}
