package com.binge.designsystem.tv.nav

import android.app.Application
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.focus.TV_FOCUS_SINK_TAG
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ITEM_KEY = "item"
private const val RAIL_ITEM = "rail item"
private const val OLD = "old"
private const val NEW = "new"

/** Frames to run a loop over when it claims on its first pass or not at all. */
private const val PUMP_FRAMES = 5

/** Frames to keep the outgoing screen composed-and-focused for, standing in for its exit transition. */
private const val HANDOFF_HOLD_FRAMES = 3

/**
 * The contract for [BingeTvNavRail]'s three focus-handoff loops (`docs/tv-foundation.md`; #54): the startup
 * offer's yield condition, [contentHandoffInFlight]'s sequenced drill-down handoff, and
 * [overlayCloseHandoffInFlight]'s unsequenced pop handoff.
 *
 * Every fixture holds a single rail item and drives `content` through a tri-state slot ([ContentSlot]) so a
 * test can make it nothing-focusable, one focusable item, or a different one, on demand. [fixture] pins
 * `expanded = true` so the item's label renders and is findable by text — cosmetic only, the rail's own
 * `railHasFocus`/`onFocusChanged` bookkeeping and all three handoffs are unaffected by it.
 *
 * The "Compose recovery park" case is modelled directly rather than reproduced: a `requestFocus()` call on
 * the rail item, with no key event ever fired, lands the rail exactly in the state Compose's own
 * disposal-triggered recovery would — `railHasFocus` true, `lastKeyWasStartDirectionKey` false — without
 * depending on a specific disposal timing this harness cannot reliably reproduce.
 *
 * **A real ← is never sent while `content` has nothing focusable.** Doing so is a genuine dead end in this
 * harness, not a production one: `performKeyInput { pressKey(Key.DirectionLeft) }` against a rail that is
 * already the leftmost focusable, with `content`'s focus group completely empty, leaves that focus group
 * unable to compose new focusable children for the rest of the test — no exception, no log, just a
 * recomposition scope that silently stops being invalidated (reproduced against several structurally
 * different `content` shapes, including one with a stable node identity whose focusability alone toggled). The
 * one real-key test below presses ← only once `content` already holds something focusable, which sidesteps it;
 * the discrimination between a genuine key and a merely-parked rail — the regression #54 itself names — is
 * carried instead by the two negative "parked rail" tests, each fault-proven directly against that regression
 * (see their own KDoc).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w960dp-h540dp-television-xhdpi")
class BingeTvNavRailFocusTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * The rail holding focus with no directional key behind it — Compose's own recovery park, modelled per the
     * class KDoc — must not read as `userMovedToRail`. The startup offer keeps retrying and takes content the
     * moment it has something to offer, rather than leaving the app stranded on the rail because it happened to
     * hold focus. Fault injection, run against this fixture: reverting the startup offer's `yieldToRail` to a
     * raw `railHasFocus` read (#54's own example of the regression) fails this — the rail would keep focus it
     * never genuinely earned.
     */
    @Test
    fun `the startup offer does not treat a parked rail focus as the user having moved there`() {
        val fixture = fixture()

        composeTestRule.onNodeWithText(RAIL_ITEM).requestFocus()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()
        pumpFrames()

        fixture.contentSlot = ContentSlot.NEW
        pumpFrames()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(NEW).assertIsFocused()
    }

    /**
     * With content never becoming focusable and nothing else claiming focus either, focus is still on the rail
     * or [TvFocusSink] well past the startup offer's 120-frame budget — there is no dead end where neither holds
     * it. #2523 deletes the offer's own `railEntry.requestFocus()` fallback: since #2518, [TvFocusSink] claims
     * the content group's entry before that fallback could ever run, and the two then alternate every frame
     * indefinitely (a sink holding focus disposes itself the next frame, and Compose's own disposal-triggered
     * recovery search — not this rail's code — lands the frame in between). Landing on the rail specifically at
     * this one sampled frame is a coincidence of that oscillation's parity, not a deliberate fallback; see
     * [BingeTvNavRailStartupFallbackRemovalTest] for the invariant checked across every frame instead of one.
     */
    @Test
    fun `focus is on the rail or the sink once the startup offer's budget has run out`() {
        fixture()

        repeat(FOCUS_HANDOFF_FRAMES + PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        val railFocused = composeTestRule.onAllNodesWithText(RAIL_ITEM).fetchSemanticsNodes().any {
            it.config.getOrElse(SemanticsProperties.Focused) { false }
        }
        val sinkFocused = composeTestRule.onAllNodesWithTag(TV_FOCUS_SINK_TAG).fetchSemanticsNodes().any {
            it.config.getOrElse(SemanticsProperties.Focused) { false }
        }
        assertTrue("neither the rail nor the sink held focus", railFocused || sinkFocused)
    }

    /**
     * A real ← is captured by the outer `onPreviewKeyEvent`, and the default focus search it falls through to
     * lands focus back on the rail's selected entry, with content still holding something it could otherwise be
     * offered. Complements the two negative cases in this file (a parked rail must *not* read as
     * `userMovedToRail`, for both the startup offer and [overlayCloseHandoffInFlight]) with the positive one: a
     * genuine key press works end to end and nothing pulls focus back off the rail afterward.
     */
    @Test
    fun `a real left lands focus on the rail, and nothing pulls it back off afterward`() {
        val fixture = fixture()

        // Let the startup offer hand focus to content once it has something, so the key below is pressed
        // against a non-empty content pane (class KDoc).
        pumpFrames()
        fixture.contentSlot = ContentSlot.NEW
        pumpFrames()
        composeTestRule.onNodeWithTag(NEW).assertIsFocused()

        composeTestRule.onNodeWithTag(NEW).performKeyInput { pressKey(Key.DirectionLeft) }
        pumpFrames()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()

        fixture.overlayEpoch = 1
        pumpFrames()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()
        composeTestRule.onNodeWithTag(NEW).assertIsNotFocused()
    }

    /**
     * A `contentDepth` change fires [contentHandoffInFlight], which is sequenced rather than raced: the outgoing
     * screen stays composed and focused for a few frames (its exit transition), and the handoff must still be
     * waiting on that loss rather than having raced ahead and given up.
     */
    @Test
    fun `contentHandoffInFlight takes focus into the replacement once the outgoing screen loses it`() {
        val fixture = fixture(initialContent = ContentSlot.OLD)
        pumpFrames()
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()

        fixture.contentDepth = 2
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()
        // The exit transition: the outgoing screen stays composed and focused for a few frames yet.
        pumpFrames(HANDOFF_HOLD_FRAMES)
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()

        // OLD disposes into a genuinely empty content, distinctly *before* NEW ever appears — not swapped in
        // the same recomposition — so a later grant onto NEW can only be this handoff's own offer taking it,
        // never Compose's disposal-triggered recovery landing on whatever new node happens to be sitting in
        // OLD's old spot (the trap `TvArrivalFocusTest`'s KDoc names: "a no-op effect would pass for the wrong
        // reason"). Fault-proven: racing `awaitContentFocusLost` away — offering the instant the depth changes,
        // while OLD is still focused — makes `taken()` true on the very first check, and the handoff exits
        // without ever retrying, so NEW is stuck unfocused; the assertion below is red without the wait.
        fixture.contentSlot = ContentSlot.NOTHING
        pumpFrames()

        fixture.contentSlot = ContentSlot.NEW
        pumpFrames()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(NEW).assertIsFocused()
    }

    /**
     * An `overlayEpoch` change fires [overlayCloseHandoffInFlight], which — unlike the drill-down — never waits
     * on a focus loss: the pane an overlay closes onto is a fresh mount that never held focus. The rail is
     * parked with focus per the class KDoc's recovery-park modelling, standing in for whatever the pop actually
     * left focus on; the handoff must still take content the moment it has something to offer, without ever
     * having anything to wait on losing. Since #2518, "content has nothing to offer" no longer means the group is
     * empty — [TvFocusSink] is always something, so the handoff claims *that* rather than leaving focus parked
     * on the rail. Fault injection: reverting this handoff's `yieldToRail` from `userMovedToRail` to a raw
     * `railHasFocus` read — the exact regression #54 names — fails this, since the parked rail reads
     * `railHasFocus == true` too and only `userMovedToRail` tells the two states apart.
     */
    @Test
    fun `overlayCloseHandoffInFlight takes focus without waiting on a focus-loss that never happens`() {
        // Content starts with something so the startup offer claims it and its own loop completes — otherwise
        // it would still be retrying every frame throughout this test and could mask a broken overlay handoff
        // by claiming NEW itself once it appears (see class KDoc for why nothing below presses a key against
        // still-empty content, which is a separate concern from this one).
        val fixture = fixture(initialContent = ContentSlot.OLD)
        pumpFrames()
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()

        fixture.contentSlot = ContentSlot.NOTHING
        pumpFrames()
        // Parked on the rail with no genuine arrival key — the shape a fresh pane returns to after an overlay
        // closes, with nothing behind it to have held focus and lost it.
        composeTestRule.onNodeWithText(RAIL_ITEM).requestFocus()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()

        fixture.overlayEpoch = 1
        pumpFrames()
        // The sink is always available, so the handoff claims it rather than leaving focus parked on the rail —
        // nothing has hung waiting on a loss.
        composeTestRule.onNodeWithTag(TV_FOCUS_SINK_TAG).assertIsFocused()

        fixture.contentSlot = ContentSlot.NEW
        pumpFrames()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(NEW).assertIsFocused()
    }

    /**
     * The stale-flag case #55 names: a real ← earlier in the session sets `lastKeyWasStartDirectionKey`, but
     * both times focus subsequently moves — off the rail, then back onto it — happen with no key event at all
     * (a bare `requestFocus()`, exactly how the handoffs themselves move focus, and how a pointer/touch
     * selection would too). Without the reset, that stale `true` survives the rail losing focus and is
     * misread on the *later*, unrelated arrival as the user having just pressed ← again. Fault injection: with
     * the `onFocusChanged` reset in [BingeTvNavRail] removed, this fails — the parked rail reads
     * `userMovedToRail == true` from the old press and [overlayCloseHandoffInFlight] wrongly yields to it,
     * leaving `OLD` stuck unfocused.
     */
    @Test
    fun `lastKeyWasStartDirectionKey does not outlive the rail-focus session it was set during`() {
        val fixture = fixture(initialContent = ContentSlot.OLD)
        pumpFrames()
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()

        // A real <- moves focus onto the rail — lastKeyWasStartDirectionKey is genuinely true here.
        composeTestRule.onNodeWithTag(OLD).performKeyInput { pressKey(Key.DirectionLeft) }
        pumpFrames()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()

        // Focus leaves the rail with no further key event.
        composeTestRule.onNodeWithTag(OLD).requestFocus()
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()

        // The rail regains focus again, also with no key event — modelling Compose's own disposal-triggered
        // recovery park (class KDoc), not a second real press.
        composeTestRule.onNodeWithText(RAIL_ITEM).requestFocus()
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()

        fixture.overlayEpoch = 1
        pumpFrames()
        composeTestRule.waitForIdle()

        // Without the reset this stays on the rail; the fix must take content back instead.
        composeTestRule.onNodeWithTag(OLD).assertIsFocused()
    }

    private fun pumpFrames(frames: Int = PUMP_FRAMES) {
        repeat(frames) { composeTestRule.mainClock.advanceTimeByFrame() }
    }

    private fun fixture(initialContent: ContentSlot = ContentSlot.NOTHING): Fixture {
        val fixture = Fixture(initialContent)
        composeTestRule.mainClock.autoAdvance = false
        composeTestRule.setContent {
            BingeTvTheme {
                BingeTvNavRail(
                    header = null,
                    items = listOf(TvNavRailItem(key = ITEM_KEY, label = RAIL_ITEM, icon = Icons.Filled.Home)),
                    footer = null,
                    selectedKey = ITEM_KEY,
                    onSelect = {},
                    // Pinned so the item's label renders and the node is findable by text; the handoffs under
                    // test don't read this parameter at all (see class KDoc).
                    expanded = true,
                    contentDepth = fixture.contentDepth,
                    overlayEpoch = fixture.overlayEpoch,
                ) {
                    when (fixture.contentSlot) {
                        ContentSlot.NOTHING -> Box(Modifier.size(80.dp))
                        ContentSlot.OLD -> Box(Modifier.testTag(OLD).size(80.dp).focusable())
                        ContentSlot.NEW -> Box(Modifier.testTag(NEW).size(80.dp).focusable())
                    }
                }
            }
        }
        return fixture
    }

    private enum class ContentSlot { NOTHING, OLD, NEW }

    private class Fixture(
        initialContent: ContentSlot,
    ) {
        var contentSlot by mutableStateOf(initialContent)
        var contentDepth by mutableIntStateOf(1)
        var overlayEpoch by mutableIntStateOf(0)
    }
}
