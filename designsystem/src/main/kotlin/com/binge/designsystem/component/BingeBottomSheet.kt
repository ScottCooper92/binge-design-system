package com.binge.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.binge.designsystem.rememberFoldSafeBottomHeight
import com.binge.designsystem.theme.BingeShapes
import kotlinx.coroutines.launch

/**
 * Wraps [ModalBottomSheet] with the binge defaults: surfaceContainerHigh container,
 * onSurface content, expressive corner radius, and `skipPartiallyExpanded = true`. For
 * advanced callers that need a reference to the sheet state, use [ModalBottomSheet] directly.
 *
 * [dockable] opts the sheet into docking, usually with `skipPartiallyExpanded = false` so it opens part-way: as
 * its top edge reaches the status bar its top corners square off and [BingeSheetDockingHeader] swaps the drag handle
 * and header for a [BingeSheetTopBar], and [bingeSheetPinnedFooter] keeps a footer on the window's bottom edge while
 * the sheet is part-way open. Both read [LocalBingeSheetDock], which only a dockable sheet provides. A dockable sheet
 * draws no drag handle of its own; [BingeSheetDockingHeader] draws it.
 *
 * When [gesturesEnabled] is `false` the sheet is **locked**: drag-to-dismiss / partial-collapse,
 * scrim-tap dismiss and back-press dismiss are all suppressed, so the sheet stays fully expanded
 * and a scrolling child (e.g. an expanded [TextEntrySurface]) never fights the sheet's drag. The
 * host drives this from its own expanded state and remains responsible for an explicit close
 * affordance while locked.
 *
 * In the tabletop posture the sheet caps itself at the crease, so it lands wholly in the flat
 * bottom half and no row is bent across the hinge. Every other window reports no separating
 * horizontal fold and the cap is absent, so nothing else in the app changes height.
 *
 * [edgeToEdge] lets the content run under the navigation bar, as a long list does, padding its own end so the last
 * row can scroll clear of the bar. The status bar and the IME are still inset, so a field in the sheet is not covered by the
 * keyboard. Both forms lay the sheet out inside the window's safe sides, so in landscape the sheet clears a side cutout or a side
 * navigation bar.
 *
 * Otherwise the content insets are [ModalBottomSheet]'s default —
 * `BottomSheetDefaults.modalWindowInsets`, `safeDrawing.only(Bottom + Top)` — which already includes
 * the IME (confirmed from `material3:1.5.0-alpha27` bytecode; issue #35). Adding `imePadding()` on
 * top would double-inset. A field that jumps the sheet on focus needs a scrollable ancestor in its
 * own content instead, so the `bringIntoView` request has somewhere local to land — see
 * [TextEntrySurface], whose own KDoc names this sheet as its intended host.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BingeBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    gesturesEnabled: Boolean = true,
    dismissOnClickOutside: Boolean = gesturesEnabled,
    dockable: Boolean = false,
    edgeToEdge: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val foldSafeHeight = rememberFoldSafeBottomHeight()
    val sheetState = rememberLockableSheetState(skipPartiallyExpanded, gesturesEnabled)
    val scope = rememberCoroutineScope()
    val dock =
        if (dockable) {
            rememberBingeSheetDock(
                sheetState = sheetState,
                gesturesEnabled = gesturesEnabled,
                close = {
                    if (gesturesEnabled) {
                        scope.launch { sheetState.hide() }.invokeOnCompletion { if (!sheetState.isVisible) onDismissRequest() }
                    } else {
                        // A locked sheet vetoes hide(), so the explicit close hands the dismissal to the host directly.
                        onDismissRequest()
                    }
                },
                expand = { scope.launch { sheetState.expand() } },
                partialExpand = { scope.launch { sheetState.partialExpand() } },
            )
        } else {
            null
        }
    val dockFraction by remember(dock) { derivedStateOf { dock?.fraction ?: 0f } }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = bingeSheetOuterModifier(modifier, foldSafeHeight),
        sheetState = sheetState,
        contentWindowInsets = { bingeSheetContentInsets(edgeToEdge) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = if (dock != null) DockingSheetShape(dockFraction) else BingeShapes.HeroTop,
        // A docking sheet draws its drag handle itself, inside BingeSheetDockingHeader, so it can fade into the bar.
        dragHandle = if (dock != null) null else ({ BottomSheetDefaults.DragHandle() }),
        // A confirm sheet suppresses only scrim-tap dismiss (so an accidental outside tap can't
        // discard an in-progress choice) while leaving back-press and drag as deliberate cancels —
        // pass dismissOnClickOutside = false with gesturesEnabled = true for that.
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = gesturesEnabled,
            shouldDismissOnClickOutside = dismissOnClickOutside,
        ),
    ) {
        CompositionLocalProvider(LocalBingeSheetDock provides dock) { content() }
    }
}

/**
 * A [SheetState] whose lock can be toggled without the sheet moving.
 *
 * Material 3 still has no `sheetGesturesEnabled` flag, so vetoing every value change away from
 * `Expanded` is what pins a locked sheet against drag-to-dismiss and partial collapse. The catch is
 * where that veto goes: `rememberSheetState` passes `confirmValueChange` as a `rememberSaveable`
 * **input key** (confirmed in material3 1.5.0-alpha27's bytecode), so a lambda that captures
 * `gesturesEnabled` gets a new identity when the flag flips, the key changes, the saved state is
 * discarded, and a fresh `SheetState` is built at its initial value — `Hidden`. The sheet then
 * animates back up. The veto meant to pin the sheet was what unpinned it, once per lock.
 *
 * So the lambda is created once and reads the flag through [rememberUpdatedState] instead. Same
 * rule, stable identity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberLockableSheetState(skipPartiallyExpanded: Boolean, gesturesEnabled: Boolean): SheetState {
    val locked by rememberUpdatedState(!gesturesEnabled)
    val confirmValueChange = remember { { target: SheetValue -> !locked || target == SheetValue.Expanded } }
    // rememberBottomSheetState replaced the skipPartiallyExpanded flag with an explicit reachable-states
    // set — Hidden/Expanded excludes PartiallyExpanded the same way the flag used to.
    val enabledValues =
        if (skipPartiallyExpanded) {
            setOf(SheetValue.Hidden, SheetValue.Expanded)
        } else {
            setOf(SheetValue.Hidden, SheetValue.PartiallyExpanded, SheetValue.Expanded)
        }
    return rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = enabledValues,
        confirmValueChange = confirmValueChange,
    )
}

/**
 * A sheet's content insets: the default's, or for an [edgeToEdge] sheet the default's top and the IME without the
 * navigation bar. The sides are not here: see [bingeSheetSideInsets].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun bingeSheetContentInsets(edgeToEdge: Boolean): WindowInsets =
    if (edgeToEdge) {
        BottomSheetDefaults.modalWindowInsets.only(WindowInsetsSides.Top).union(WindowInsets.ime)
    } else {
        BottomSheetDefaults.modalWindowInsets
    }

/**
 * Lays the sheet out inside the window's safe sides, so a side cutout or a side navigation bar in landscape stays clear
 * of it. The sheet is capped at [BottomSheetDefaults.SheetMaxWidth] and centred, so where the window is wider than that
 * plus the inset it keeps its width and nothing is padded; on a narrow landscape window it narrows to the safe width.
 */
@Composable
internal fun Modifier.bingeSheetSideInsets(): Modifier = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

@Composable
private fun bingeSheetOuterModifier(modifier: Modifier, foldSafeHeight: Dp?): Modifier {
    // heightIn caps rather than sets, so a sheet already shorter than the crease is untouched.
    val sided = modifier.bingeSheetSideInsets()
    return foldSafeHeight?.let { sided.heightIn(max = it) } ?: sided
}
