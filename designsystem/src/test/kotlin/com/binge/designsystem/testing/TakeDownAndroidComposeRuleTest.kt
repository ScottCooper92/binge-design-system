package com.binge.designsystem.testing

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The take-down Android rule hands out the scenario, so a test can recreate its activity with `scenario.recreate()` and needs no
 * wrapper of its own. A recreated activity has no content; the test sets it again through the scenario, and the rule still takes
 * that content down when the test ends.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class TakeDownAndroidComposeRuleTest {
    @get:Rule
    val rule = createTakeDownAndroidComposeRule<ComponentActivity>()

    @Test
    fun `the scenario hosts the rule's activity`() {
        var hosted: ComponentActivity? = null
        rule.scenario.onActivity { hosted = it }

        assertSame(rule.activity, hosted)
    }

    @Test
    fun `a recreation through the scenario gives a new activity that can show content again`() {
        rule.setContent { Text("before") }
        rule.onNodeWithText("before").assertIsDisplayed()
        val first = rule.activity

        rule.scenario.recreate()
        rule.scenario.onActivity { it.setContent { Text("after") } }
        rule.waitForIdle()

        assertNotSame(first, rule.activity)
        rule.onNodeWithText("after").assertIsDisplayed()
    }
}
