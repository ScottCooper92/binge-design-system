package com.binge.designsystem.testing

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The in-memory restoration tester does what a configuration change does to a test's content: it saves what `rememberSaveable`
 * holds, takes the content down, and composes it again from what it saved.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class InMemoryStateRestorationTesterTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `what rememberSaveable holds comes back after the restore`() {
        val tester = InMemoryStateRestorationTester(rule)
        var read = -1
        var write: (Int) -> Unit = {}
        tester.setContent {
            var count by rememberSaveable { mutableIntStateOf(0) }
            write = { count = it }
            read = count
        }
        rule.waitForIdle()
        rule.runOnIdle { write(7) }
        rule.waitForIdle()

        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals("the restored composition started from what was saved, not from its initial 0", 7, read)
    }

    @Test
    fun `the content is taken down and composed again`() {
        val tester = InMemoryStateRestorationTester(rule)
        var composed = 0
        var disposed = 0
        tester.setContent {
            DisposableEffect(Unit) {
                composed++
                onDispose { disposed++ }
            }
        }
        rule.waitForIdle()
        assertEquals(1, composed)

        tester.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals(1, disposed)
        assertEquals(2, composed)
    }

    @Test
    fun `the registry still refuses a value the platform could not put in a Bundle`() {
        val tester = InMemoryStateRestorationTester(rule)
        var savesAnObject: Boolean? = null
        var savesAString: Boolean? = null
        tester.setContent {
            val registry = requireNotNull(LocalSaveableStateRegistry.current)
            savesAnObject = registry.canBeSaved(Any())
            savesAString = registry.canBeSaved("text")
        }
        rule.waitForIdle()

        assertEquals(false, savesAnObject)
        assertEquals(true, savesAString)
    }
}
