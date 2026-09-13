package com.binge.designsystem.tv.focus

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf

/** The spec in force outside the nearest [TvStableFocusScroll], so a nested scrollable can put it back. */
@OptIn(ExperimentalFoundationApi::class)
private val LocalInheritedBringIntoViewSpec = compositionLocalOf<BringIntoViewSpec?> { null }

/**
 * Confines a scrollable to the *minimum* bring-into-view scroll: an already-visible child is left exactly where
 * it is, and only a fully off-screen one is scrolled to. So a sideways or downward focus move between children
 * that are already on screen requests **no** scroll — focus is colour, not geometry
 * (`docs/tv-foundation.md` > The accent model).
 *
 * **Wrap the scrollable itself** — the `LazyColumn`, `LazyRow` or grid this governs. Wrapping content *inside*
 * one of its items does nothing: `LocalBringIntoViewSpec` is read by the scrollable's own node, so a provider in
 * a descendant item sits below the reader and is never seen — wrap the container, as the host app's immersive
 * hub and poster grid do.
 *
 * Without it, a leanback device parks each newly focused child at a fraction of the viewport, so the list
 * re-pivots even on a wholly-visible child; where a screen also pins itself to the top, pin and pivot fight and
 * the band jumps on every press.
 *
 * The scope reaches **every** scrollable inside [content], including a row nested in the wrapped list. A row that
 * wants the ambient feel back for its own scrolling calls [TvInheritedFocusScroll] — [TvCardRow] and the
 * host app's poster row already do, so the horizontal card runs keep the default parking while the column they sit in
 * holds still.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvStableFocusScroll(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalInheritedBringIntoViewSpec provides LocalBringIntoViewSpec.current,
        LocalBringIntoViewSpec provides MinimumScrollBringIntoViewSpec,
        content = content,
    )
}

/**
 * Restores the bring-into-view spec from outside the enclosing [TvStableFocusScroll], for a scrollable that
 * should keep the ambient feel while the container around it holds still. A no-op with no [TvStableFocusScroll]
 * above, so a component can carry it unconditionally.
 *
 * This is the half of the contract the immersive hub established: the vertical list must not re-pivot as focus
 * crosses its rows, but each row's own horizontal scroll should still park a focused card the way every other
 * carousel does. Baked into [TvCardRow] so a screen gets both halves by drawing an ordinary row.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvInheritedFocusScroll(content: @Composable () -> Unit) {
    val inherited = LocalInheritedBringIntoViewSpec.current
    if (inherited == null) {
        content()
    } else {
        CompositionLocalProvider(LocalBringIntoViewSpec provides inherited, content = content)
    }
}

/** The non-TV default: keep an already visible child in place and scroll only enough to reveal one. */
@OptIn(ExperimentalFoundationApi::class)
val MinimumScrollBringIntoViewSpec: BringIntoViewSpec = object : BringIntoViewSpec {}
