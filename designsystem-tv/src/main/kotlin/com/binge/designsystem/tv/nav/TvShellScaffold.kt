package com.binge.designsystem.tv.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalInputModeManager
import com.binge.designsystem.tv.focus.tvExitFocusGroup
import kotlinx.coroutines.delay

/**
 * How long the rail's highlight may lead the content. The rail selects on focus, so holding ↓ walks every
 * destination in between, and content that followed at once would mount one screen per step.
 */
const val TV_SHELL_CONTENT_SETTLE_MILLIS = 220L

/**
 * A TV app's shell: [BingeTvNavRail] beside the selected destination's [content], each destination's UI state
 * kept while it is off screen, an optional full-screen [overlay] above it all, and the TV Back rule.
 *
 * Back with focus in the content moves focus onto the rail. Back on the rail, off [homeKey], selects home.
 * Back on the rail at home calls [onBackAtRoot], or leaves the app when that is null. An open [overlay], or
 * a handler composed inside [content] (a destination's own back stack), takes Back first. The overlay traps
 * directional focus, so it cannot drift onto the rail or content beneath it; its own ← or Back handler is
 * the way out. It is hosted with [LocalTvHostedAsOverlay] set, so its boards carry their own overscan.
 *
 * Keys are saved in a Bundle, so each item's key must be one: a String or an enum. [content] follows the
 * rail after [settleMillis], so walking the rail does not mount every destination it passes. Hoist [settle] to read
 * the settled key, or to jump straight to a destination with [TvShellSettle.jumpTo].
 *
 * A shell that keeps each destination's stack outside [content] passes [contentDepth] and [onPop]: Back with focus
 * in the content then pops while the depth is above one. [onGoHome] replaces selecting [homeKey] on Back, and focus
 * moves to the rail after it. An overlay hosted above this scaffold rather than in [overlay] sets [overlayOpen], so
 * the scaffold leaves Back to it. [contentFocusRequester] and [overlayEpoch] pass through to [BingeTvNavRail], for a
 * shell that restores focus into the content when such an overlay closes.
 *
 * [keyContentByDestination] true composes each destination under its own saved-state key, so a swap disposes the
 * old destination's tree as the new one mounts. Pass false for a shell that keeps destination state itself and
 * animates between destinations inside [content]: the content then stays one tree, with both panes present while
 * it animates. A destination that leaves the rail has its saved state dropped.
 *
 * The scaffold puts the window in keyboard input mode as it first composes. A launch by `am start`, an instrumentation
 * or some launchers leaves a TV app in touch mode, where `clickable` refuses focus, so the rail's startup offer would
 * find nothing to land on and the first D-pad press would carry focus into the rail. A TV has no touchscreen, so touch
 * mode here is only ever an artefact of how the app was started.
 */
