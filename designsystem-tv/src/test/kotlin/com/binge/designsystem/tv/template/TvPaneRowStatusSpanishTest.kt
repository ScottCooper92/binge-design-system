package com.binge.designsystem.tv.template

import android.app.Application
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.binge.designsystem.tv.theme.BingeTvTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val KEY = "companions"

/** The status mark's words come from this module's Spanish strings, plural included, when the device is in Spanish. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "es-w960dp-h540dp-television-xhdpi")
class TvPaneRowStatusSpanishTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `a count of one takes the singular`() {
        setRow(badgeCount = 1)

        composeTestRule.onNode(hasContentDescription("1 novedad"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun `a count of several takes the plural`() {
        setRow(badgeCount = 3)

        composeTestRule.onNode(hasContentDescription("3 novedades"), useUnmergedTree = true).assertExists()
    }

    @Test
    fun `an alert is described in Spanish`() {
        setRow(badgeAlert = true)

        composeTestRule.onNode(hasContentDescription("Requiere atención"), useUnmergedTree = true).assertExists()
    }

    private fun setRow(badgeCount: Int? = null, badgeAlert: Boolean = false) {
        composeTestRule.setContent {
            BingeTvTheme {
                TvListPaneBoard(
                    title = "Ajustes",
                    groups =
                        listOf(
                            TvPaneGroup(
                                title = "Aplicaciones",
                                rows =
                                    listOf(
                                        TvPaneRow(
                                            key = KEY,
                                            label = "Aplicaciones complementarias",
                                            body = "",
                                            badgeCount = badgeCount,
                                            badgeAlert = badgeAlert,
                                        ),
                                    ),
                            ),
                        ),
                    focusedKey = KEY,
                    onFocusRow = {},
                )
            }
        }
    }
}
