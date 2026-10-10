package com.binge.designsystem.testing

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [createTakeDownComposeRule] must remove the content before the rule disposes it, or every consumer's pager test goes
 * back to failing at random, far from the cause. Each test applies the rule by hand so it can look at what happens
 * after the test body has finished.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TakeDownComposeRuleTest {
    private fun run(rule: org.junit.rules.TestRule, body: () -> Unit) {
        rule
            .apply(
                object : Statement() {
                    override fun evaluate() = body()
                },
                Description.EMPTY,
            ).evaluate()
    }

    @Test
    fun `the content has left the composition before the rule disposes it`() {
        val inner = createComposeRule()
        var disposed = false
        var disposedBeforeTeardown = false
        // Looks at the content after the take-down but before the inner rule's own teardown, which would dispose it anyway.
        val observed = object : ComposeContentTestRule by inner {
            override fun apply(base: Statement, description: Description): Statement =
                inner.apply(
                    object : Statement() {
                        override fun evaluate() {
                            base.evaluate()
                            disposedBeforeTeardown = disposed
                        }
                    },
                    description,
                )
        }
        val rule = TakeDownComposeRule(observed)
        run(rule) {
            rule.setContent { DisposableEffect(Unit) { onDispose { disposed = true } } }
            rule.waitForIdle()
            assertFalse("the content is still showing during the body", disposed)
        }
        assertTrue("the rule left the content composed", disposedBeforeTeardown)
    }

    @Test
    fun `a test that never sets content passes`() {
        run(createTakeDownComposeRule()) {}
    }

    @Test
    fun `the test's own failure wins over one thrown while taking the content down`() {
        val rule = createTakeDownComposeRule()
        val thrown = assertThrows(IllegalStateException::class.java) {
            run(rule) {
                rule.setContent { DisposableEffect(Unit) { onDispose { throw IllegalArgumentException("take-down") } } }
                rule.waitForIdle()
                throw IllegalStateException("body")
            }
        }
        assertEquals("body", thrown.message)
    }

    @Test
    fun `a failure while taking the content down fails a test that otherwise passed`() {
        val rule = createTakeDownComposeRule()
        val thrown = assertThrows(IllegalArgumentException::class.java) {
            run(rule) {
                rule.setContent { DisposableEffect(Unit) { onDispose { throw IllegalArgumentException("take-down") } } }
                rule.waitForIdle()
            }
        }
        assertEquals("take-down", thrown.message)
    }
}
