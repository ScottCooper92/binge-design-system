package com.binge.designsystem.tv.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.launch

/**
 * The shell's claim that the content it just switched to owes its first focusable one focus request. The shell [arm]s it on a cold
 * start, a tab switch or a change of content depth; the first [tvShellArrivalTarget] to be placed after that claims it and makes the request,
 * whenever its data lands. A direction key or Back [clear]s it, and a second [arm] before anyone claimed the first reports
 * the first as a miss. [onMiss] is the debug log.
 */
@Stable
class TvShellArrival(
    private val onMiss: (String) -> Unit = {},
    private val onTrace: (String) -> Unit = {},
) {
    private var pending = false
    private var lastKey: Any? = null
    private var started = false
    private var quiet = false

    /**
     * Arms the arrival when [key] differs from the last one passed. The first key arms only with [armFirst], which is a cold
     * start: the shell's first destination is owed focus as much as any it switches to. Idempotent, so it may be called in
     * composition, which must arm before the destination's nodes are placed.
     */
    fun armOnChange(key: Any, armFirst: Boolean = false) {
        if (!started) {
            started = true
            if (armFirst) arm()
        } else if (key != lastKey) {
            if (quiet) {
                quiet = false
                onTrace("quiet switch armed nothing")
            } else {
                arm()
            }
        }
        lastKey = key
    }

    /** The next change of destination is one the user already placed focus for (Back on the bar going home), so it arms nothing. */
    fun quietNextSwitch() {
        quiet = true
    }

    fun arm() {
        if (pending) onMiss("a new arrival superseded one no destination claimed")
        pending = true
        onTrace("arm")
    }

    /** The user chose where focus goes, so an arrival still waiting must not take it. */
    fun clear() {
        onTrace(if (pending) "clear while an arrival is pending" else "clear with nothing pending")
        pending = false
    }

    /** The destination has nothing to focus, so the arrival it was armed with is owed to no one. Not the user's choice, so not a [clear]. */
    internal fun settle() {
        if (!pending) return
        pending = false
        onTrace("settled: destination has no target")
    }

    /** Whether an arrival is armed and unclaimed, for the debug trace of a key that arrives while one is. */
    val isPending: Boolean get() = pending

    /** A debug-build trace line; the shell sends it to logcat under the hand-off tag. */
    fun trace(what: String) = onTrace(what)

    internal fun claim(): Boolean =
        pending.also {
            pending = false
            if (it) onTrace("claimed by a target")
        }

    internal fun miss(what: String) = onMiss(what)
}

/** Provided by the shell around its content; null outside it, where nothing is owed. */
val LocalTvShellArrival = staticCompositionLocalOf<TvShellArrival?> { null }

/**
 * Composed by a destination state that has nothing focusable (an empty plate): it settles the shell's pending arrival once,
 * so the next arm is not reported as a miss and a key later does not read as the user cancelling one.
 *
 * @need settle a tv shell's focus hand-off from a page with nothing to focus
 * @group tv
 */
@Composable
fun TvShellArrivalNone() {
    val arrival = LocalTvShellArrival.current
    LaunchedEffect(arrival) { arrival?.settle() }
}

/**
 * Marks a destination's first focusable. When it is first placed while the shell's arrival is pending it claims it and makes one request
 * for [requester], which this modifier also applies.
 */
fun Modifier.tvShellArrivalTarget(requester: FocusRequester): Modifier = focusRequester(requester).then(TvShellArrivalElement(requester))

/**
 * The claim without the requester: for a destination whose [requester] is attached on an inner node (a grid's body, its plate), so
 * the request is made on exactly that node. [tvShellArrivalTarget] would attach it here as well, and a request on a node with a
 * focusable sibling ahead of the body can land on the sibling.
 */
fun Modifier.tvShellArrivalClaims(requester: FocusRequester): Modifier = then(TvShellArrivalElement(requester))

private data class TvShellArrivalElement(
    val requester: FocusRequester,
) : ModifierNodeElement<TvShellArrivalNode>() {
    override fun create() = TvShellArrivalNode(requester)

    override fun update(node: TvShellArrivalNode) {
        node.requester = requester
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "tvShellArrivalTarget"
    }
}

private class TvShellArrivalNode(
    var requester: FocusRequester,
) : Modifier.Node(),
    LayoutAwareModifierNode,
    CompositionLocalConsumerModifierNode {
    private var claimed = false

    override fun onDetach() {
        claimed = false
    }

    override fun onPlaced(coordinates: LayoutCoordinates) {
        if (claimed) return
        claimed = true
        val arrival = currentValueOf(LocalTvShellArrival) ?: return
        if (!arrival.claim()) {
            arrival.trace("target reached with no arrival pending")
            return
        }
        coroutineScope.launch {
            val granted = runCatching { requester.requestFocus() }.getOrDefault(false)
            if (!granted) arrival.miss("the destination's first focusable did not take focus")
        }
    }
}
