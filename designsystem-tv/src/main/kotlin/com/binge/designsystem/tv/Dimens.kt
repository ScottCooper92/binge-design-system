package com.binge.designsystem.tv

// Dp values are in res/values/dimens.xml (access via dimensionResource)

/**
 * Rows the **collapsed** nav rail's band shows while the list's tail is off screen. The collapsed strip is the
 * expanded scroll frozen at the same position, cut to this many rows with a fade above the pinned footer
 * stand-in; once the tail (through the terminal footer row) is on screen, nothing is cut and this cap does not
 * apply. The pivot parks the focused row at this band's **centre** slot (`(n - 1) / 2`), which is what keeps the
 * focused destination inside the band — and therefore visible — whenever the rail collapses.
 */
const val TV_NAV_RAIL_COLLAPSED_ITEMS = 7

/** The backdrop and rail crossfade, shared so a rail expanding over artwork and the artwork changing move together. */
const val TV_IMMERSIVE_CROSSFADE_MILLIS = 320
