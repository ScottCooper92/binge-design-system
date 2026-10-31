package com.binge.designsystem.component

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class, qualifiers = "w900dp-h600dp")
class BingeSheetBackLabelTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `a pushed page names where Back returns to, and the label is the target`() {
        var backs = 0
        rule.setContent {
            BingeExpressiveTheme(dynamicColor = false) {
                BingeTaskSheetPanel(pages = listOf(ROOT_PAGE, "detail"), onPush = {}, onBack = { backs++ }, backLabel = "Companions") {}
            }
        }

        rule.onNodeWithText("Companions").performClick()

        assertEquals(1, backs)
    }
}
