package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.testing.layout.TestWindowLayoutInfo
import androidx.window.testing.layout.WindowLayoutInfoPublisherRule
import com.binge.designsystem.rememberFoldSafeBottomHeight
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.testing.tabletopFold
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * That [BingeBottomSheet] really shrinks in the tabletop posture.
 *
 * The arithmetic is [com.binge.designsystem.FoldSafeBottomHeightTest]'s; what is unproven
 * without rendering is the wiring — `ModalBottomSheet` composes the caller's `Modifier` alongside its
 * own width, drag-anchor and inset chain, and a `heightIn` it happened to measure around would leave
 * the cap computed, passed, and silently ignored. So the assertion is that the sheet ends up exactly
 * as tall as the value the API handed it, not merely shorter than it was.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w800dp-h1000dp-xhdpi")
class BingeBottomSheetFoldTest {
    private val publisher = WindowLayoutInfoPublisherRule()
    private val compose = createTakeDownComposeRule()

    @get:Rule
    val rules: RuleChain = RuleChain.outerRule(publisher).around(compose)

    private var cap: Dp? = null

    @Test
    fun `a tall sheet caps itself at the crease and is unbounded without one`() {
        showTallSheet()
        val unfolded = sheetHeight()
        assertNull("a window with no fold offered a cap", cap)

        publisher.overrideWindowLayoutInfo(TestWindowLayoutInfo(listOf(tabletopFold())))
        compose.waitForIdle()

        val expected = cap
        assertNotNull("the tabletop fold produced no cap, so the sheet was never asked to shrink", expected)
        assertEquals("the sheet ignored the cap it was given", expected, sheetHeight())
        assertTrue("the cap did not shrink the sheet, so this proves nothing", sheetHeight() < unfolded)
    }

    private fun showTallSheet() {
        compose.setContent {
            BingeExpressiveTheme {
                cap = rememberFoldSafeBottomHeight()
                BingeBottomSheet(onDismissRequest = {}, modifier = Modifier.testTag(SHEET)) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(TALLER_THAN_ANY_WINDOW),
                    )
                }
            }
        }
        compose.waitForIdle()
    }

    private fun sheetHeight(): Dp {
        val bounds = compose.onNodeWithTag(SHEET).getUnclippedBoundsInRoot()
        return bounds.bottom - bounds.top
    }

    private companion object {
        const val SHEET = "sheet"

        val TALLER_THAN_ANY_WINDOW = 4000.dp
    }
}
