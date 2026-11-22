package com.binge.designsystem.template

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.testing.TestTheme
import com.binge.designsystem.testing.createTakeDownComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class CreateFooterTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    private var clicks = 0

    private fun show(
        visible: Boolean = true,
        enabled: Boolean = true,
        loading: Boolean = false,
    ) {
        rule.setContent {
            TestTheme {
                CreateFooter(
                    label = "Add slider",
                    icon = Icons.Filled.Add,
                    onClick = { clicks++ },
                    visible = visible,
                    enabled = enabled,
                    loading = loading,
                )
            }
        }
    }

    @Test
    fun aTapCreates() {
        show()

        rule.onNodeWithText("Add slider").assertIsDisplayed().performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun aPageWithoutTheActionDrawsNothing() {
        show(visible = false)

        rule.onNodeWithText("Add slider").assertDoesNotExist()
    }

    @Test
    fun whileTheListLoadsItCannotBeUsedYet() {
        show(enabled = false)

        rule.onNodeWithText("Add slider").assertIsNotEnabled()
    }

    @Test
    fun anEnabledFooterTakesTaps() {
        show(enabled = true)

        rule.onNodeWithText("Add slider").assertIsEnabled()
    }

    @Test
    fun aCreateInFlightTakesNoTaps() {
        show(loading = true)

        rule.onNode(hasClickAction()).performClick()

        assertEquals(0, clicks)
    }
}
