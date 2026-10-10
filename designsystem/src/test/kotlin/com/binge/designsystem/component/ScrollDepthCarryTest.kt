package com.binge.designsystem.component

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
 * The deepest index survives a configuration change and nothing else (#449), including one that reaches an activity
 * that is already stopped (#503). The configuration-change check is passed in, so a test chooses whether a disposal is a
 * leave or a relaunch.
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
    fun `a visit that left starts fresh when its surface comes back from the back stack`() {
        var show by mutableStateOf(true)
        rule.setContent {
            val holder = rememberSaveableStateHolder()
            if (show) holder.SaveableStateProvider("surface") { ScrollDepthEffect({ reported += it }, { lastVisible }) { changing } }
        }
        rule.waitForIdle()
        scrollTo(30)
        scrollTo(5)

        show = false
        rule.waitForIdle()
        show = true
        rule.waitForIdle()
        show = false
        rule.waitForIdle()

        assertEquals(listOf<Int?>(30, 5), reported)
    }

    @Test
    fun `a configuration change that reaches an already stopped activity restores the stop's save`() {
        val stopped = SaveableStateRegistry(restoredValues = null, canBeSaved = { true })
        var registry by mutableStateOf(stopped)
        rule.setContent {
            CompositionLocalProvider(LocalSaveableStateRegistry provides registry) {
                if (shown) ScrollDepthEffect({ reported += it }, { lastVisible }) { changing }
            }
        }
        rule.waitForIdle()
        scrollTo(30)
        scrollTo(5)

        // The stop saves the live visit, with no configuration change under way yet.
        val savedAtStop = stopped.performSave()
        // The change then relaunches the activity: the disposal is not a leave, and it saves nothing more.
        changing = true
        shown = false
        rule.waitForIdle()
        reported.clear()
        registry = SaveableStateRegistry(restoredValues = savedAtStop, canBeSaved = { true })
        changing = false
        shown = true
        rule.waitForIdle()
        leave()

        assertEquals(listOf<Int?>(30), reported)
    }
}
