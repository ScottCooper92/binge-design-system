package com.binge.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.binge.designsystem.R
import com.binge.designsystem.rememberFoldSafeBottomHeight

/**
 * Wraps [ModalBottomSheet] with the binge defaults: surfaceContainerHigh container,
 * onSurface content, expressive corner radius, and `skipPartiallyExpanded = true`. For
 * advanced callers that need a reference to the sheet state, use [ModalBottomSheet] directly.
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
 * No `contentWindowInsets` is passed, so this takes [ModalBottomSheet]'s default —
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
    content: @Composable ColumnScope.() -> Unit,
) {
    val foldSafeHeight = rememberFoldSafeBottomHeight()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        // heightIn caps rather than sets, so a sheet already shorter than the crease is untouched.
        modifier = foldSafeHeight?.let { modifier.heightIn(max = it) } ?: modifier,
        sheetState = rememberLockableSheetState(skipPartiallyExpanded, gesturesEnabled),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(
            topStart = dimensionResource(R.dimen.bottom_sheet_corner),
            topEnd = dimensionResource(R.dimen.bottom_sheet_corner),
        ),
        // A confirm sheet suppresses only scrim-tap dismiss (so an accidental outside tap can't
        // discard an in-progress choice) while leaving back-press and drag as deliberate cancels —
        // pass dismissOnClickOutside = false with gesturesEnabled = true for that.
        properties = ModalBottomSheetProperties(
            shouldDismissOnBackPress = gesturesEnabled,
            shouldDismissOnClickOutside = dismissOnClickOutside,
        ),
        content = content,
    )
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
    return rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded,
        confirmValueChange = confirmValueChange,
    )
}
