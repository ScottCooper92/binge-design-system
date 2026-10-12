package com.binge.designsystem.catalog

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FAILED = "Couldn't save your change"

/**
 * The failed-save sample draws its own pill for the frame (under `LocalInspectionMode`), where a snackbar host is still
 * empty. The live catalog app has the template's real one, so it must not draw a second.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w400dp-h800dp")
class FormScreenSaveFailedSampleTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the live sample shows the failure once`() {
        rule.setContent { FormScreenSaveFailedSample() }
        rule.waitForIdle()

        rule.onAllNodesWithText(FAILED).assertCountEquals(1)
    }
}
