package com.binge.designsystem.component

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * A side sheet that was open when the activity is recreated comes back open, rather than replaying its slide (#442).
 * The sheet starts its slide from the flag's value on its first composition, so that is the value checked.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class BingeModalSideSheetRestoreTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a restored sheet is already shown on its first composition`() {
        val restoration = StateRestorationTester(rule)
        val firstValues = mutableListOf<Boolean>()
        restoration.setContent {
            val shown = rememberSideSheetShown()
            remember { firstValues.add(shown.value) }
            LaunchedEffect(Unit) { shown.value = true }
        }
        rule.waitForIdle()

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals(listOf(false, true), firstValues)
    }
}