@Composable
fun TvShellScaffold(
    header: TvNavRailItem?,
    items: List<TvNavRailItem>,
    footer: TvNavRailItem?,
    selectedKey: Any,
    homeKey: Any,
    onSelect: (Any) -> Unit,
    modifier: Modifier = Modifier,
    overlay: (@Composable BoxScope.() -> Unit)? = null,
    onBackAtRoot: (() -> Unit)? = null,
    contentDepth: Int = 1,
    pinFooter: Boolean = false,
    settleMillis: Long = TV_SHELL_CONTENT_SETTLE_MILLIS,
    settle: TvShellSettle = rememberTvShellSettle(selectedKey),
    onPop: (() -> Unit)? = null,
    onGoHome: (() -> Unit)? = null,
    overlayOpen: Boolean = overlay != null,
    contentFocusRequester: FocusRequester? = null,
    overlayEpoch: Int = 0,
    keyContentByDestination: Boolean = true,
    content: @Composable (key: Any) -> Unit,
) {
    LeaveTouchModeOnLaunch()
    SettleOn(settle, selectedKey, settleMillis)
    val settled = settle.settledKey
    val railFocus = remember { FocusRequester() }
    var railHasFocus by remember { mutableStateOf(false) }
    val atRoot = railHasFocus && selectedKey == homeKey
    val holder = rememberSaveableStateHolder()
    ForgetDepartedDestinations(holder, railKeys(header, items, footer))
    BackHandler(enabled = !overlayOpen && (!atRoot || onBackAtRoot != null)) {
        when {
            !railHasFocus && contentDepth > 1 && onPop != null -> onPop()
            !railHasFocus -> runCatching { railFocus.requestFocus() }
            selectedKey != homeKey && onGoHome != null -> {
                onGoHome()
                runCatching { railFocus.requestFocus() }
            }
            selectedKey != homeKey -> onSelect(homeKey)
            else -> onBackAtRoot?.invoke()
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        BingeTvNavRail(
            header = header,
            items = items,
            footer = footer,
            selectedKey = selectedKey,
            onSelect = onSelect,
            contentDepth = contentDepth,
            railFocusRequester = railFocus,
            onRailFocusChanged = { railHasFocus = it },
            contentFocusRequester = contentFocusRequester,
            overlayEpoch = overlayEpoch,
            pinFooter = pinFooter,
        ) {
            if (keyContentByDestination) {
                holder.SaveableStateProvider(settled) { content(settled) }
            } else {
                content(settled)
            }
        }
        overlay?.let {
            CompositionLocalProvider(LocalTvHostedAsOverlay provides true) {
                Box(modifier = Modifier.fillMaxSize().tvExitFocusGroup(), content = it)
            }
        }
    }
}

/**
 * The destination [TvShellScaffold]'s content shows: the rail's selection once it has held for the settle delay.
 * [jumpTo] settles on a key at once, for a deep link or a Back to home that should not wait; select the same key
 * through the scaffold's `selectedKey` too, or the content follows the rail back after the delay.
 */
@Stable
class TvShellSettle internal constructor(
    initialKey: Any,
) {
    /** The key the content shows. */
    var settledKey: Any by mutableStateOf(initialKey)
        internal set

    /** Settles on [key] now, skipping the delay. */
    fun jumpTo(key: Any) {
        settledKey = key
    }

    internal companion object {
        val Saver: Saver<TvShellSettle, Any> = Saver(save = { it.settledKey }, restore = { TvShellSettle(it) })
    }
}

/** A [TvShellSettle] starting on [initialKey], saved across recreation. The key must fit in a Bundle. */
@Composable
fun rememberTvShellSettle(initialKey: Any): TvShellSettle = rememberSaveable(saver = TvShellSettle.Saver) { TvShellSettle(initialKey) }

/**
 * Requests keyboard input mode if the window starts in touch mode. A `SideEffect`, not a `LaunchedEffect`: it runs as
 * the first composition applies, before the rail's startup offer gets its first frame, so that offer is made in
 * keyboard mode rather than spent in touch mode.
 */
@Composable
private fun LeaveTouchModeOnLaunch() {
    val inputModeManager = LocalInputModeManager.current
    SideEffect { if (inputModeManager.inputMode == InputMode.Touch) inputModeManager.requestInputMode(InputMode.Keyboard) }
}

/**
 * Moves [settle] onto [selectedKey] once it has held for [settleMillis]. Kept out of [TvShellScaffold] so the
 * wait is not read as part of the Back handler's focus request.
 */
@Composable
private fun SettleOn(
    settle: TvShellSettle,
    selectedKey: Any,
    settleMillis: Long,
) {
    LaunchedEffect(selectedKey) {
        if (selectedKey != settle.settledKey) {
            delay(settleMillis)
            settle.settledKey = selectedKey
        }
    }
}

private fun railKeys(
    header: TvNavRailItem?,
    items: List<TvNavRailItem>,
    footer: TvNavRailItem?,
): Set<Any> = (listOfNotNull(header) + items + listOfNotNull(footer)).mapTo(mutableSetOf()) { it.key }

/** Drops [holder]'s saved state for a destination once it leaves the rail, so it does not outlive the destination. */
@Composable
private fun ForgetDepartedDestinations(holder: SaveableStateHolder, keys: Set<Any>) {
    val previous = remember { mutableStateOf(keys) }
    LaunchedEffect(keys) {
        (previous.value - keys).forEach(holder::removeState)
        previous.value = keys
    }
}
