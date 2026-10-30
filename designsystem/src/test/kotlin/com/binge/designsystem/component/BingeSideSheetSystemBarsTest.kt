package com.binge.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import com.binge.designsystem.testing.WithWindowInsets
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val StatusBar = 24.dp
private val NavigationBar = 48.dp
private const val CONTENT_TAG = "panel-content"

/**
 * The side sheet's window draws edge to edge, so its panel reserves the system bars itself: content starts below the
 * status bar and ends above the navigation bar, while the panel's surface still runs behind both.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h600dp")
class BingeSideSheetSystemBarsTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `the panel's content sits between the status bar and the navigation bar`() {
        var statusPx = 0
        var navPx = 0
        rule.setContent {
            WithWindowInsets({
                statusPx = StatusBar.roundToPx()
                navPx = NavigationBar.roundToPx()
                WindowInsetsCompat
                    .Builder()
                    .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, statusPx, 0, 0))
                    .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navPx))
                    .build()
            }) {
                BingeExpressiveTheme(dynamicColor = false) {
                    BingeSideSheetPanel {
                        Box(Modifier.fillMaxSize().testTag(CONTENT_TAG))
                    }
                }
            }
        }
        rule.waitForIdle()
        val bounds = rule.onNodeWithTag(CONTENT_TAG).fetchSemanticsNode().boundsInRoot
        val windowHeight = rule
            .onRoot()
            .fetchSemanticsNode()
            .boundsInRoot.bottom
        assertEquals(statusPx.toFloat(), bounds.top, 1f)
        assertEquals(windowHeight - navPx, bounds.bottom, 1f)
    }
}
