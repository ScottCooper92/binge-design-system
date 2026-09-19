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
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import com.binge.designsystem.tv.theme.BingeTvTheme
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
 * **A real ← is never sent while the rail is already the focused, leftmost node.** #60 root-caused the freeze
 * PR #59 hit doing exactly that: `performKeyInput { pressKey(Key.DirectionLeft) }` against an already-focused
 * rail leaves the content focus group unable to compose new focusable children for the rest of the test — no
 * exception, no log, just a recomposition scope that silently stops being invalidated. The trigger is not
 * "`content` is empty" (that was this harness's own repro, not the actual precondition): it is any real
 * directional key whose default 2D focus search exhausts the **entire composition** without finding one
 * candidate anywhere, which is deterministic and content-independent here, because the rail is structurally
 * this layout's leftmost region — a further ← from an already-focused rail can never find a candidate, whether
 * `content` holds nothing, something unfocused, or something that already had focus and lost it. Confirmed
 * directly: a second real ← pressed from the rail freezes the harness identically with `content` fully
 * populated; an *unrelated* real key (`Key.A`) from the same parked rail does not freeze anything, so this is
 * specific to Compose recognising the key as one its default search acts on, not to real key input in general.
 *
 * It is not this component's `onPreviewKeyEvent` failing to guard the search: consuming the key in *both* its
 * `KeyDown` and `KeyUp` phases (so no modifier in this file ever reports the event unhandled) does not stop the
 * freeze, and neither does trapping the exit with `focusProperties { onExit = { cancelFocusChange() } }` — that
 * callback never even fires, because the search fails before it would offer a candidate to veto. Nor is it a
 * delay: pumping 300 frames afterward, or toggling `mainClock.autoAdvance` around the press, never recovers it.
 * There is no modifier-chain interception point between dispatch and whatever inside Compose UI's focus search
 * corrupts the recomposer here, which is why this is a harness defect to file upstream (against
 * `androidx.compose.ui.test`'s key dispatch on Robolectric) rather than a bug in this module's focus wiring.
 *
 * That also makes the exact race #54 asked for — a real ← landing on the rail *while the startup offer is still
 * retrying* because `content` has not taken focus yet — impossible to build with a literal key press here, not
 * merely inconvenient: "still retrying" **means** `content` has no focus yet, so the only node such a press can
 * ever be dispatched from is the rail itself, and a further ← from the rail is always the zero-candidate search
 * above. The two "parked rail" tests below and the one real-key test carry the discrimination #54 needs instead —
 * the parked cases model a real press's *end state* (`requestFocus()`, no key), and the real-key test presses ←
 * only once `content` already holds something focusable, landing safely on the rail without ever asking Compose
 * to search from it again.
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
     * With content never becoming focusable and nothing else claiming focus either, the startup offer's own
     * 120-frame budget runs out and its `railEntry.requestFocus()` fallback is what lands focus on the rail —
     * not an incidental focus state the loop mistook for the user having gotten there.
     */
    @Test
    fun `the startup offer falls back to the rail entry once content has nothing to offer`() {
        fixture()

        repeat(FOCUS_HANDOFF_FRAMES + PUMP_FRAMES) { composeTestRule.mainClock.advanceTimeByFrame() }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()
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
     * having anything to wait on losing. Fault injection: reverting this handoff's `yieldToRail` from
     * `userMovedToRail` to a raw `railHasFocus` read — the exact regression #54 names — fails this, since the
     * parked rail reads `railHasFocus == true` too and only `userMovedToRail` tells the two states apart.
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
        // Content still has nothing to offer — the rail keeps focus, and nothing has hung waiting on a loss.
        composeTestRule.onNodeWithText(RAIL_ITEM).assertIsFocused()

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
