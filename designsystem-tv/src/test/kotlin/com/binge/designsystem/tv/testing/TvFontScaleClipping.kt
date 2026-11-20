package com.binge.designsystem.tv.testing

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.Density
import com.binge.designsystem.testing.assertNoTextClipped
import org.junit.Assert.assertTrue

/**
 * The one font scale a TV surface is checked for clipping at. A television is read from across the room, so
 * Scott decided it stops at 1.3; the phone surfaces go on to 2.0. There is deliberately no scale parameter, so a
 * TV test cannot quietly ask for the larger one.
 */
const val TV_CLIPPING_FONT_SCALE = 1.3f

/**
 * This density with the font scale at [TV_CLIPPING_FONT_SCALE]; provide it as `LocalDensity` around the TV theme.
 * It is a plain function so a test can call it without the Compose compiler.
 */
fun Density.atTvFontScale(): Density = Density(density, fontScale = TV_CLIPPING_FONT_SCALE)

/**
 * Runs `assertNoTextClipped` over the hosted content and fails if it checked fewer than [minChecked] text nodes, so
 * a pass that skipped everything it could not see is not a pass. [what] names the state for the failure message;
 * [root] is the window to check, which a content in a window of its own, such as a sheet, must name.
 */
fun ComposeTestRule.assertNoTvTextClipped(
    what: String,
    minChecked: Int,
    root: SemanticsNodeInteraction = onRoot(useUnmergedTree = true),
) {
    waitForIdle()
    val checked = assertNoTextClipped(root)
    assertTrue("only $checked text nodes were checked for $what", checked >= minChecked)
}
