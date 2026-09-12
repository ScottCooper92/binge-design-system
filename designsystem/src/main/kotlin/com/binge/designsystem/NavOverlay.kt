package com.binge.designsystem

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.Dp

/**
 * How much of the window the navigation overlays rather than reserves.
 *
 * The docked bottom bar shrank the content area, so a screen's last row landed above it for free.
 * The floating bar doesn't: it is drawn *over* content, which is the point — content scrolls beneath
 * it. So a scrollable has to pad itself by this much or its final rows sit under the bar with no way
 * to scroll them clear.
 *
 * Both overlaying presentations provide a non-zero value: the floating bar a bottom inset, the
 * expanded rail a start inset plus the same bottom inset (it is the shell at that width, so nothing
 * else holds a scrollable's last row off the gesture bar). The docked bar reserves its space, and
 * full-screen pushes are composed
 * *above* the shell and so never see a provider at all — both read the zero default, which is why
 * consuming this is always safe.
 */
val LocalNavOverlayInsets = compositionLocalOf { PaddingValues() }

/**
 * [base] plus [LocalNavOverlayInsets] — the padding a scrollable inside the nav shell should apply.
 * Pass the screen's own `contentPadding` as [base]; the two are summed rather than replaced, so a
 * screen keeps its own bottom spacing on top of the bar's.
 *
 * [bleedStart] drops the start inset for a surface that runs *under* the rail on purpose — a hub
 * whose artwork reaches the panel edge. Such a surface stays full-bleed and its own children clear
 * the rail individually (see [navOverlayStart]); everything else insets wholesale and leaves the
 * flag alone.
 */
@Composable
fun navOverlayPadding(base: PaddingValues = PaddingValues(), bleedStart: Boolean = false): PaddingValues {
    val overlay = LocalNavOverlayInsets.current
    val direction = LocalLayoutDirection.current
    val start = if (bleedStart) dimensionResource(R.dimen.zero) else overlay.calculateStartPadding(direction)
    return PaddingValues(
        start = base.calculateStartPadding(direction) + start,
        top = base.calculateTopPadding() + overlay.calculateTopPadding(),
        end = base.calculateEndPadding(direction) + overlay.calculateEndPadding(direction),
        bottom = base.calculateBottomPadding() + overlay.calculateBottomPadding(),
    )
}

/**
 * Just the start inset from [LocalNavOverlayInsets] — what a *child* of a full-bleed surface pads its
 * first column by so it clears the rail while the surface behind it keeps reaching the panel edge.
 *
 * This is the opt-out half of the overlay contract: bleeding is the default, and the things that must
 * stay clear (a section header, a carousel's first card, the hero's copy) opt out. The inverse — each
 * bleeding surface widening itself back out past an inset it was given — is the model the TV shell
 * tried first and replaced, because every new full-bleed surface had to remember the trick.
 */
@Composable
fun navOverlayStart(): Dp = LocalNavOverlayInsets.current.calculateStartPadding(LocalLayoutDirection.current)
