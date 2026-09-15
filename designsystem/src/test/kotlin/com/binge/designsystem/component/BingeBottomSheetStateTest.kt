package com.binge.designsystem.component

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A sheet locks itself while its action is in flight, and must not move when it does.
 *
 * `rememberSheetState` takes `confirmValueChange` as a `rememberSaveable` input key, so a veto
 * lambda that captures the lock flag is rebuilt on every flip — and with it the whole [SheetState],
 * at `Hidden`. This is the test for that: the state survives the flip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeBottomSheetStateTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `locking a sheet does not rebuild its state`() {
        var gesturesEnabled by mutableStateOf(true)
        val seen = mutableListOf<SheetState>()
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                seen += rememberLockableSheetState(skipPartiallyExpanded = true, gesturesEnabled = gesturesEnabled)
            }
        }
        composeTestRule.waitForIdle()

        gesturesEnabled = false
        composeTestRule.waitForIdle()

        // Recomposed at all, so the assertion below is not passing vacuously.
        assertTrue("expected a recomposition on the flip, saw ${seen.size}", seen.size >= 2)
        assertSame("locking rebuilt the sheet state", seen.first(), seen.last())
        assertEquals(1, seen.distinct().size)
    }

    @Test
    fun `unlocking again does not rebuild it either`() {
        var gesturesEnabled by mutableStateOf(false)
        val seen = mutableListOf<SheetState>()
        composeTestRule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                seen += rememberLockableSheetState(skipPartiallyExpanded = true, gesturesEnabled = gesturesEnabled)
            }
        }
        composeTestRule.waitForIdle()

        gesturesEnabled = true
        composeTestRule.waitForIdle()

        assertTrue("expected a recomposition on the flip, saw ${seen.size}", seen.size >= 2)
        assertSame("unlocking rebuilt the sheet state", seen.first(), seen.last())
    }
}
