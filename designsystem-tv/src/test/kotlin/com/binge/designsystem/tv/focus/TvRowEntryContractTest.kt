package com.binge.designsystem.tv.focus

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The shared focus-memory contract, pinned against its reference implementation [rememberTvRowEntry] — the seam
 * seven TV cell surfaces already share (rows, the detail/gallery/cast grids, search). Each case is one of the
 * invariants a six-implementations audit found restated per surface, so a regression in the contract
 * is one red test, not N:
 *
 * - **Saveable, so it outlives disposal** (a plain `remember` is what a scroll-out wipes) — survives a saved
 *   instance-state round trip.
 * - **The container is seeded to the remembered cell** (two bugs bound an entry requester to a cell that was
 *   never composed) — the entry requester resolves to the remembered cell and no other.
 * - **Staleness is resolved against the current data, never trusted** — a remembered index past the
 *   current item count is coerced in, not returned out of range.
 *
 * The fourth rule — memory kept separate from the ring key that a blur nulls — is a consumer-wiring rule (the
 * ring lives on the consumer, not here), pinned at the surfaces by the host app's own focus-semantics tests.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class TvRowEntryContractTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /** Saveable: the remembered cell rides `rememberSaveable`, so a saved-state restore keeps it (not a reset to 0). */
    @Test
    fun `the remembered cell survives state restoration`() {
        val restorer = StateRestorationTester(composeTestRule)
        lateinit var entry: TvRowEntry
        restorer.setContent {
            BingeTvTheme { entry = rememberTvRowEntry(itemCount = 5) }
        }
        composeTestRule.runOnIdle { entry.rememberFocused(3) }

        restorer.emulateSavedInstanceStateRestore()

        composeTestRule.runOnIdle {
            assertEquals("The remembered index must survive a saved-state restore", 3, entry.entryIndex)
        }
    }

    /** Seeding: the entry requester binds to the remembered cell and no other, so an arrival lands on it. */
    @Test
    fun `the entry requester resolves to the remembered cell`() {
        lateinit var entry: TvRowEntry
        composeTestRule.setContent {
            BingeTvTheme {
                entry = rememberTvRowEntry(itemCount = 3, overrideIndex = 2)
                Row {
                    repeat(3) { index ->
                        Box(
                            Modifier
                                .testTag("cell$index")
                                .size(40.dp)
                                .then(entry.entryModifier(index))
                                .focusable(),
                        )
                    }
                }
            }
        }
        composeTestRule.runOnUiThread { runCatching { entry.entryFocus.requestFocus() } }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("cell2").assertIsFocused()
    }

    /**
     * Reset key: the remembered cell is scoped to [resetKey]. A change recreates the memory (a new query enters
     * at the top), while it survives a recomposition that keeps the key — search's per-query reset.
     */
    @Test
    fun `the remembered cell resets when the reset key changes`() {
        lateinit var entry: TvRowEntry
        var resetKey by mutableStateOf("odyssey")
        composeTestRule.setContent {
            BingeTvTheme { entry = rememberTvRowEntry(itemCount = 5, resetKey = resetKey) }
        }
        composeTestRule.runOnIdle { entry.rememberFocused(3) }
        composeTestRule.waitForIdle()
        composeTestRule.runOnIdle {
            assertEquals("The memory holds within a reset key", 3, entry.entryIndex)
        }

        resetKey = "troy"
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            assertEquals("A new reset key re-enters at the top", 0, entry.entryIndex)
        }
    }

    /** Staleness: a remembered index past the current count is coerced in, never returned out of range. */
    @Test
    fun `the entry index coerces into the current item count`() {
        lateinit var shrunk: TvRowEntry
        lateinit var empty: TvRowEntry
        composeTestRule.setContent {
            BingeTvTheme {
                shrunk = rememberTvRowEntry(itemCount = 3, overrideIndex = 9)
                empty = rememberTvRowEntry(itemCount = 0, overrideIndex = 4)
            }
        }
        composeTestRule.runOnIdle {
            assertEquals("A too-large index clamps to the last cell", 2, shrunk.entryIndex)
            assertEquals("An empty list clamps to 0", 0, empty.entryIndex)
        }
    }
}
