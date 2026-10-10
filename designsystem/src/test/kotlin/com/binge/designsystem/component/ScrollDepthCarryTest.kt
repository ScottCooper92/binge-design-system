package com.binge.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The deepest index survives a configuration change and nothing else (#449). The configuration-change check is passed
 * in, so the restore stands for either a rotation or the back stack's save of a surface that was left.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class ScrollDepthCarryTest {
    @get:Rule
    val rule = createComposeRule()

    private val reported = mutableListOf<Int?>()
    private var lastVisible by mutableStateOf<Int?>(null)
    private var shown by mutableStateOf(true)
    private var changing = false

    private fun show(): StateRestorationTester {
        val restoration = StateRestorationTester(rule)
        restoration.setContent {
            if (shown) ScrollDepthEffect({ reported += it }, { lastVisible }) { changing }
        }
        rule.waitForIdle()
        return restoration
    }

    private fun scrollTo(index: Int) {
        lastVisible = index
        rule.waitForIdle()
    }

    private fun leave() {
        changing = false
        shown = false
        rule.waitForIdle()
    }

    @Test
    fun `a rotation carries the deepest index, so a visit that scrolled back up reports its depth`() {
        val restoration = show()
        scrollTo(30)
        scrollTo(5)

        changing = true
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        leave()

        assertEquals(listOf<Int?>(30), reported)
    }

    @Test
    fun `any other restore starts a fresh visit`() {
        val restoration = show()
        scrollTo(30)
        scrollTo(5)

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()
        leave()

        assertEquals(listOf<Int?>(30, 5), reported)
    }
}
