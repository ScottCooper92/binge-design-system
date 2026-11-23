package com.binge.designsystem.template

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.binge.designsystem.testing.ShadowMeshSpecification
import com.binge.designsystem.testing.createTakeDownComposeRule
import com.binge.designsystem.theme.BingeExpressiveTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** An app's own lazy item keyed like the hero's old plain key lays out beside the hero, where it used to crash as a duplicate key. */
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [34],
    application = android.app.Application::class,
    shadows = [ShadowMeshSpecification::class],
    qualifiers = "w411dp-h891dp-xhdpi",
)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HeroDetailLazyKeyTest {
    @get:Rule
    val rule = createTakeDownComposeRule()

    @Test
    fun `an item keyed like the old hero key renders beside the hero`() {
        rule.setContent {
            BingeExpressiveTheme {
                HeroDetailLazyScreen(
                    title = "Title",
                    onBack = {},
                    hero = { Text("The hero") },
                ) {
                    items(listOf("hero-detail-hero"), key = { it }) { Text("Odd key") }
                }
            }
        }

        rule.onNodeWithText("The hero").assertIsDisplayed()
        rule.onNodeWithText("Odd key").assertIsDisplayed()
    }
}
