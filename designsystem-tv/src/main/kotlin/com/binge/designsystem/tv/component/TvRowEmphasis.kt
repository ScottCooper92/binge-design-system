package com.binge.designsystem.tv.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.MaterialTheme

/**
 * How strongly a row in a two-pane D-pad list is marked: resting, current, or focused.
 *
 * [Current] is the row the right-hand pane describes while focus is in that pane. Painting it with the same
 * focus fill makes two things look focused at once while the user chooses a value. An enum rather than a
 * boolean pair makes "resting and focused" unrepresentable: the three states are mutually exclusive in fact,
 * so also in the type.
 *
 * Rows do not compute this. The containing column observes its subtree and derives it with [tvRowEmphasis].
 */
enum class TvRowEmphasis {
    /** Not the row the pane describes. Painted with whatever resting pair the caller already had. */
    Resting,

    /** The row the pane describes, while focus is elsewhere. Dim accent — see [containerColor]. */
    Current,

    /** The row the pane describes, with focus in the column holding it. The full focus fill. */
    Focused,
}

/**
 * The one derivation, in one place: which of the three states a row is in.
 *
 * [isDescribedRow] is the caller's hoisted selection matched against this row; [columnHasFocus] is `hasFocus`
 * for the whole column subtree. It leans on an invariant a two-pane list holds: every row reports focus
 * arrival through its own `onFocusChanged`, so whenever the column has focus the row holding it is the
 * described row. That is why "described and column focused" reads as "this row is focused" without a third
 * input.
 *
 * A plain function rather than something inlined per surface, so two surfaces provably answer it the same.
 */
fun tvRowEmphasis(isDescribedRow: Boolean, columnHasFocus: Boolean): TvRowEmphasis =
    when {
        !isDescribedRow -> TvRowEmphasis.Resting
        columnHasFocus -> TvRowEmphasis.Focused
        else -> TvRowEmphasis.Current
    }

/**
 * The container behind a row at this emphasis, falling through to [resting] where there is nothing to mark.
 *
 * [TvRowEmphasis.Current] is a designed token pair rather than an alpha over the surface: dropping the accent
 * to a fraction composites against whatever is behind the row, so its contrast cannot be guaranteed. A
 * neutral grey was tried and is indistinguishable from an ordinary row at ten feet, which marks nothing.
 */
@Composable
fun TvRowEmphasis.containerColor(resting: Color): Color =
    when (this) {
        TvRowEmphasis.Resting -> resting
        TvRowEmphasis.Current -> MaterialTheme.colorScheme.primaryContainer
        TvRowEmphasis.Focused -> MaterialTheme.colorScheme.primary
    }

/**
 * The content colour to pair with [containerColor] at this emphasis, falling through to [resting].
 *
 * Split from the container because `ListItemColors` wants the two separately, and because the resting content
 * colour is not the resting container's partner: a chosen value's accent label is the selection channel,
 * which is none of this function's business.
 */
@Composable
fun TvRowEmphasis.contentColor(resting: Color): Color =
    when (this) {
        TvRowEmphasis.Resting -> resting
        TvRowEmphasis.Current -> MaterialTheme.colorScheme.onPrimaryContainer
        TvRowEmphasis.Focused -> MaterialTheme.colorScheme.onPrimary
    }
