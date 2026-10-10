package com.binge.designsystem.tv.nav

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
 * rail after [settleMillis], so walking the rail does not mount every destination it passes.
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
    content: @Composable (key: Any) -> Unit,
) {
    var settled by rememberSaveable { mutableStateOf(selectedKey) }
    LaunchedEffect(selectedKey) {
        if (selectedKey != settled) {
            delay(settleMillis)
            settled = selectedKey
        }
    }
    val railFocus = remember { FocusRequester() }
    var railHasFocus by remember { mutableStateOf(false) }
    val atRoot = railHasFocus && selectedKey == homeKey
    BackHandler(enabled = overlay == null && (!atRoot || onBackAtRoot != null)) {
        when {
            !railHasFocus -> runCatching { railFocus.requestFocus() }
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
            pinFooter = pinFooter,
        ) {
            val holder = rememberSaveableStateHolder()
            holder.SaveableStateProvider(settled) { content(settled) }
        }
        overlay?.let {
            CompositionLocalProvider(LocalTvHostedAsOverlay provides true) {
                Box(modifier = Modifier.fillMaxSize().tvExitFocusGroup(), content = it)
            }
        }
    }
}
