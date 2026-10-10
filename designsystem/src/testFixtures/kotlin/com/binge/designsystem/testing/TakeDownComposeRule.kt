package com.binge.designsystem.testing

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * `createComposeRule()` with the content taken down before the rule disposes it, for a Robolectric suite that collects
 * `LazyPagingItems` anywhere.
 *
 * A text field still focused when a rule disposes its composition leaves work pending on Compose's shared UI dispatcher.
 * Robolectric discards that work, and the dispatcher never runs anything again. `LazyPagingItems` captures that
 * dispatcher, so the next test to collect paged items stays on its loading state for good: a focus test sorted ahead of
 * a pager test fails the pager test, not itself. Removing the content while the looper is still live lets the
 * dispatcher drain, and doing it in the rule means no test has to remember to.
 */
fun createTakeDownComposeRule(): ComposeContentTestRule = TakeDownComposeRule(createComposeRule())

/** [createTakeDownComposeRule] in keyboard input mode, for a test that drives focus with keys; see [createKeyboardComposeRule]. */
fun createTakeDownKeyboardComposeRule(): ComposeContentTestRule = TakeDownComposeRule(createComposeRule(KeyboardComposeUiTestConfig))

/** [createTakeDownComposeRule] for a test that needs the hosting activity, such as one pressing Back through its dispatcher. */
inline fun <reified A : ComponentActivity> createTakeDownAndroidComposeRule(): TakeDownAndroidComposeRule<A> =
    TakeDownAndroidComposeRule(createAndroidComposeRule<A>())

/** [createTakeDownAndroidComposeRule] in keyboard input mode. */
inline fun <reified A : ComponentActivity> createTakeDownKeyboardAndroidComposeRule(): TakeDownAndroidComposeRule<A> =
    TakeDownAndroidComposeRule(createAndroidComposeRule<A>(KeyboardComposeUiTestConfig))

/** An activity-hosted compose rule whose content is taken down before it disposes; see [createTakeDownComposeRule]. */
class TakeDownAndroidComposeRule<A : ComponentActivity>(
    private val rule: AndroidComposeTestRule<*, A>,
) : ComposeContentTestRule by TakeDownComposeRule(rule) {
    val activity: A get() = rule.activity
}

/** Wraps [rule] so its content is removed, and the looper drained, before the rule disposes it. */
class TakeDownComposeRule(
    private val rule: ComposeContentTestRule,
) : ComposeContentTestRule by rule {
    private var shown by mutableStateOf(true)
    private var composed = false

    override fun setContent(composable: @Composable () -> Unit) {
        composed = true
        rule.setContent { if (shown) composable() }
    }

    override fun apply(base: Statement, description: Description): Statement =
        rule.apply(
            object : Statement() {
                override fun evaluate() {
                    // The test's own failure wins over one in the take-down.
                    val outcome = runCatching { base.evaluate() }
                    val takenDown = runCatching { takeDown() }
                    outcome.getOrThrow()
                    takenDown.getOrThrow()
                }
            },
            description,
        )

    private fun takeDown() {
        if (!composed) return
        shown = false
        rule.waitForIdle()
    }
}
