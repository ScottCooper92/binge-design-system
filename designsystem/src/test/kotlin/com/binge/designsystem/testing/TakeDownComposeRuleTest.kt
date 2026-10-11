package com.binge.designsystem.testing

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [TakeDownComposeRule] does what it says (#524), or every consumer test built on it goes green for the wrong reason:
 * the content leaves the composition while the inner rule is still live, a test with no content passes, and the
 * test's own failure wins over one in the take-down.
 *
 * Each test drives the rule by hand, so it can see the order of the take-down and the inner rule's own teardown.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TakeDownComposeRuleTest {
    @Test
    fun `the content leaves the composition before the inner rule tears it down`() {
        val inner = MarkedRule(createComposeRule())
        val rule = TakeDownComposeRule(inner)
        var disposedAfterInnerBody: Boolean? = null

        run(rule) {
            rule.setContent { DisposableEffect(Unit) { onDispose { disposedAfterInnerBody = inner.bodyDone } } }
            rule.waitForIdle()
        }

        assertEquals("the content was taken down inside the inner rule's body", false, disposedAfterInnerBody)
    }

    @Test
    fun `a test that never sets content passes`() {
        var ran = false

        run(TakeDownComposeRule(createComposeRule())) { ran = true }

        assertTrue(ran)
    }

    @Test
    fun `the test's own failure wins over one in the take-down`() {
        val rule = TakeDownComposeRule(createComposeRule())

        val thrown =
            runCatching {
                run(rule) {
                    rule.setContent { DisposableEffect(Unit) { onDispose { throw IllegalStateException("take-down") } } }
                    rule.waitForIdle()
                    throw AssertionError("body")
                }
            }.exceptionOrNull()

        assertEquals("body", thrown?.message)
    }

    @Test
    fun `a failing take-down still fails a passing test`() {
        val rule = TakeDownComposeRule(createComposeRule())
        var bodyFinished = false

        val thrown =
            runCatching {
                run(rule) {
                    rule.setContent { DisposableEffect(Unit) { onDispose { throw IllegalStateException("take-down") } } }
                    rule.waitForIdle()
                    bodyFinished = true
                }
            }.exceptionOrNull()

        assertTrue(bodyFinished)
        if (thrown == null) fail("the take-down's failure was swallowed")
        assertFalse(thrown is AssertionError)
    }

    private fun run(rule: ComposeContentTestRule, body: () -> Unit) {
        rule
            .apply(
                object : Statement() {
                    override fun evaluate() = body()
                },
                Description.EMPTY,
            ).evaluate()
    }

    /** [rule], marking when its body (the take-down included) has returned and its own teardown is about to begin. */
    private class MarkedRule(
        private val rule: ComposeContentTestRule,
    ) : ComposeContentTestRule by rule {
        var bodyDone = false

        override fun apply(base: Statement, description: Description): Statement =
            rule.apply(
                object : Statement() {
                    override fun evaluate() {
                        base.evaluate()
                        bodyDone = true
                    }
                },
                description,
            )
    }
}
