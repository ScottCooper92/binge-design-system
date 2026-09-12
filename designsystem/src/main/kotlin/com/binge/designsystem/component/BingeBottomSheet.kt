package com.binge.designsystem.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
 * In the tabletop posture the sheet caps itself at the crease (#819), so it lands wholly in the flat
 * bottom half and no row is bent across the hinge. Every other window reports no separating
 * horizontal fold and the cap is absent, so nothing else in the app changes height.
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
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = skipPartiallyExpanded,
            // Material3 1.4.0 has no `sheetGesturesEnabled` flag, so vetoing every value change
            // away from Expanded is what pins the locked sheet against drag-to-dismiss/collapse.
            confirmValueChange = { target -> gesturesEnabled || target == SheetValue.Expanded },
        ),
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
